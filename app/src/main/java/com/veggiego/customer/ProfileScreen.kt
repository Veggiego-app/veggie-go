package com.veggiego.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Scaffold
import android.content.Intent
import android.net.Uri

@Composable
fun ProfileScreen(
    navController: NavController
) {
    val context =
        LocalContext.current

    // ✅ USER DATA
    var name by remember {
        mutableStateOf("")
    }

    var phone by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {

        val uid =

            FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid ?: ""

        FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(uid)
            .get()

            .addOnSuccessListener { user ->

                name =
                    user.getString("name")
                        ?: user.getString("fullName")
                                ?: "Customer"

                phone =
                    user.getString("phone")
                        ?: FirebaseAuth
                            .getInstance()
                            .currentUser
                            ?.phoneNumber
                                ?: ""

            }
    }

    var editMode by remember {
        mutableStateOf(false)
    }
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }
    Scaffold(

        bottomBar = {

            BottomBar(
                navController
            )

        }

    ) { padding ->
        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .background(Color.White)
                    .padding(20.dp)

        ) {

            Spacer(
                modifier = Modifier.height(30.dp)
            )

            // ✅ TITLE
            Text(

                text = "👤 My Profile",

                fontSize = 25.sp,

                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(15.dp)
            )

            // ✅ PROFILE CARD
            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(24.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    )

            ) {

                Column(

                    modifier =
                        Modifier.padding(20.dp)

                ) {

                    // ✅ NAME
                    Text(
                        text = "Name",
                        color = Color.Gray
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    if (editMode) {

                        OutlinedTextField(

                            value = name,

                            onValueChange = {
                                name = it
                            },

                            modifier =
                                Modifier.fillMaxWidth()
                        )

                    } else {

                        Text(

                            text = name,

                            fontSize = 22.sp,

                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Divider()

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    // ✅ PHONE
                    Text(
                        text = "Phone",
                        color = Color.Gray
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    if (editMode) {

                        OutlinedTextField(

                            value = phone,

                            onValueChange = {
                                phone = it
                            },

                            modifier =
                                Modifier.fillMaxWidth()
                        )

                    } else {

                        Text(

                            text = phone,

                            fontSize = 20.sp,

                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Divider()

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            // ✅ EDIT / SAVE BUTTON
            Button(

                onClick = {

                    editMode = !editMode
                },

                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    )

            ) {

                Text(

                    text =
                        if (editMode)
                            "Save Profile"
                        else
                            "Edit Profile"
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // ✅ ORDER HISTORY
            Button(

                onClick = {

                    navController.navigate("orders")
                },

                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    )

            ) {

                Text(
                    text = "📦 Order History"
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )
            Button(

                onClick = {

                    navController.navigate(
                        "select_address"
                    )
                },

                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF2E7D32)
                    )

            ) {

                Text(
                    text = "📍 My Addresses"
                )
            }
            Spacer(
                modifier = Modifier.height(16.dp)
            )


            Button(

                onClick = {

                    val intent = Intent(Intent.ACTION_SEND).apply {

                        type = "text/plain"

                        putExtra(

                            Intent.EXTRA_TEXT,

                            "🍃 Order delicious Pure Veg food with VeggieGo!\n\n" +

                                    "✅ Pure Veg Restaurants\n" +

                                    "🛵 Fast Delivery\n" +

                                    "📍 Gandhidham & Adipur\n\n" +

                                    "Download Now:\n" +

                                    "https://play.google.com/store/apps/details?id=com.veggiego.customer"

                        )

                    }

                    context.startActivity(

                        Intent.createChooser(

                            intent,

                            "Share VeggieGo"

                        )

                    )

                },

                modifier = Modifier.fillMaxWidth(),

                colors = ButtonDefaults.buttonColors(

                    containerColor = Color(0xFF1976D2)

                )

            ) {

                Text(

                    "📤 Share VeggieGo"

                )

            }
            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Button(

                onClick = {

                    val emailIntent = Intent(

                        Intent.ACTION_SENDTO

                    ).apply {

                        data = Uri.parse(

                            "mailto:support@veggiego.co.in"

                        )

                        putExtra(

                            Intent.EXTRA_SUBJECT,

                            "VeggieGo Customer Support"

                        )

                    }

                    context.startActivity(emailIntent)

                },

                modifier = Modifier.fillMaxWidth(),

                colors = ButtonDefaults.buttonColors(

                    containerColor = Color(0xFF00897B)

                )

            ) {

                Text(

                    "📧 Contact Support"

                )

            }
            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Button(

                onClick = {

                    try {

                        context.startActivity(

                            Intent(

                                Intent.ACTION_VIEW,

                                Uri.parse(

                                    "market://details?id=com.veggiego.customer"

                                )

                            )

                        )

                    } catch (e: Exception) {

                        context.startActivity(

                            Intent(

                                Intent.ACTION_VIEW,

                                Uri.parse(

                                    "https://play.google.com/store/apps/details?id=com.veggiego.customer"

                                )

                            )

                        )

                    }

                },

                modifier = Modifier.fillMaxWidth(),

                colors = ButtonDefaults.buttonColors(

                    containerColor = Color(0xFF34A853)

                )

            ) {

                Text(

                    "⭐ Rate VeggieGo"

                )

            }
            Spacer(
                modifier = Modifier.height(16.dp)
            )
            Button(

                onClick = {

                    navController.navigate(
                        "favorites"
                    )
                },

                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFFE91E63)
                    )

            ) {

                Text(
                    text = "❤️ Favorites"
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // ✅ LOGOUT
            Button(

                onClick = {

                    FirebaseAuth
                        .getInstance()
                        .signOut()

                    navController.navigate("login") {

                        popUpTo(0)
                    }
                },

                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )

            ) {

                Text(
                    text = "Logout"
                )

            }
            Spacer(
                modifier = Modifier.height(16.dp)
            )



            Button(

                onClick = {

                    showDeleteDialog = true

                },

                modifier =
                    Modifier.fillMaxWidth(),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFFD32F2F)
                    )

            ) {

                Text(
                    "🗑 Delete Account"
                )
            }
        }
        if (showDeleteDialog) {

            AlertDialog(

                onDismissRequest = {
                    showDeleteDialog = false
                },

                title = {
                    Text("Delete Account")
                },

                text = {
                    Text(
                        "Are you sure you want to delete your account?"
                    )
                },

                confirmButton = {

                    TextButton(

                        onClick = {

                            showDeleteDialog = false

                            val uid =
                                FirebaseAuth
                                    .getInstance()
                                    .currentUser
                                    ?.uid ?: ""

                            if (uid.isEmpty())
                                return@TextButton

                            FirebaseFirestore
                                .getInstance()
                                .collection("users")
                                .document(uid)
                                .delete()

                                .addOnSuccessListener {

                                    FirebaseAuth
                                        .getInstance()
                                        .signOut()

                                    Toast.makeText(
                                        context,
                                        "Account Deleted",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate("login") {

                                        popUpTo(0)

                                    }

                                }

                        }

                    ) {

                        Text("Yes")

                    }

                },

                dismissButton = {

                    TextButton(

                        onClick = {

                            showDeleteDialog = false

                        }

                    ) {

                        Text("No")

                    }

                }

            )

        }
    }
}
