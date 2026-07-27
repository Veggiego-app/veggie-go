package com.veggiego.customer

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.google.firebase.firestore.SetOptions
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import com.google.firebase.firestore.FieldValue

@Composable
fun AddAddressScreen(
    navController: NavController,
    addressId: String = ""
) {

    val db =
        FirebaseFirestore.getInstance()

    val currentUser =
        FirebaseAuth
            .getInstance()
            .currentUser

    val context =
        LocalContext.current

    if (currentUser == null) {

        Box(

            modifier =
                Modifier.fillMaxSize(),

            contentAlignment =
                Alignment.Center

        ) {

            Text(
                text =
                    "Please wait..."
            )
        }

        return
    }

    val userId =
        currentUser.uid

    var latitude by remember {
        mutableStateOf(
            if (AddressData.mapSelected) {
                AddressData.selectedLatitude
            } else {
                0.0
            }
        )
    }

    var longitude by remember {
        mutableStateOf(
            if (AddressData.mapSelected) {
                AddressData.selectedLongitude
            } else {
                0.0
            }
        )
    }

    var fullName by remember {

        mutableStateOf(

            currentUser.displayName
                ?: "Customer"
        )
    }

    var phone by remember {

        mutableStateOf(

            currentUser.phoneNumber
                ?: ""
        )
    }

    var house by remember {
        mutableStateOf("")
    }

    var area by remember {
        mutableStateOf(
            if (AddressData.mapSelected) {
                AddressData.selectedArea
            } else {
                ""
            }
        )
    }

    var city by remember {
        mutableStateOf(
            if (AddressData.mapSelected) {
                AddressData.selectedCity
            } else {
                ""
            }
        )
    }

    var pincode by remember {
        mutableStateOf(
            if (AddressData.mapSelected) {
                AddressData.selectedPincode
            } else {
                ""
            }
        )
    }

    var originalArea by remember { mutableStateOf("") }
    var originalCity by remember { mutableStateOf("") }
    var originalPincode by remember { mutableStateOf("") }
    var originalLatitude by remember { mutableStateOf(0.0) }
    var originalLongitude by remember { mutableStateOf(0.0) }

    var landmark by remember {
        mutableStateOf("")
    }
    val isEditMode = addressId.isNotEmpty()

    BackHandler {

        if (isEditMode) {

            AddressData.selectedArea = originalArea
            AddressData.selectedCity = originalCity
            AddressData.selectedPincode = originalPincode
            AddressData.selectedLatitude = originalLatitude
            AddressData.selectedLongitude = originalLongitude

            area = originalArea
            city = originalCity
            pincode = originalPincode
            latitude = originalLatitude
            longitude = originalLongitude

        } else {

            AddressData.mapSelected = false
            AddressData.selectedArea = ""
            AddressData.selectedCity = ""
            AddressData.selectedPincode = ""
            AddressData.selectedLatitude = 0.0
            AddressData.selectedLongitude = 0.0
        }

        AddressData.mapResultReady = false
        val wentBack =
            navController.popBackStack(
                route = "select_address",
                inclusive = false
            )

        if (!wentBack) {
            navController.navigate("select_address") {
                launchSingleTop = true
            }
        }
    }

    var mapSelected by remember {
        mutableStateOf(
            isEditMode || AddressData.mapSelected
        )
    }

    // ✅ MAP DATA LIVE OBSERVER

    LaunchedEffect(
        AddressData.mapSelected,
        AddressData.selectedLatitude,
        AddressData.selectedLongitude,
        AddressData.selectedArea,
        AddressData.selectedCity,
        AddressData.selectedPincode
    ) {

        if (!AddressData.mapSelected) {
            return@LaunchedEffect
        }

        latitude =
            AddressData.selectedLatitude

        longitude =
            AddressData.selectedLongitude

        area =
            AddressData.selectedArea

        city =
            AddressData.selectedCity

        pincode =
            AddressData.selectedPincode

        mapSelected =
            true
    }

    // ✅ EDIT ADDRESS

    LaunchedEffect(addressId) {

        if (addressId.isNotEmpty()) {

            db.collection("users")
                .document(userId)
                .collection("addresses")
                .document(addressId)
                .get()

                .addOnSuccessListener {

                    val address =
                        it.toObject(
                            Address::class.java
                        )

                    if (address != null) {

                        fullName =
                            address.fullName

                        phone =
                            address.phone

                        house =
                            address.house

                        // Original saved location back button ke liye
                        originalArea =
                            address.area

                        originalCity =
                            address.city

                        originalPincode =
                            address.pincode

                        originalLatitude =
                            address.latitude

                        originalLongitude =
                            address.longitude

                        landmark =
                            address.landmark

                        /*
                         Map se fresh result aaya hai to Firestore ki
                         purani location se overwrite nahi karna.
                        */
                        if (AddressData.mapResultReady) {

                            area =
                                AddressData.selectedArea

                            city =
                                AddressData.selectedCity

                            pincode =
                                AddressData.selectedPincode

                            latitude =
                                AddressData.selectedLatitude

                            longitude =
                                AddressData.selectedLongitude

                        } else {

                            area =
                                address.area

                            city =
                                address.city

                            pincode =
                                address.pincode

                            latitude =
                                address.latitude

                            longitude =
                                address.longitude

                            AddressData.selectedArea =
                                address.area

                            AddressData.selectedCity =
                                address.city

                            AddressData.selectedPincode =
                                address.pincode

                            AddressData.selectedLatitude =
                                address.latitude

                            AddressData.selectedLongitude =
                                address.longitude
                        }

                        AddressData.mapSelected =
                            true

                        mapSelected =
                            true
                    }
                }
        }
    }

    Column(

        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
            .verticalScroll(
                rememberScrollState()
            )

    ) {

        // ✅ MAP BUTTON

        OutlinedButton(
            onClick = {

                // Current form ki location map ko do

                AddressData.selectedLatitude =
                    latitude

                AddressData.selectedLongitude =
                    longitude

                AddressData.selectedArea =
                    area

                AddressData.selectedCity =
                    city

                AddressData.selectedPincode =
                    pincode

                AddressData.mapSelected =
                    latitude != 0.0 &&
                            longitude != 0.0

                AddressData.mapResultReady = false

                navController.navigate(
                    "map_picker?openForm=false"
                )
            },

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(18.dp)

        ) {

            Text(

                when {

                    !mapSelected ->
                        "🗺 Select Delivery Location"

                    else ->
                        "✅ Delivery Location Selected — Change"
                }

            )
        }

        Spacer(
            modifier =
                Modifier.height(18.dp)
        )
        if (!mapSelected) {

            Text(

                text = "Please select your delivery location on map first.",

                color = MaterialTheme.colorScheme.primary,

                style = MaterialTheme.typography.bodyMedium,

                modifier = Modifier.padding(bottom = 12.dp)

            )
        }
// ✅ FULL NAME

        OutlinedTextField(

            value = fullName,

            onValueChange = {

                fullName = it.filter { ch ->

                    ch.isLetter() || ch.isWhitespace()

                }

            },

            label = {
                Text("Full Name")
            },

            enabled = mapSelected,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

// ✅ PHONE

        OutlinedTextField(

            value = phone,

            onValueChange = {

                phone = it

                    .filter { ch ->

                        ch.isDigit()

                    }

                    .take(10)

            },

            label = {
                Text("Phone Number")
            },
            enabled = mapSelected,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )
        // ✅ HOUSE

        OutlinedTextField(

            value = house,

            onValueChange = {
                house = it
            },

            label = {
                Text("House / Flat")
            },
            enabled = mapSelected,

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        // ✅ AREA

        // ✅ AREA - MAP SE AUTO

        OutlinedTextField(
            value = area,

            onValueChange = {},

            readOnly = true,

            label = {
                Text("Area / Road")
            },

            supportingText = {
                Text(
                    "Selected map location se automatically detected"
                )
            },

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        // ✅ LANDMARK

        OutlinedTextField(

            value = landmark,

            onValueChange = {
                landmark = it
            },

            label = {
                Text("Landmark")
            },

            enabled = mapSelected,

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        // ✅ CITY

        // ✅ CITY - MAP SE AUTO

        OutlinedTextField(
            value = city,

            onValueChange = {},

            readOnly = true,

            label = {
                Text("City")
            },

            supportingText = {
                Text(
                    "Selected map location se automatically detected"
                )
            },

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        // ✅ PINCODE

        // ✅ PINCODE - MAP SE AUTO

        OutlinedTextField(
            value = pincode,

            onValueChange = {},

            readOnly = true,

            label = {
                Text("Pincode")
            },

            supportingText = {
                Text(
                    "Selected map location se automatically detected"
                )
            },

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        // ✅ SAVE BUTTON

        Button(

            onClick = {

                // ✅ MAP REQUIRED

                if (!mapSelected) {

                    Toast
                        .makeText(

                            context,

                            "Please select map first",

                            Toast.LENGTH_SHORT
                        )
                        .show()

                    return@Button
                }
                // ✅ FULL NAME REQUIRED

                if (

                    fullName.isBlank()

                ) {

                    Toast
                        .makeText(

                            context,

                            "Please enter full name",

                            Toast.LENGTH_SHORT

                        )
                        .show()

                    return@Button
                }
                // ✅ HOUSE REQUIRED

                if (

                    house.isEmpty()

                ) {

                    Toast
                        .makeText(

                            context,

                            "Please enter house / flat",

                            Toast.LENGTH_SHORT
                        )
                        .show()

                    return@Button
                }
                // ✅ PHONE REQUIRED

                if (

                    phone.isBlank()

                ) {

                    Toast
                        .makeText(

                            context,

                            "Please enter mobile number",

                            Toast.LENGTH_SHORT

                        )
                        .show()

                    return@Button
                }

                // ✅ PHONE VALIDATION

                val cleanPhone =

                    phone.replace("+91", "")
                        .replace(" ", "")

                if (

                    !cleanPhone.matches(
                        Regex("^[6-9]\\d{9}$")
                    )

                ) {

                    Toast
                        .makeText(

                            context,

                            "Enter valid mobile number",

                            Toast.LENGTH_SHORT
                        )
                        .show()

                    return@Button
                }

                val address =

                    Address(

                        id = addressId,

                        fullName = fullName,

                        phone = phone,

                        house = house,

                        area = area,

                        city = city,

                        pincode = pincode,

                        landmark = landmark,

                        latitude = latitude,

                        longitude = longitude
                    )

                val docId =

                    if (addressId.isEmpty()) {

                        db.collection("users")
                            .document(userId)
                            .collection("addresses")
                            .document()
                            .id

                    } else {

                        addressId
                    }

                db.collection("users")
                    .document(userId)
                    .collection("addresses")
                    .document(docId)

                    .set(
                        address.copy(
                            id = docId
                        )
                    )

                    .addOnSuccessListener {

                        // ✅ Update User Profile + Customer Code

                        val userRef =
                            db.collection("users")
                                .document(userId)

                        val counterRef =
                            db.collection("settings")
                                .document("counters")

                        userRef.get()
                            .addOnSuccessListener { userDoc ->

                                if (
                                    userDoc.exists() &&
                                    !userDoc.getString("customerCode").isNullOrBlank()
                                ) {

                                    userRef.set(
                                        mapOf(
                                            "fullName" to fullName,
                                            "name" to fullName,
                                            "phone" to phone,
                                            "updatedAt" to FieldValue.serverTimestamp(),
                                            "status" to (userDoc.getString("status") ?: "ACTIVE")
                                        ),
                                        SetOptions.merge()
                                    )
                                        .addOnSuccessListener {

                                            AddressData.mapSelected = false
                                            AddressData.mapResultReady = false

                                            AddressData.selectedArea = ""
                                            AddressData.selectedCity = ""
                                            AddressData.selectedPincode = ""
                                            AddressData.selectedLatitude = 0.0
                                            AddressData.selectedLongitude = 0.0

                                            mapSelected = false

                                            navController.popBackStack()
                                        }
                                        .addOnFailureListener {

                                            Toast.makeText(
                                                context,
                                                "Profile update failed",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }

                                } else {

                                    db.runTransaction { transaction ->

                                        val counterDoc =
                                            transaction.get(counterRef)

                                        val lastNumber =
                                            counterDoc.getLong("lastCustomerNumber") ?: 0L

                                        val nextNumber =
                                            lastNumber + 1L

                                        val code =
                                            "VGC" + nextNumber
                                                .toString()
                                                .padStart(6, '0')

                                        transaction.set(
                                            counterRef,
                                            mapOf(
                                                "lastCustomerNumber" to nextNumber
                                            ),
                                            SetOptions.merge()
                                        )

                                        transaction.set(
                                            userRef,
                                            mapOf(
                                                "fullName" to fullName,
                                                "name" to fullName,
                                                "phone" to phone,
                                                "customerCode" to code,
                                                "createdAt" to FieldValue.serverTimestamp(),
                                                "updatedAt" to FieldValue.serverTimestamp(),
                                                "status" to "ACTIVE"
                                            ),
                                            SetOptions.merge()
                                        )

                                        code
                                    }
                                        .addOnSuccessListener {

                                            AddressData.mapSelected = false
                                            AddressData.mapResultReady = false

                                            AddressData.selectedArea = ""
                                            AddressData.selectedCity = ""
                                            AddressData.selectedPincode = ""
                                            AddressData.selectedLatitude = 0.0
                                            AddressData.selectedLongitude = 0.0

                                            mapSelected = false

                                            navController.popBackStack()
                                        }
                                        .addOnFailureListener { e ->

                                            Toast.makeText(
                                                context,
                                                "Customer profile failed: ${e.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                }
                            }
                    }
            },

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(18.dp)

        ) {

            Text(

                if (
                    addressId.isEmpty()
                ) {
                    "Save Address"
                } else {
                    "Update Address"
                }
            )
        }
    }
}