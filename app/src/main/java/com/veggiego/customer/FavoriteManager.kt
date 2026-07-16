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

    ) {

        val uid =
            auth.currentUser?.uid ?: return

        val docRef =

            db.collection("favorites")
                .document(uid)
                .collection("restaurants")
                .document(restaurant.id)

        val snapshot =
            docRef.get().await()

        if (snapshot.exists()) {

            docRef.delete()

        } else {

            docRef.set(restaurant).await()

            android.util.Log.d(
                "FIRESTORE",
                "Restaurant Saved"
            )
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

        val snapshot =

            db.collection("favorites")
                .document(uid)
                .collection("restaurants")
                .document(restaurantId)
                .get()
                .await()

        return snapshot.exists()
    }
}