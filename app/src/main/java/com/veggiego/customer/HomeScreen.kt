package com.veggiego.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.statusBarsPadding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Close
import android.location.Location
import androidx.activity.compose.BackHandler
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.navigationBarsPadding

data class SearchSuggestion(

    val type: String = "",

    val title: String = "",

    val restaurantId: String = "",

    val restaurantName: String = "",

    val restaurantLogo: String = "",

    val restaurantCount: Int = 0,

    val score: Int = 0
)

@Composable
fun HomeScreen(

    navController: NavController

) {

    var restaurantList by remember {

        mutableStateOf<List<RestaurantData>>(
            emptyList()
        )
    }

    var searchSuggestions by remember {

        mutableStateOf<List<SearchSuggestion>>(
            emptyList()
        )
    }
    var searchJob by remember {

        mutableStateOf<Job?>(null)
    }

    val scope = rememberCoroutineScope()

    var menuCache by remember {

        mutableStateOf<List<SearchSuggestion>>(

            emptyList()

        )
    }

    val restaurantPageSize = 20

    var visibleRestaurantCount by remember {
        mutableIntStateOf(restaurantPageSize)
    }

    var isSearchLoading by remember {
        mutableStateOf(false)
    }

    var menuCacheAddressKey by remember {
        mutableStateOf("")
    }

    var searchText by remember {

        mutableStateOf("")
    }

    var isLoading by remember {

        mutableStateOf(true)
    }

    // One shared minute ticker keeps weekly-slot status fresh without Firestore writes.
    var currentMinuteTick by remember {
        mutableLongStateOf(System.currentTimeMillis() / 60_000L)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            currentMinuteTick = System.currentTimeMillis() / 60_000L
        }
    }

    var selectedFilter by remember {

        mutableStateOf("All")
    }

    var isAddressChecked by remember {
        mutableStateOf(false)
    }

    val db =
        FirebaseFirestore.getInstance()

    var maintenanceMode by remember {
        mutableStateOf(false)
    }

    var maintenanceMessage by remember {
        mutableStateOf("")
    }

    BackHandler(

        enabled = searchText.isNotBlank()

    ) {

        searchText = ""

        searchSuggestions = emptyList()

    }

    fun calculateDistanceKm(

        startLat: Double,

        startLng: Double,

        endLat: Double,

        endLng: Double

    ): Double {

        val result = FloatArray(1)

        Location.distanceBetween(

            startLat,
            startLng,

            endLat,
            endLng,

            result

        )

        return result[0] / 1000.0

    }

    fun deliveryTimeFromDistance(

        km: Double

    ): String {

        return when {

            km <= 2 -> "15-20 min"

            km <= 5 -> "20-30 min"

            km <= 8 -> "30-40 min"

            km <= 12 -> "40-50 min"

            else -> "50-60 min"

        }

    }
    val userId =

        com.google.firebase.auth.FirebaseAuth
            .getInstance()
            .currentUser
            ?.uid ?: ""
    DisposableEffect(Unit) {

        val registration = db.collection("settings")
            .document("app")
            .addSnapshotListener { snapshot, _ ->

                if (snapshot == null) {
                    return@addSnapshotListener
                }

                maintenanceMode =
                    snapshot.getBoolean("maintenanceMode") ?: false

                maintenanceMessage =
                    snapshot.getString("maintenanceMessage") ?: ""
            }

        onDispose {
            registration.remove()
        }
    }

    LaunchedEffect(Unit) {

        if (
            userId.isEmpty() ||
            AddressData.selectedAddress.value != null
        ) {
            isAddressChecked = true
            return@LaunchedEffect
        }

        val userRef =
            db.collection("users")
                .document(userId)

        fun openAddressSelection() {

            isAddressChecked = true

            navController.navigate("select_address") {
                popUpTo("home") {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }

        fun useAddress(address: Address) {

            AddressData.selectedAddress.value = address

            CartData.selectedAddress =
                "${address.house}, ${address.area}"

            isAddressChecked = true
        }

        fun loadFirstAddress() {

            userRef.collection("addresses")
                .limit(1)
                .get()
                .addOnSuccessListener { result ->

                    val firstAddress =
                        result.documents
                            .firstOrNull()
                            ?.let { document ->

                                document
                                    .toObject(Address::class.java)
                                    ?.copy(id = document.id)
                            }

                    if (firstAddress == null) {

                        openAddressSelection()

                    } else {

                        useAddress(firstAddress)

                        userRef.set(
                            mapOf(
                                "selectedAddressId" to firstAddress.id
                            ),
                            SetOptions.merge()
                        )
                    }
                }
                .addOnFailureListener {

                    openAddressSelection()
                }
        }

        userRef.get()
            .addOnSuccessListener { userDocument ->

                val selectedAddressId =
                    userDocument
                        .getString("selectedAddressId")
                        .orEmpty()

                if (selectedAddressId.isBlank()) {

                    loadFirstAddress()

                } else {

                    userRef.collection("addresses")
                        .document(selectedAddressId)
                        .get()
                        .addOnSuccessListener { addressDocument ->

                            val savedAddress =
                                if (addressDocument.exists()) {

                                    addressDocument
                                        .toObject(Address::class.java)
                                        ?.copy(id = addressDocument.id)

                                } else {

                                    null
                                }

                            if (savedAddress == null) {

                                loadFirstAddress()

                            } else {

                                useAddress(savedAddress)
                            }
                        }
                        .addOnFailureListener {

                            loadFirstAddress()
                        }
                }
            }
            .addOnFailureListener {

                loadFirstAddress()
            }
    }
    // ✅ RESTAURANTS: realtime status stays unchanged, listener is cleaned up safely.
    DisposableEffect(Unit) {

        val registration = db.collection("restaurants")
            .whereEqualTo("status", "APPROVED")
            .addSnapshotListener { value, error ->

                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                restaurantList = value?.documents?.mapNotNull { doc ->
                    try {
                        RestaurantData(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            category = doc.getString("category") ?: "",
                            imageUrl = doc.getString("logoUrl")
                                ?: doc.getString("imageUrl")
                                ?: "",
                            rating = doc.get("rating")?.toString() ?: "4.5",
                            deliveryTime = doc.getString("deliveryTime") ?: "20-30 min",
                            offer = doc.getString("offer") ?: "",
                            isPureVeg = doc.getBoolean("isPureVeg") ?: true,
                            autoOpen = doc.getBoolean("autoOpen") ?: false,
                            liveStatus = doc.getString("liveStatus") ?: "",
                            isHoliday = doc.getBoolean("isHoliday") ?: false,
                            online = doc.getBoolean("online") ?: true,
                            temporaryClosed = doc.getBoolean("temporaryClosed") ?: false,
                            openingText = doc.getString("openingText") ?: "",
                            weeklySlots = parseRestaurantWeeklySlots(doc.get("weeklySlots")),
                            lat = doc.getDouble("lat") ?: 0.0,
                            lng = doc.getDouble("lng") ?: 0.0,
                            displayOrder = doc.getLong("displayOrder")?.toInt() ?: 999999
                        )
                    } catch (_: Exception) {
                        null
                    }
                }?.sortedBy { it.displayOrder } ?: emptyList()

                isLoading = false
            }

        onDispose {
            registration.remove()
        }
    }

    // Menu is intentionally NOT downloaded when Home opens.
    // It is loaded only after the customer starts searching.

    // ✅ LIVE SEARCH
    LaunchedEffect(
        searchText,
        restaurantList,
        menuCache,
        AddressData.selectedAddress.value
    ) {

        searchJob?.cancel()

        if (searchText.trim().length < 2) {
            searchSuggestions = emptyList()
            isSearchLoading = false
            return@LaunchedEffect
        }

        searchJob = scope.launch {

            delay(300)

            val query = searchText.trim()
            val currentAddress = AddressData.selectedAddress.value

            val nearbyRestaurants = restaurantList.mapNotNull { restaurant ->
                if (
                    currentAddress == null ||
                    restaurant.lat == 0.0 ||
                    restaurant.lng == 0.0
                ) {
                    null
                } else {
                    val km = calculateDistanceKm(
                        restaurant.lat,
                        restaurant.lng,
                        currentAddress.latitude,
                        currentAddress.longitude
                    )

                    if (km <= 15.0) restaurant else null
                }
            }

            val allowedRestaurantIds = nearbyRestaurants.map { it.id }.toSet()

            val restaurantSuggestions = nearbyRestaurants.mapNotNull { restaurant ->
                val name = restaurant.name.trim()
                val score = when {
                    name.equals(query, true) -> 100
                    name.startsWith(query, true) -> 90
                    name.contains(query, true) -> 80
                    else -> 0
                }

                if (score == 0) {
                    null
                } else {
                    SearchSuggestion(
                        type = "restaurant",
                        title = restaurant.name,
                        restaurantId = restaurant.id,
                        restaurantName = restaurant.name,
                        restaurantLogo = restaurant.imageUrl,
                        score = score
                    )
                }
            }

            val addressKey = currentAddress?.let {
                "${it.latitude}_${it.longitude}"
            } ?: "no_address"

            // Load only nearby restaurant menus and only when search is used.
            if (menuCacheAddressKey != addressKey) {
                menuCache = emptyList()
                menuCacheAddressKey = addressKey
            }

            if (menuCache.isEmpty() && nearbyRestaurants.isNotEmpty()) {
                isSearchLoading = true

                val loadedFoods = mutableListOf<SearchSuggestion>()
                var completedRequests = 0

                nearbyRestaurants.forEach { restaurant ->
                    db.collection("restaurants")
                        .document(restaurant.id)
                        .collection("menu")
                        .get()
                        .addOnCompleteListener { task ->

                            if (task.isSuccessful) {
                                task.result?.documents?.forEach { item ->
                                    val visible = item.getBoolean("visible") ?: true

                                    if (visible) {
                                        loadedFoods.add(
                                            SearchSuggestion(
                                                type = "food",
                                                title = item.getString("name") ?: "",
                                                restaurantId = restaurant.id,
                                                restaurantName = restaurant.name,
                                                restaurantLogo = restaurant.imageUrl
                                            )
                                        )
                                    }
                                }
                            }

                            completedRequests++

                            if (completedRequests == nearbyRestaurants.size) {
                                menuCache = loadedFoods.toList()
                                isSearchLoading = false
                            }
                        }
                }
            }

            val foodMap = hashMapOf<String, MutableList<SearchSuggestion>>()

            menuCache
                .filter { allowedRestaurantIds.contains(it.restaurantId) }
                .forEach { food ->
                    val score = when {
                        food.title.equals(query, true) -> 70
                        food.title.startsWith(query, true) -> 60
                        food.title.contains(query, true) -> 50
                        else -> 0
                    }

                    if (score > 0) {
                        foodMap.getOrPut(food.title) { mutableListOf() }
                            .add(food.copy(score = score))
                    }
                }

            val finalFoods = foodMap.values.map { list ->
                list.maxBy { it.score }.copy(restaurantCount = list.size)
            }

            searchSuggestions = (restaurantSuggestions + finalFoods)
                .sortedByDescending { it.score }
        }
    }

    val customerAddress =

        AddressData

            .selectedAddress

            .value

    // ✅ FILTERS
    val maxDeliveryDistanceKm = 15.0

    val restaurantsWithDistance =

        restaurantList.mapNotNull { restaurant ->

            if (

                customerAddress == null ||

                restaurant.lat == 0.0 ||

                restaurant.lng == 0.0

            ) {

                null

            } else {

                val km =

                    calculateDistanceKm(

                        restaurant.lat,

                        restaurant.lng,

                        customerAddress.latitude,

                        customerAddress.longitude

                    )

                if (km <= maxDeliveryDistanceKm) {

                    restaurant.copy(

                        distanceKm = km,

                        distanceText =

                            "${"%.1f".format(km)} km • ${deliveryTimeFromDistance(km)}"

                    )

                } else {

                    null

                }

            }

        }

    val restaurantSortCalendar = remember(currentMinuteTick) {
        java.util.Calendar.getInstance()
    }

    val filteredRestaurants =
        when (selectedFilter) {

            "Pure Veg" ->
                restaurantsWithDistance.filter {
                    it.isPureVeg
                }

            "Offers" ->
                restaurantsWithDistance.filter {
                    it.offer.isNotBlank()
                }

            else ->
                restaurantsWithDistance
        }
            .sortedWith(
                compareBy<RestaurantData> { restaurant ->
                    if (
                        isRestaurantOpenNow(
                            restaurant,
                            restaurantSortCalendar
                        )
                    ) {
                        0
                    } else {
                        1
                    }
                }.thenBy { restaurant ->
                    restaurant.displayOrder
                }
            )

    LaunchedEffect(selectedFilter, customerAddress, filteredRestaurants.size) {
        visibleRestaurantCount = restaurantPageSize
    }

    val visibleRestaurants = filteredRestaurants.take(visibleRestaurantCount)

    if (maintenanceMode) {

        Box(

            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFDF8F3)),

            contentAlignment = Alignment.Center

        ) {

            Column(

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                modifier =
                    Modifier.padding(30.dp)

            ) {

                Text(

                    text = "⚠️",

                    fontSize = 70.sp

                )

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                Text(

                    text = "Sorry!",

                    fontWeight =
                        FontWeight.Bold,

                    fontSize = 30.sp

                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Text(

                    text =
                        "We are temporarily unavailable.",

                    fontSize = 18.sp

                )

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                Text(

                    text = maintenanceMessage,

                    color = Color.Gray,

                    fontSize = 16.sp

                )

                Spacer(
                    modifier =
                        Modifier.height(30.dp)
                )

                Button(

                    onClick = {

                    }

                ) {

                    Text(
                        "Retry"
                    )

                }

            }

        }

        return

    }
    if (!isAddressChecked) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFDF8F3)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        return
    }

    Box(

        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color(0xFFFDF8F3))

    ) {

        // ✅ FULL SCREEN SCROLL

        LazyColumn(

            modifier = Modifier.fillMaxSize(),

            contentPadding =
                PaddingValues(bottom = 130.dp)

        ) {

            // ✅ HEADER

            item {

                Row(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),

                    verticalAlignment =
                        Alignment.CenterVertically

                ) {

                    Column(

                        modifier = Modifier.fillMaxWidth()

                    ) {

                        Text(
                            text = "Delivering To ▼",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Text(

                            text =
                                if (
                                    AddressData.selectedAddress.value != null
                                ) {

                                    "📍 ${AddressData.selectedAddress.value!!.house}, ${AddressData.selectedAddress.value!!.area}"

                                } else {

                                    "📍 Select Address"
                                },

                            fontSize = 18.sp,

                            fontWeight = FontWeight.Bold,

                            maxLines = 1,

                            overflow = TextOverflow.Ellipsis,

                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                    navController.navigate(
                                        "select_address"

                                    )
                                }
                        )

                        Text(
                            text = "Tap to change address",
                            fontSize = 12.sp,
                            color = Color(0xFFFF6B00)
                        )
                    }
                }
            }

            // ✅ SEARCH BAR

            item {

                Card(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),

                    shape =
                        RoundedCornerShape(18.dp),

                    elevation =
                        CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )

                ) {

                    OutlinedTextField(

                        value = searchText,

                        onValueChange = {

                            searchText = it

                        },

                        trailingIcon = {

                            if (searchText.isNotEmpty()) {

                                IconButton(

                                    onClick = {

                                        searchText = ""

                                    }

                                ) {

                                    Icon(

                                        Icons.Default.Close,

                                        contentDescription = null

                                    )

                                }

                            }

                        },

                        modifier = Modifier
                            .fillMaxWidth(),

                        placeholder = {

                            Text(

                                "Search for food, restaurant..."

                            )

                        },

                        leadingIcon = {

                            Icon(

                                imageVector =
                                    Icons.Default.Search,

                                contentDescription = null,

                                tint = Color.Gray
                            )
                        },

                        singleLine = true,

                        shape =
                            RoundedCornerShape(18.dp)
                    )
                }
            }

            // ✅ SEARCH SUGGESTIONS

            if (
                searchText.isNotBlank() &&
                searchSuggestions.isNotEmpty()
            ) {

                item {

                    Card(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),

                        shape = RoundedCornerShape(18.dp)

                    ) {

                        Column {

                            // ----------------------
                            // RESTAURANTS
                            // ----------------------

                            val restaurants =

                                searchSuggestions

                                    .filter {

                                        it.type == "restaurant"

                                    }

                                    .take(5)

                            if (restaurants.isNotEmpty()) {

                                Text(

                                    text = "Restaurants",

                                    fontWeight = FontWeight.Bold,

                                    color = Color.Gray,

                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        top = 14.dp,
                                        bottom = 8.dp
                                    )
                                )

                                restaurants.forEach { suggestion ->

                                    Row(

                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {

                                                searchText = ""

                                                navController.navigate(

                                                    "restaurant_detail/${suggestion.restaurantId}"

                                                )
                                            }
                                            .padding(14.dp),

                                        verticalAlignment = Alignment.CenterVertically

                                    ) {

                                        AsyncImage(

                                            model = suggestion.restaurantLogo,

                                            contentDescription = null,

                                            modifier = Modifier

                                                .size(42.dp)

                                                .clip(RoundedCornerShape(10.dp))

                                        )

                                        Spacer(
                                            modifier = Modifier.width(12.dp)
                                        )

                                        Column {

                                            Text(

                                                suggestion.title,

                                                fontWeight = FontWeight.Bold
                                            )

                                            Text(

                                                "Restaurant",

                                                color = Color.Gray,

                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    HorizontalDivider()
                                }
                            }

                            // ----------------------
                            // FOODS
                            // ----------------------

                            val foods =

                                searchSuggestions

                                    .filter {

                                        it.type == "food"

                                    }

                                    .take(10)

                            if (foods.isNotEmpty()) {

                                Text(

                                    text = "Foods",

                                    fontWeight = FontWeight.Bold,

                                    color = Color.Gray,

                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        top = 14.dp,
                                        bottom = 8.dp
                                    )
                                )

                                foods.forEach { suggestion ->

                                    Row(

                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {

                                                searchText = ""

                                                navController.navigate(

                                                    "food_restaurants/${suggestion.title}"

                                                )
                                            }
                                            .padding(14.dp),

                                        verticalAlignment = Alignment.CenterVertically

                                    ) {

                                        AsyncImage(

                                            model = suggestion.restaurantLogo,

                                            contentDescription = null,

                                            modifier = Modifier

                                                .size(42.dp)

                                                .clip(RoundedCornerShape(10.dp))

                                        )

                                        Spacer(
                                            modifier = Modifier.width(12.dp)
                                        )

                                        Column(

                                            modifier = Modifier.weight(1f)

                                        ) {

                                            Text(

                                                suggestion.title,

                                                fontWeight = FontWeight.Bold
                                            )

                                            Text(

                                                "Available in ${suggestion.restaurantCount} Restaurants",

                                                color = Color.Gray,

                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
            if (searchText.trim().length >= 2 && isSearchLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (

                searchText.isNotBlank()

                &&

                searchSuggestions.isEmpty() &&
                !isSearchLoading

            ) {

                item {

                    Card(

                        modifier = Modifier

                            .fillMaxWidth()

                            .padding(

                                horizontal = 16.dp,

                                vertical = 8.dp

                            ),

                        shape = RoundedCornerShape(18.dp)

                    ) {

                        Column(

                            modifier = Modifier

                                .fillMaxWidth()

                                .padding(24.dp),

                            horizontalAlignment = Alignment.CenterHorizontally

                        ) {

                            Text(

                                text = "🔍",

                                fontSize = 40.sp

                            )

                            Spacer(

                                modifier = Modifier.height(12.dp)

                            )

                            Text(

                                text = "No Results Found",

                                fontWeight = FontWeight.Bold,

                                fontSize = 18.sp

                            )

                            Spacer(

                                modifier = Modifier.height(6.dp)

                            )

                            Text(

                                text = "Try another food or restaurant name.",

                                color = Color.Gray,

                                fontSize = 14.sp

                            )

                        }

                    }

                }

            }
            // ✅ CATEGORY CARDS

            item {

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                val categories = listOf(

                    Triple(
                        "Pizza",
                        R.drawable.pizza,
                        Color(0xFFFF7043)
                    ),

                    Triple(
                        "Burger",
                        R.drawable.burger,
                        Color(0xFFFFC107)
                    ),

                    Triple(
                        "Dosa",
                        R.drawable.dosa,
                        Color(0xFF66BB6A)
                    ),

                    Triple(
                        "Punjabi",
                        R.drawable.punjabi,
                        Color(0xFF42A5F5)
                    ),

                    Triple(
                        "Drinks",
                        R.drawable.drinks,
                        Color(0xFFAB47BC)
                    )
                )

                LazyRow(

                    contentPadding =
                        PaddingValues(horizontal = 16.dp),

                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)

                ) {

                    items(categories) { category ->

                        Card(

                            modifier = Modifier
                                .width(110.dp)
                                .height(140.dp)
                                .clickable {

                                    searchText =
                                        category.first
                                },

                            shape =
                                RoundedCornerShape(22.dp),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        category.third
                                )

                        ) {

                            Column(

                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),

                                verticalArrangement =
                                    Arrangement.SpaceBetween

                            ) {

                                Image(

                                    painter =
                                        painterResource(
                                            id = category.second
                                        ),

                                    contentDescription = null,

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .clip(
                                            RoundedCornerShape(16.dp)
                                        )
                                )

                                Text(

                                    text = category.first,

                                    color = Color.White,

                                    fontWeight = FontWeight.Bold,

                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // ✅ FILTERS

            item {

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                Row(

                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(
                            rememberScrollState()
                        )
                        .padding(horizontal = 16.dp),

                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)

                ) {

                    listOf(

                        "All",
                        "Pure Veg",
                        "Rating 4+",
                        "Fast Delivery",
                        "Offers",
                        "Pizza",
                        "Burger",
                        "Dosa",
                        "Punjabi",
                        "Drinks"

                    ).forEach { filter ->

                        FilterChip(

                            selected =
                                selectedFilter == filter,

                            onClick = {

                                selectedFilter = filter
                            },

                            label = {

                                Text(
                                    text = filter
                                )
                            }
                        )
                    }
                }
            }

            // ✅ OFFER BANNER

            item {

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                ShareAutoBanner()

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(

                    text = "All Restaurants",

                    fontSize = 20.sp,

                    fontWeight = FontWeight.Bold,

                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }

            // ✅ RESTAURANTS

            if (isLoading) {

                items(3) {

                    ShimmerMenuCard()
                }

            } else {

                if (

                    filteredRestaurants.isEmpty()

                ) {

                    item {

                        Column(

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 24.dp,
                                    vertical = 60.dp
                                ),

                            horizontalAlignment =
                                Alignment.CenterHorizontally

                        ) {

                            Text(

                                text = "🍽️",

                                fontSize = 70.sp
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(18.dp)
                            )

                            Text(
                                text = "🍃 VeggieGo is Coming Soon to Your Area",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )

                            Text(
                                text = "We're expanding our delivery service.\nPlease select another nearby address or check back soon.",
                                color = Color.Gray,
                                fontSize = 15.sp
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(20.dp)
                            )

                            Button(

                                onClick = {

                                    navController.navigate("select_address")
                                },

                                shape =
                                    RoundedCornerShape(14.dp)

                            ) {

                                Text(
                                    text = "📍 Change Delivery Address"
                                )
                            }
                        }
                    }

                } else {

                    itemsIndexed(
                        items = visibleRestaurants,
                        key = { _, restaurant -> restaurant.id }
                    ) { index, restaurant ->

                        RestaurantCard(
                            restaurant = restaurant,
                            timeVersion = currentMinuteTick,
                            onRestaurantClick = { restaurantId ->
                                navController.navigate(
                                    "restaurant_detail/$restaurantId"
                                )
                            }
                        )

                        if (
                            index == visibleRestaurants.lastIndex &&
                            visibleRestaurantCount < filteredRestaurants.size
                        ) {
                            LaunchedEffect(index, filteredRestaurants.size) {
                                visibleRestaurantCount =
                                    (visibleRestaurantCount + restaurantPageSize)
                                        .coerceAtMost(filteredRestaurants.size)
                            }
                        }
                    }
                }
            }
        }

        // ✅ BOTTOM BAR

        Box(

            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 75.dp)

        ) {

            FloatingCartBar(
                navController
            )
        }
        Box(

            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()

        ) {

            BottomBar(
                navController
            )
        }
    }
}
@Composable
fun ShareAutoBanner() {

    val context = LocalContext.current

    var showShare by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3500)
            showShare = !showShare
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable {
                if (showShare) {

                    val shareText =
                        "🍃 Enjoy delicious Pure Veg food with VeggieGo!\n" +
                                "Download now:\n" +
                                "https://play.google.com/store/apps/details?id=com.veggiego.customer"

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }

                    context.startActivity(
                        Intent.createChooser(
                            intent,
                            "Share VeggieGo"
                        )
                    )
                }
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (showShare)
                    Color(0xFFE8F5E9)
                else
                    Color(0xFFFFF3CD)
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = if (showShare) "📤" else "🛵",
                fontSize = 30.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text =
                        if (showShare)
                            "Share VeggieGo"
                        else
                            "FREE DELIVERY",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text =
                        if (showShare)
                            "Invite friends & family"
                        else
                            "On eligible orders",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }

            if (showShare) {
                Text(
                    text = "Share",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A)
                )
            }
        }
    }
}