package com.veggiego.customer

import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.MyLocation
import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.LocationServices
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import com.google.android.libraries.places.api.Places

import com.google.android.libraries.places.api.model.Place

import com.google.android.libraries.places.api.model.AutocompleteSessionToken

import com.google.android.libraries.places.api.net.PlacesClient

import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.gms.maps.model.LatLngBounds

import com.google.android.libraries.places.api.net.FetchPlaceRequest
import androidx.compose.foundation.layout.navigationBarsPadding
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest

@Composable
fun MapPickerScreen(

    navController: NavController

) {

    val context =
        LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val focusManager = LocalFocusManager.current

    val keyboardController =
        LocalSoftwareKeyboardController.current

    if (!Places.isInitialized()) {

        Places.initialize(
            context,
            "AIzaSyBZxLPwh3xhYkpL1y7rk4iCGrz1Rxf6H2k"
        )

    }

    val placesClient = remember {

        Places.createClient(context)

    }
    val fusedLocationClient =

        LocationServices
            .getFusedLocationProviderClient(
                context
            )

    var selectedLocation by remember {

        mutableStateOf(

            if (
                AddressData.selectedLatitude != 0.0 &&
                AddressData.selectedLongitude != 0.0
            ) {

                LatLng(
                    AddressData.selectedLatitude,
                    AddressData.selectedLongitude
                )

            } else {

                LatLng(
                    23.0753,
                    70.1337
                )

            }

        )

    }

    var addressText by remember {

        mutableStateOf(
            "Move map to select location"
        )
    }
    var searchText by remember {
        mutableStateOf("")
    }
    var predictions by remember {

        mutableStateOf(

            emptyList<com.google.android.libraries.places.api.model.AutocompletePrediction>()

        )

    }

    val sessionToken = remember {

        AutocompleteSessionToken.newInstance()

    }

    val cameraPositionState =

        rememberCameraPositionState {

            position =

                CameraPosition.fromLatLngZoom(

                    selectedLocation,

                    16f
                )
        }
    LaunchedEffect(searchText) {

        if (searchText.length < 2) {

            predictions = emptyList()

            return@LaunchedEffect

        }

        val gandhidhamCenter =

            LatLng(
                23.0753,
                70.1337
            )

        val bounds =

            LatLngBounds(

                LatLng(
                    gandhidhamCenter.latitude - 0.18,
                    gandhidhamCenter.longitude - 0.18
                ),

                LatLng(
                    gandhidhamCenter.latitude + 0.18,
                    gandhidhamCenter.longitude + 0.18
                )

            )

        val request =

            FindAutocompletePredictionsRequest
                .builder()

                .setSessionToken(sessionToken)

                .setQuery(searchText)

                .setCountries("IN")

                .setLocationBias(
                    com.google.android.libraries.places.api.model.RectangularBounds
                        .newInstance(bounds)
                )

                .build()

        placesClient
            .findAutocompletePredictions(request)
            .addOnSuccessListener {

                android.util.Log.d(
                    "PLACES",
                    "Success : ${it.autocompletePredictions.size}"
                )

                predictions = it.autocompletePredictions
            }

            .addOnFailureListener { e ->

                android.util.Log.e(
                    "PLACES",
                    "Error",
                    e
                )

                predictions = emptyList()
            }

    }

    LaunchedEffect(
        cameraPositionState.isMoving
    ) {

        if (

            !cameraPositionState.isMoving

        ) {

            selectedLocation =

                cameraPositionState.position.target

            try {

                val geocoder =

                    Geocoder(
                        context,
                        Locale.getDefault()
                    )

                val addresses =

                    geocoder.getFromLocation(

                        selectedLocation.latitude,

                        selectedLocation.longitude,

                        1
                    )

                if (

                    !addresses.isNullOrEmpty()

                ) {

                    val address =
                        addresses[0]

                    val areaText =

                        address.subLocality
                            ?: address.thoroughfare
                            ?: address.featureName
                            ?: address.subAdminArea
                            ?: address.locality
                            ?: ""

                    val cityText =

                        address.locality
                            ?: address.subAdminArea
                            ?: address.adminArea
                            ?: ""

                    addressText =

                        if (cityText.isBlank()) {

                            areaText

                        } else {

                            "$areaText, $cityText"

                        }
                }

            } catch (
                e: Exception
            ) {

                e.printStackTrace()
            }
        }
    }
    @SuppressLint("MissingPermission")
    fun moveToCurrentLocation() {

        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient
            .getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )
            .addOnSuccessListener { location: Location? ->

                if (location != null) {

                    val currentLatLng = LatLng(
                        location.latitude,
                        location.longitude
                    )

                    selectedLocation = currentLatLng

                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(
                            currentLatLng,
                            18f
                        )
                    )
                }
            }
            .addOnFailureListener {

                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->

                        location ?: return@addOnSuccessListener

                        val currentLatLng = LatLng(
                            location.latitude,
                            location.longitude
                        )

                        selectedLocation = currentLatLng

                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(
                                currentLatLng,
                                18f
                            )
                        )
                    }
            }
    }
    val gpsLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                moveToCurrentLocation()

            } else {

                Toast.makeText(
                    context,
                    "Please turn on location to use current location.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    fun checkGpsAndMove() {

        val locationRequest =
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000L
            ).build()

        val settingsRequest =
            LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build()

        LocationServices
            .getSettingsClient(context)
            .checkLocationSettings(settingsRequest)
            .addOnSuccessListener {

                moveToCurrentLocation()

            }
            .addOnFailureListener { e ->

                if (e is ResolvableApiException) {

                    val intentSenderRequest =
                        IntentSenderRequest.Builder(
                            e.resolution
                        ).build()

                    gpsLauncher.launch(intentSenderRequest)

                } else {

                    Toast.makeText(
                        context,
                        "Please turn on location from phone settings.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            hasLocationPermission = granted

            if (granted) {

                checkGpsAndMove()

            } else {

                Toast.makeText(
                    context,
                    "Please allow location permission from app settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {

        // ✅ GOOGLE MAP

        GoogleMap(

            modifier = Modifier.fillMaxSize(),

            cameraPositionState = cameraPositionState,

            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            ),

            uiSettings = MapUiSettings(

                myLocationButtonEnabled = false

            )

        )
        FloatingActionButton(

            onClick = {

                if (hasLocationPermission) {

                    checkGpsAndMove()

                } else {

                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },

            modifier =
                Modifier
                    .padding(

                        end = 16.dp,

                        bottom = 220.dp
                    )
                    .align(
                        Alignment.BottomEnd
                    )

        ) {

            Icon(

                imageVector =
                    Icons.Default.MyLocation,

                contentDescription =
                    null
            )
        }
        // ✅ CENTER PIN

        Box(

            modifier =
                Modifier.fillMaxSize(),

            contentAlignment =
                Alignment.Center

        ) {

            Icon(

                imageVector =
                    Icons.Default.LocationOn,

                contentDescription = null,

                tint =
                    Color.Red,

                modifier =
                    Modifier.size(42.dp)
            )
        }

        // ✅ TOP SEARCH STYLE CARD

        Card(

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 50.dp
                )
                .shadow(
                    8.dp,
                    RoundedCornerShape(18.dp)
                ),

            shape =
                RoundedCornerShape(18.dp),

            colors =
                CardDefaults.cardColors(
                    containerColor =
                        Color.White
                )

        ) {

            Column(

                modifier =
                    Modifier.padding(16.dp)

            ) {

                OutlinedTextField(

                    value = searchText,

                    onValueChange = {
                        searchText = it
                    },

                    modifier = Modifier.fillMaxWidth(),

                    placeholder = {
                        Text("Search address")
                    },

                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null
                        )
                    },

                    trailingIcon = {

                        if (searchText.isNotBlank()) {

                            IconButton(

                                onClick = {

                                    searchText = ""
                                    predictions = emptyList()

                                }

                            ) {

                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear"
                                )

                            }

                        }

                    },

                    singleLine = true
                )
                if (predictions.isNotEmpty()) {

                    LazyColumn(

                        modifier =
                            Modifier.height(220.dp)

                    ) {

                        items(predictions) { prediction ->

                            Text(

                                text =
                                    prediction
                                        .getFullText(null)
                                        .toString(),

                                modifier =
                                    Modifier

                                        .fillMaxWidth()

                                        .clickable {

                                            predictions = emptyList()

                                            searchText = ""

                                            focusManager.clearFocus()

                                            keyboardController?.hide()

                                            val placeRequest =

                                                FetchPlaceRequest.newInstance(

                                                    prediction.placeId,

                                                    listOf(

                                                        Place.Field.LAT_LNG,

                                                        Place.Field.ADDRESS

                                                    )

                                                )

                                            placesClient

                                                .fetchPlace(placeRequest)

                                                .addOnSuccessListener {

                                                    val latLng =

                                                        it.place.latLng
                                                            ?: return@addOnSuccessListener

                                                    selectedLocation =
                                                        latLng

                                                    cameraPositionState.move(

                                                        CameraUpdateFactory

                                                            .newLatLngZoom(

                                                                latLng,

                                                                17f

                                                            )

                                                    )

                                                }

                                        }

                                        .padding(16.dp)

                            )

                            Divider()

                        }

                    }

                }
            }
        }

        // ✅ BOTTOM CONFIRM CARD

        Card(

            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),

            shape =
                RoundedCornerShape(24.dp),

            colors =
                CardDefaults.cardColors(
                    containerColor =
                        Color.White
                ),

            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                )

        ) {

            Column(

                modifier =
                    Modifier.padding(18.dp)

            ) {

                Text(

                    text =
                        "Delivering Here",

                    fontWeight =
                        FontWeight.Bold,

                    fontSize = 18.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(

                    text = addressText,

                    color =
                        Color.Gray
                )

                Spacer(
                    modifier =
                        Modifier.height(18.dp)
                )

                Button(

                    onClick = {

                        AddressData.selectedLatitude =

                            selectedLocation.latitude

                        AddressData.selectedLongitude =

                            selectedLocation.longitude

                        val geocoder =

                            Geocoder(
                                context,
                                Locale.getDefault()
                            )

                        val addresses =

                            geocoder.getFromLocation(

                                selectedLocation.latitude,

                                selectedLocation.longitude,

                                1
                            )

                        if (

                            !addresses.isNullOrEmpty()

                        ) {

                            val address =
                                addresses[0]

                            AddressData.selectedArea =

                                when {

                                    !address.subLocality.isNullOrBlank() ->
                                        address.subLocality!!

                                    !address.thoroughfare.isNullOrBlank() ->
                                        address.thoroughfare!!

                                    !address.featureName.isNullOrBlank() ->
                                        address.featureName!!

                                    !address.subAdminArea.isNullOrBlank() ->
                                        address.subAdminArea!!

                                    !address.locality.isNullOrBlank() ->
                                        address.locality!!

                                    else ->
                                        ""

                                }

                            AddressData.selectedCity =

                                when {

                                    !address.locality.isNullOrBlank() ->
                                        address.locality!!

                                    !address.subAdminArea.isNullOrBlank() ->
                                        address.subAdminArea!!

                                    !address.adminArea.isNullOrBlank() ->
                                        address.adminArea!!

                                    else ->
                                        ""

                                }

                            AddressData.selectedPincode =

                                address.postalCode ?: ""
                            if (AddressData.selectedArea.isBlank()) {

                                AddressData.selectedArea =

                                    address.getAddressLine(0)
                                        ?.split(",")
                                        ?.firstOrNull()
                                        ?: "Unknown Area"

                            }

                            if (AddressData.selectedCity.isBlank()) {

                                AddressData.selectedCity =

                                    "Unknown City"

                            }
                        }
                        AddressData.mapSelected = true

                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("map_area", AddressData.selectedArea)

                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("map_city", AddressData.selectedCity)

                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("map_pincode", AddressData.selectedPincode)

                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("map_latitude", AddressData.selectedLatitude)

                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("map_longitude", AddressData.selectedLongitude)

                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("map_picked", true)

                        navController.popBackStack()
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(16.dp)

                ) {

                    Text(
                        text =
                            "Confirm Location"
                    )
                }
            }
        }
    }
}