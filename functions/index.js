const functions = require("firebase-functions/v1");

const admin = require("firebase-admin");

admin.initializeApp();

exports.orderStatusNotification = functions.firestore
    .document("orders/{orderId}")

    .onUpdate(async (change, context) => {

        const before =
            change.before.data();

        const after =
            change.after.data();

        // ✅ STATUS CHANGED

        if (

            before.status !== after.status

        ) {

            let title =
                "VeggieGo";

            let body = "";

            // ✅ STATUS MESSAGE

            switch (after.status) {

                case "ACCEPTED":

                    body =
                        "🍕 Order Accepted";
                    break;

                case "PREPARING":

                    body =
                        "👨‍🍳 Preparing Your Food";
                    break;

                case "PICKED_UP":

                    body =
                        "🛵 Rider Picked Your Order";
                    break;

                case "OUT_FOR_DELIVERY":

                    body =
                        "🚚 Out For Delivery";
                    break;

                case "DELIVERED":

                    body =
                        "✅ Order Delivered";
                    break;

                default:

                    body =
                        "📦 Order Update";
            }

            // ✅ GET TOKEN

            const tokenDoc =

                await admin
                    .firestore()

                    .collection("tokens")
                    .doc("customer")
                    .get();

            const token =
                tokenDoc.data()?.token;

            if (!token) {

                console.log(
                    "❌ No token found"
                );

                return null;
            }

            // ✅ SEND NOTIFICATION

            const message = {

                notification: {

                    title: title,

                    body: body
                },

                token: token
            };

            await admin
                .messaging()
                .send(message);

            console.log(
                "✅ Notification Sent"
            );
        }

        return null;
    });
exports.chatNotification = functions.firestore

    .document(
        "chats/{orderId}/messages/{messageId}"
    )

    .onCreate(async (snap, context) => {

        const msg =
            snap.data();

        const orderId =
            context.params.orderId;

        // ✅ GET ORDER

        const orderDoc =

            await admin
                .firestore()
                .collection("orders")
                .doc(orderId)
                .get();

        const order =
            orderDoc.data();

        if (!order) {

            return null;
        }

        let token = "";

        // ✅ CUSTOMER MESSAGE → RIDER

        if (
            msg.senderType === "customer"
        ) {

            const riderId =
                order.riderId;

            if (!riderId)
                return null;

            const riderDoc =

                await admin
                    .firestore()
                    .collection("riders")
                    .doc(riderId)
                    .get();

            token =
                riderDoc.data()?.fcmToken || "";
        }

        // ✅ RIDER MESSAGE → CUSTOMER

        else {

            const customerId =
                order.userId;

            if (!customerId)
                return null;

            const customerDoc =

                await admin
                    .firestore()
                    .collection("users")
                    .doc(customerId)
                    .get();

            token =
                customerDoc.data()?.fcmToken || "";
        }

        if (!token) {

            console.log(
                "❌ No token found"
            );

            return null;
        }
        console.log(
                "CUSTOMER TOKEN:",
                token
            );

        // ✅ SEND PUSH

        const payload = {

            notification: {

                title:

                    msg.senderType ===
                    "customer"

                        ? "💬 Customer Message"

                        : "💬 Rider Message",

                body: msg.message
            },

            data: {

                title:

                    msg.senderType ===
                    "customer"

                        ? "💬 Customer Message"

                        : "💬 Rider Message",

                body: msg.message
            },

            android: {

                priority: "high",

                notification: {

                    sound: "default",

                    channelId: "veggiego_channel"
                }
            },

            token: token
        };

        await admin
            .messaging()
            .send(payload);

        return null;
    });