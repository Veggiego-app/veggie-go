package com.veggiego.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
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
import android.location.Location

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(

    navController: NavController,

    from: String = "home"

) {

    val db =
        FirebaseFirestore.getInstance()

    val userId =
        FirebaseAuth
            .getInstance()
            .currentUser
            ?.uid ?: ""

    var addresses by remember {

        mutableStateOf<List<Address>>(
            emptyList()
        )
    }

    var loading by remember {

        mutableStateOf(true)
    }

    LaunchedEffect(Unit) {

        if (userId.isNotEmpty()) {

            db.collection("users")
                .document(userId)
                .collection("addresses")
                .get()

                .addOnSuccessListener { result ->

                    addresses =
                        result.toObjects(
                            Address::class.java
                        )

                    if (

                        AddressData
                            .selectedAddress
                            .value == null &&

                        addresses.isNotEmpty()

                    ) {

                        AddressData
                            .selectedAddress
                            .value =

                            addresses.first()
                    }

                    loading = false
                }

        } else {

            loading = false
        }
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        "My Addresses"
                    )
                },

                colors =

                    TopAppBarDefaults
                        .topAppBarColors(

                            containerColor =
                                Color.White
                        )
            )
        },

        bottomBar = {

            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {

                Button(

                    onClick = {

                        AddressData.mapSelected = false

                        AddressData.selectedArea = ""
                        AddressData.selectedCity = ""
                        AddressData.selectedPincode = ""
                        AddressData.selectedLatitude = 0.0
                        AddressData.selectedLongitude = 0.0

                        navController.navigate(
                            "map_picker?openForm=true"
                        )

                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),

                    shape = RoundedCornerShape(16.dp),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32)
                        )

                ) {

                    Text(
                        text = "➕ Add New Address",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Color(0xFFF7F7F7)
                )

        ) {

            if (loading) {

                Box(

                    modifier =
                        Modifier.fillMaxSize(),

                    contentAlignment =
                        Alignment.Center

                ) {

                    CircularProgressIndicator(

                        color =
                            Color(0xFF2E7D32)
                    )
                }

            } else if (

                addresses.isEmpty()

            ) {

                Box(

                    modifier =
                        Modifier.fillMaxSize(),

                    contentAlignment =
                        Alignment.Center

                ) {

                    Text(

                        "No saved addresses found",

                        color =
                            Color.Gray
                    )
                }

            } else {

                LazyColumn(

                    modifier =
                        Modifier.fillMaxSize(),

                    contentPadding =
                        PaddingValues(16.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)

                ) {

                    items(addresses) { address ->

                        AddressItem(

                            address = address,

                            isSelected =
                                AddressData.selectedAddress.value?.id == address.id,

                            onClick = {

                                AddressData.selectedAddress.value = address

                                CartData.selectedAddress =
                                    "${address.house}, ${address.area}"

// ✅ Cart empty hai to restaurant distance calculate mat karo
                                if (CartData.currentRestaurantId.value.isBlank()) {

                                    CartData.selectedDistanceKm = 0.0

                                    if (from == "cart") {

                                        navController.popBackStack()

                                    } else {

                                        navController.navigate("home") {

                                            popUpTo("home") {
                                                inclusive = false
                                            }

                                            launchSingleTop = true
                                        }
                                    }

                                    return@AddressItem
                                }

                                db.collection("restaurants")
                                    .document(CartData.currentRestaurantId.value)
                                    .get()

                                    .addOnSuccessListener { restaurantDoc ->

                                        val restaurantLat =
                                            restaurantDoc.getDouble("lat") ?: 0.0

                                        val restaurantLng =
                                            restaurantDoc.getDouble("lng") ?: 0.0

                                        val result = FloatArray(1)

                                        Location.distanceBetween(

                                            restaurantLat,
                                            restaurantLng,

                                            address.latitude,
                                            address.longitude,

                                            result

                                        )

                                        CartData.selectedDistanceKm =
                                            result[0] / 1000.0

                                        if (from == "cart") {

                                            navController.popBackStack()

                                        } else {

                                            navController.navigate("home") {

                                                popUpTo("home") {
                                                    inclusive = false
                                                }

                                                launchSingleTop = true
                                            }
                                        }

                                    }

                                    .addOnFailureListener {

                                        CartData.selectedDistanceKm = 0.0

                                        if (from == "cart") {

                                            navController.popBackStack()

                                        } else {

                                            navController.navigate("home") {

                                                popUpTo("home") {
                                                    inclusive = false
                                                }

                                                launchSingleTop = true
                                            }
                                        }
                                    }

                            },

                            onEdit = {

                                navController.navigate(

                                    "add_address?addressId=${address.id}"
                                )
                            },

                            onDelete = {

                                db.collection("users")
                                    .document(userId)
                                    .collection("addresses")
                                    .document(address.id)
                                    .delete()

                                val remaining = addresses.filter {

                                    it.id != address.id

                                }

                                addresses = remaining

                                if (AddressData.selectedAddress.value?.id == address.id) {

                                    if (remaining.isEmpty()) {

                                        AddressData.selectedAddress.value = null

                                        CartData.selectedAddress = ""

                                        CartData.selectedDistanceKm = 0.0

                                        navController.navigate("select_address") {

                                            popUpTo("home") {
                                                inclusive = false
                                            }

                                            launchSingleTop = true
                                        }

                                    } else {

                                        AddressData.selectedAddress.value =
                                            remaining.first()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddressItem(

    address: Address,

    isSelected: Boolean,

    onClick: () -> Unit,

    onEdit: () -> Unit,

    onDelete: () -> Unit

) {

    var showDeleteDialog by remember {

        mutableStateOf(false)
    }

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),

        shape =
            RoundedCornerShape(16.dp),

        colors =

            CardDefaults.cardColors(

                containerColor =

                    if (isSelected) {

                        Color(0xFFF1F8F2)

                    } else {

                        Color.White
                    }
            ),

        elevation =

            CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )

    ) {

        Column(

            modifier = Modifier
                .fillMaxWidth()
                .clickable {

                    onClick()
                }
                .padding(16.dp)

        ) {

            if (isSelected) {

                Surface(

                    color =
                        Color(0xFFE0F2E2),

                    shape =
                        RoundedCornerShape(50.dp)

                ) {

                    Text(

                        text =
                            "🟢 CURRENT DELIVERY ADDRESS",

                        color =
                            Color(0xFF1B5E20),

                        fontSize =
                            12.sp,

                        fontWeight =
                            FontWeight.Bold,

                        modifier =
                            Modifier.padding(

                                horizontal = 12.dp,

                                vertical = 7.dp
                            )
                    )
                }

                Spacer(

                    modifier =
                        Modifier.height(14.dp)
                )
            }

            Row(

                verticalAlignment =
                    Alignment.CenterVertically

            ) {

                Icon(

                    Icons.Default.LocationOn,

                    contentDescription = null,

                    tint =
                        Color(0xFF2E7D32)
                )

                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )

                Text(

                    text =
                        address.fullName,

                    fontSize = 18.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(

                text =

                    "${address.house}, ${address.area}, ${address.city}",

                color =
                    Color.Gray
            )

            if (

                address.landmark
                    .isNotEmpty()

            ) {

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                Text(

                    text =
                        "Landmark: ${address.landmark}",

                    color =
                        Color.LightGray,

                    fontSize = 12.sp
                )
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            if (!isSelected) {

                OutlinedButton(

                    onClick = {

                        onClick()
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(12.dp),

                    colors =
                        ButtonDefaults.outlinedButtonColors(

                            contentColor =
                                Color(0xFF2E7D32)
                        )

                ) {

                    Text(

                        text =
                            "Use This Address",

                        fontWeight =
                            FontWeight.Bold
                    )
                }

                Spacer(

                    modifier =
                        Modifier.height(6.dp)
                )
            }

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.End

            ) {

                TextButton(

                    onClick = {

                        onEdit()
                    }

                ) {

                    Text(
                        text =
                            "✏ Edit"
                    )
                }

                TextButton(

                    onClick = {

                        showDeleteDialog =
                            true
                    }

                ) {

                    Text(

                        text =
                            "🗑 Delete",

                        color =
                            Color.Red
                    )
                }
            }
        }
    }

    if (

        showDeleteDialog

    ) {

        AlertDialog(

            onDismissRequest = {

                showDeleteDialog =
                    false
            },

            title = {

                Text(
                    text =
                        "Delete Address?"
                )
            },

            text = {

                Text(
                    text =
                        "Are you sure you want to delete this address?"
                )
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        showDeleteDialog =
                            false

                        onDelete()
                    }

                ) {

                    Text(
                        text =
                            "YES"
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {

                        showDeleteDialog =
                            false
                    }

                ) {

                    Text(
                        text =
                            "NO"
                    )
                }
            }
        )
    }
}