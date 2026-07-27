const {
    setGlobalOptions
} = require("firebase-functions/v2");

const {
    onDocumentWritten
} = require("firebase-functions/v2/firestore");

const {
    initializeApp
} = require("firebase-admin/app");

const {
    getFirestore
} = require("firebase-admin/firestore");

const {
    getMessaging
} = require("firebase-admin/messaging");

const logger =
    require("firebase-functions/logger");

initializeApp();

setGlobalOptions({

    region:
        "asia-south1",

    maxInstances:
        10
});


exports.restaurantNewOrderNotification =
    onDocumentWritten(
        "orders/{orderId}",
        async (event) => {

            const afterSnapshot =
                event.data?.after;

            if (
                !afterSnapshot ||
                !afterSnapshot.exists
            ) {
                return null;
            }

            const beforeSnapshot =
                event.data?.before;

            const after =
                afterSnapshot.data();

            const before =
                beforeSnapshot &&
                beforeSnapshot.exists

                    ? beforeSnapshot.data()

                    : null;

            const currentStatus =
                String(
                    after.status || ""
                ).toUpperCase();

            const previousStatus =
                String(
                    before?.status || ""
                ).toUpperCase();

            const allowedStatuses = [

                "APPROVED",

                "RESTAURANT_PENDING"
            ];

            if (
                !allowedStatuses.includes(
                    currentStatus
                )
            ) {

                return null;
            }

            if (
                currentStatus ===
                previousStatus
            ) {

                return null;
            }

            const restaurantId =
                String(
                    after.restaurantId || ""
                );

            const orderId =
                String(
                    event.params.orderId
                );

            if (!restaurantId) {

                logger.error(
                    "Restaurant ID missing",
                    {
                        orderId:
                            orderId
                    }
                );

                return null;
            }

            const restaurantDocument =
                await getFirestore()
                    .collection(
                        "restaurants"
                    )
                    .doc(
                        restaurantId
                    )
                    .get();

            if (
                !restaurantDocument.exists
            ) {

                logger.error(
                    "Restaurant not found",
                    {
                        restaurantId:
                            restaurantId,

                        orderId:
                            orderId
                    }
                );

                return null;
            }

            const restaurantToken =
                restaurantDocument
                    .data()
                    ?.fcmToken;

            if (!restaurantToken) {

                logger.error(
                    "Restaurant FCM token missing",
                    {
                        restaurantId:
                            restaurantId,

                        orderId:
                            orderId
                    }
                );

                return null;
            }

            const customerName =
                String(
                    after.customerName ||
                    "Customer"
                );

            await getMessaging()
                .send({

                    token:
                        restaurantToken,

                    data: {

                        title:
                            "🍔 New Order Received",

                        body:
                            `${customerName} placed a new order`,

                        orderId:
                            orderId,

                        type:
                            "NEW_ORDER"
                    },

                    android: {

                        priority:
                            "high",

                        ttl:
                            3600000
                    }
                });

            logger.info(
                "Restaurant notification sent",
                {
                    restaurantId:
                        restaurantId,

                    orderId:
                        orderId
                }
            );

            return null;
        }
    );