package com.veggiego.customer

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FavoriteManager {

    private val db =
        FirebaseFirestore.getInstance()

    private val auth =
        FirebaseAuth.getInstance()

    suspend fun toggleRestaurantFavorite(

        restaurant: FavoriteRestaurant

    ): Boolean {

        val uid =
            auth.currentUser?.uid
                ?: return false

        if (restaurant.id.isBlank()) {

            android.util.Log.e(
                "FAVORITE_ERROR",
                "Restaurant ID is empty"
            )

            return false
        }

        return try {

            val docRef =

                db.collection("favorites")
                    .document(uid)
                    .collection("restaurants")
                    .document(restaurant.id)

            val snapshot =
                docRef.get().await()

            if (snapshot.exists()) {

                docRef.delete().await()

                false

            } else {

                docRef.set(restaurant).await()

                true
            }

        } catch (e: Exception) {

            android.util.Log.e(
                "FAVORITE_ERROR",
                "Favorite update failed",
                e
            )

            false
        }
    }

    suspend fun toggleFoodFavorite(

        food: FavoriteFood

    ) {

        val uid =
            auth.currentUser?.uid ?: return

        val docRef =

            db.collection("favorites")
                .document(uid)
                .collection("foods")
                .document(food.id)

        val snapshot =
            docRef.get().await()

        if (snapshot.exists()) {

            docRef.delete()

        } else {

            docRef.set(food)
        }
    }

    suspend fun isRestaurantFavorite(

        restaurantId: String

    ): Boolean {

        val uid =
            auth.currentUser?.uid
                ?: return false

        if (restaurantId.isBlank()) {

            return false
        }

        return try {

            val snapshot =

                db.collection("favorites")
                    .document(uid)
                    .collection("restaurants")
                    .document(restaurantId)
                    .get()
                    .await()

            snapshot.exists()

        } catch (e: Exception) {

            android.util.Log.e(
                "FAVORITE_ERROR",
                "Unable to check favorite",
                e
            )

            false
        }
    }
}