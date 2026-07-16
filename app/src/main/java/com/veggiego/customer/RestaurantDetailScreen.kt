package com.veggiego.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlinx.coroutines.launch
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import kotlinx.coroutines.CoroutineScope
import com.google.firebase.firestore.Query
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    navController: NavController,
    restaurantId: String,
    focusItem: String = ""
) {

    val db =
        FirebaseFirestore.getInstance()

    var restaurant by remember {

        mutableStateOf(
            RestaurantData()
        )
    }

    var menuItems by remember {

        mutableStateOf(
            listOf<MenuItem>()
        )
    }

    var sortedCategories by remember {

        mutableStateOf(
            listOf<String>()
        )

    }

    var sortedSubCategories by remember {

        mutableStateOf(
            listOf<Pair<String, String>>()
        )

    }

    var selectedCategory by remember {

        mutableStateOf("All")
    }
    var searchQuery by remember {

        mutableStateOf(
            TextFieldValue("")
        )
    }


    LaunchedEffect(restaurantId) {

        db.collection("restaurants")

            .document(restaurantId)

            .addSnapshotListener { value, _ ->

                if (value != null && value.exists()) {

                    restaurant = RestaurantData(

                        id = value.id,

                        name =
                            value.getString("name") ?: "",

                        category =
                            value.getString("category") ?: "",

                        imageUrl =
                            value.getString("logoUrl")
                                ?: value.getString("imageUrl")
                                ?: "",

                        rating =
                            value.get("rating")
                                ?.toString() ?: "4.5",

                        deliveryTime =
                            value.getString("deliveryTime")
                                ?: "20 mins",

                        offer =
                            value.getString("offer") ?: "",

                        isPureVeg =
                            value.getBoolean("isPureVeg")
                                ?: true,

                        autoOpen =
                            value.getBoolean("autoOpen")
                                ?: false,

                        liveStatus =
                            value.getString("liveStatus")
                                ?: "",

                        isHoliday =
                            value.getBoolean("isHoliday")
                                ?: false,

                        online =
                            value.getBoolean("online")
                                ?: true,

                        temporaryClosed =
                            value.getBoolean("temporaryClosed")
                                ?: false,

                        openingText =
                            value.getString("openingText") ?: "",

                        lat =
                            value.getDouble("lat") ?: 0.0,

                        lng =
                            value.getDouble("lng") ?: 0.0
                    )
                }
            }


        db.collection("restaurants")

            .document(restaurantId)

            .collection("menu")

            .orderBy(
                "sortOrder",
                Query.Direction.ASCENDING
            )

            .addSnapshotListener { value, _ ->

                if (value != null) {

                    menuItems =

                        value.documents.mapNotNull { doc ->

                            val visible =

                                doc.getBoolean("visible")
                                    ?: true

                            val available =

                                doc.getBoolean("available")
                                    ?: true

                            val startTime =

                                doc.getString("startTime")
                                    ?: ""

                            val endTime =

                                doc.getString("endTime")
                                    ?: ""
                            val timeSlots =

                                doc.get("timeSlots")
                                        as? List<Map<String, Any>>
                                    ?: emptyList()

                            // 🚀 HIDDEN FILTER

                            if (!visible) {

                                return@mapNotNull null
                            }


                            // 🚀 TIME FILTER

                            val currentTime =

                                java.text.SimpleDateFormat(
                                    "HH:mm",
                                    java.util.Locale.getDefault()
                                ).format(
                                    java.util.Date()
                                )

// 🚀 OLD SINGLE SLOT SUPPORT

                            if (

                                startTime.isNotEmpty() &&

                                endTime.isNotEmpty()

                            ) {

                                if (

                                    currentTime < startTime ||

                                    currentTime > endTime

                                ) {

                                    return@mapNotNull null
                                }
                            }

// 🚀 NEW MULTI SLOT SUPPORT

                            if (timeSlots.isNotEmpty()) {

                                var insideSlot = false

                                timeSlots.forEach { slot ->

                                    val slotStart =
                                        slot["start"]
                                            ?.toString() ?: ""

                                    val slotEnd =
                                        slot["end"]
                                            ?.toString() ?: ""

                                    if (

                                        currentTime >= slotStart &&

                                        currentTime <= slotEnd

                                    ) {

                                        insideSlot = true
                                    }
                                }

                                if (!insideSlot) {

                                    return@mapNotNull null
                                }
                            }

                            MenuItem(

                                name =
                                    doc.getString("name") ?: "",
                                price =

                                    (doc.getLong("price")
                                        ?: 0L).toInt(),

                                variants =

                                    (doc.get("variants")
                                            as? List<Map<String, Any>>)

                                        ?.map {

                                            VariantModel(

                                                name =
                                                    it["name"]
                                                            as? String ?: "",

                                                price =
                                                    (it["price"]
                                                            as? Long)
                                                        ?.toInt() ?: 0
                                            )
                                        }

                                        ?: emptyList(),

                                addons =

                                    (doc.get("addons")
                                            as? List<Map<String, Any>>)

                                        ?.map {

                                            AddonModel(

                                                name =
                                                    it["name"]
                                                            as? String ?: "",

                                                price =
                                                    (it["price"]
                                                            as? Long)
                                                        ?.toInt() ?: 0
                                            )
                                        }

                                        ?: emptyList(),

                                image =
                                    doc.getString("imageUrl")

                                        ?: doc.getString("image")

                                        ?: "",

                                category = (

                                        doc.getString(
                                            "categoryName"
                                        )

                                            ?.trim()

                                            ?.takeIf {

                                                it.isNotEmpty()
                                            }

                                            ?: doc.getString(
                                                "category"
                                            )

                                                ?.trim()

                                                ?.takeIf {

                                                    it.isNotEmpty()
                                                }

                                            ?: "Others"

                                        ),
                                subCategory =


                                    doc.getString(
                                        "subCategoryName"
                                    )

                                        ?: doc.getString(
                                            "subCategory"
                                        )

                                        ?: "",
                                description =

                                    doc.getString("description")
                                        ?: "",

                                veg =
                                    doc.getBoolean("veg")
                                        ?: true,

                                available = available,

                                recommended =
                                    doc.getBoolean("recommended")
                                        ?: false,

                                bestseller =
                                    doc.getBoolean("bestseller")
                                        ?: false
                            )

                        }
                }
            }

    }
    val currentTime =

        java.text.SimpleDateFormat(
            "HH:mm",
            java.util.Locale.getDefault()
        ).format(
            java.util.Date()
        )

    var categoryTimeMap by remember {

        mutableStateOf(
            mapOf<String, Boolean>()
        )
    }
    var categoryStockMap by remember {
        mutableStateOf(mapOf<String, Boolean>())
    }

    var categoryVisibleMap by remember {
        mutableStateOf(mapOf<String, Boolean>())
    }

    var subCategoryStockMap by remember {
        mutableStateOf(mapOf<String, Boolean>())
    }

    var subCategoryTimeMap by remember {
        mutableStateOf(mapOf<String, Boolean>())
    }
    var subCategoryVisibleMap by remember {

        mutableStateOf(
            mapOf<String, Boolean>()
        )
    }

    LaunchedEffect(menuItems) {

        FirebaseFirestore
            .getInstance()
            .collection("restaurants")
            .document(restaurantId)
            .collection("categories")

            .orderBy(

                "sortOrder",

                Query.Direction.ASCENDING

            )

            .get()
            .addOnSuccessListener { result ->
                sortedCategories =

                    result.documents.mapNotNull {

                        it.getString("name")

                    }

                result.documents.forEach { doc ->

                    val name =
                        doc.getString("name")
                            ?: return@forEach

                    val timeSlots =

                        doc.get("timeSlots")
                                as? List<Map<String, Any>>
                            ?: emptyList()
                    val available =
                        doc.getBoolean("available") ?: true

                    val visible =
                        doc.getBoolean("visible") ?: true

                    categoryVisibleMap =
                        categoryVisibleMap.toMutableMap().apply {
                            put(name, visible)
                        }

                    categoryStockMap =
                        categoryStockMap.toMutableMap().apply {

                            put(
                                name,
                                available
                            )

                        }
                    if (timeSlots.isEmpty()) {

                        categoryTimeMap =

                            categoryTimeMap.toMutableMap().apply {

                                put(
                                    name,
                                    true
                                )
                            }

                    } else {

                        var inside = false

                        timeSlots.forEach { slot ->

                            val start =
                                slot["start"]
                                    ?.toString() ?: ""

                            val end =
                                slot["end"]
                                    ?.toString() ?: ""

                            if (

                                currentTime >= start &&

                                currentTime <= end

                            ) {

                                inside = true
                            }
                        }

                        categoryTimeMap =

                            categoryTimeMap.toMutableMap().apply {

                                put(
                                    name,
                                    inside
                                )
                            }
                    }
                }
            }
    }
    LaunchedEffect(menuItems) {

        FirebaseFirestore
            .getInstance()
            .collection("restaurants")
            .document(restaurantId)
            .collection("subcategories")

            .orderBy(

                "sortOrder",

                Query.Direction.ASCENDING

            )

            .get()
            .addOnSuccessListener { result ->
                sortedSubCategories =

                    result.documents.mapNotNull { doc ->

                        val categoryId =

                            doc.getString("categoryId")
                                ?: return@mapNotNull null

                        val name =

                            doc.getString("name")
                                ?: return@mapNotNull null

                        Pair(

                            categoryId,

                            name

                        )

                    }

                result.documents.forEach { doc ->

                    val name =
                        doc.getString("name")
                            ?: return@forEach

                    val available =
                        doc.getBoolean("available")
                            ?: true

                    subCategoryStockMap =
                        subCategoryStockMap.toMutableMap().apply {

                            put(
                                name,
                                available
                            )

                        }
                    val visible =
                        doc.getBoolean("visible")
                            ?: true

                    subCategoryVisibleMap =
                        subCategoryVisibleMap.toMutableMap().apply {

                            put(
                                name,
                                visible
                            )

                        }
                    val timeSlots =

                        doc.get("timeSlots")
                                as? List<Map<String, Any>>
                            ?: emptyList()

                    if (timeSlots.isEmpty()) {

                        subCategoryTimeMap =

                            subCategoryTimeMap.toMutableMap().apply {

                                put(
                                    name,
                                    true
                                )

                            }

                    } else {

                        var inside = false

                        timeSlots.forEach { slot ->

                            val start =
                                slot["start"]
                                    ?.toString() ?: ""

                            val end =
                                slot["end"]
                                    ?.toString() ?: ""

                            if (

                                currentTime >= start &&

                                currentTime <= end

                            ) {

                                inside = true

                            }

                        }

                        subCategoryTimeMap =

                            subCategoryTimeMap.toMutableMap().apply {

                                put(
                                    name,
                                    inside
                                )

                            }

                    }

                }

            }

    }
    val visibleCategories =

        sortedCategories.filter { category ->

            categoryTimeMap[category] != false &&

                    categoryVisibleMap[category] != false &&

                    menuItems.any {

                        it.category == category

                    }

        }

    val categories =

        listOf("All") +

                visibleCategories

    // =============================
// Recommended (Always Same)
// =============================

    val recommendedItems =

        menuItems.filter {

            it.recommended &&

                    categoryTimeMap[it.category] != false

        }

// =============================
// Search Only Menu
// =============================

    val filteredMenuItems =

        menuItems.filter { item ->

            val categoryMatch =

                if (selectedCategory == "All") {

                    true

                } else {

                    item.category == selectedCategory

                }

            val searchMatch =

                item.name.contains(
                    searchQuery.text,
                    ignoreCase = true
                ) ||

                        item.description.contains(
                            searchQuery.text,
                            ignoreCase = true
                        )

            categoryMatch &&
                    searchMatch &&
                    categoryTimeMap[item.category] != false &&
                    categoryVisibleMap[item.category] != false &&
                    subCategoryVisibleMap[item.subCategory] != false &&
                    subCategoryTimeMap[item.subCategory] != false

        }

// =============================

    val groupedItems =

        filteredMenuItems.groupBy {

            it.category

        }
    // =============================
// Category -> SubCategory Map
// =============================

    val categoryMap = remember(

        menuItems,

        sortedCategories,

        sortedSubCategories,

        categoryVisibleMap,

        categoryTimeMap,

        subCategoryVisibleMap,

        subCategoryTimeMap

    ) {

        buildMap<String, List<String>> {

            sortedCategories.forEach { category ->

                if (categoryVisibleMap[category] == false) return@forEach

                if (categoryTimeMap[category] == false) return@forEach

                val subs =

                    menuItems

                        .filter {

                            it.category == category &&

                                    subCategoryVisibleMap[it.subCategory] != false &&

                                    subCategoryTimeMap[it.subCategory] != false

                        }

                        .sortedBy { item ->

                            sortedSubCategories.indexOfFirst {

                                it.second == item.subCategory

                            }

                        }

                        .map {

                            it.subCategory

                        }

                        .distinct()

                put(

                    category,

                    subs

                )

            }

        }

    }
    val listState =
        rememberLazyListState()

    var collapsedSubCategories by remember {
        mutableStateOf(setOf<String>())
    }

    fun scrollToMenuTarget(
        target: String,
        scope: CoroutineScope,
        collapsedSubCategories: Set<String>
    ) {
        if (target.isBlank()) return

        scope.launch {

            var index = 0

            index++ // RestaurantHeader
            index++ // RestaurantInfo
            index++ // Search Bar

            if (
                searchQuery.text.isBlank() &&
                recommendedItems.isNotEmpty()
            ) {
                index++ // Recommended
            }

            visibleCategories.forEach { category ->

                val itemsList =
                    groupedItems[category]
                        ?: return@forEach

                val categoryIndex = index
                index++ // Category Header

                if (category.equals(target, ignoreCase = true)) {
                    listState.scrollToItem(categoryIndex)
                    return@launch
                }

                val orderedSubCategories =

                    categoryMap[category]

                        ?: emptyList()

                orderedSubCategories.forEach { subCategory ->

                    val subItems =

                        itemsList.filter {

                            it.subCategory == subCategory

                        }

                    if (subItems.isEmpty()) return@forEach

                    if (
                        subCategoryTimeMap[subCategory] == false ||
                        subCategoryVisibleMap[subCategory] == false
                    ) {
                        return@forEach
                    }

                    val subCategoryIndex = index
                    index++ // SubCategory Header

                    if (subCategory.equals(target, ignoreCase = true)) {
                        listState.scrollToItem(subCategoryIndex)
                        return@launch
                    }

                    if (!collapsedSubCategories.contains(subCategory)) {

                        subItems.forEach { menuItem ->

                            if (
                                menuItem.name.equals(
                                    target,
                                    ignoreCase = true
                                )
                            ) {
                                listState.scrollToItem(index)
                                return@launch
                            }

                            index++
                        }

                    }
                }
            }
        }
    }
    LaunchedEffect(
        focusItem,
        groupedItems,
        categoryMap
    ) {

        if (focusItem.isBlank()) return@LaunchedEffect

        if (categoryMap.isEmpty()) return@LaunchedEffect

        scrollToMenuTarget(
            focusItem,
            this,
            collapsedSubCategories
        )
    }

    val coroutineScope = rememberCoroutineScope()

    val bottomSheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

    var showMenuSheet by remember {
        mutableStateOf(false)
    }

    var expandedCategory by remember {
        mutableStateOf("")
    }



    Box(

        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color.White)

    ) {

        LazyColumn(

            state = listState,

            modifier = Modifier

                .fillMaxSize()

        ) {

            item {

                RestaurantHeader(

                    restaurant = restaurant,

                    onBack = {

                        navController.popBackStack()
                    }
                )
            }

            item {

                RestaurantInfoSection(
                    restaurant
                )
                if (restaurant.temporaryClosed) {

                    Text(

                        text = "🔴 TEMPORARILY CLOSED",

                        color = Color.Red,

                        modifier =
                            Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                    )
                } else if (restaurant.isHoliday) {

                    Text(

                        text = "🏖 HOLIDAY TODAY",

                        color = Color.Red,

                        modifier =
                            Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                    )
                } else if (

                    restaurant.autoOpen &&
                    restaurant.online

                ) {

                    Text(

                        text = "🟢 OPEN NOW",

                        color = Color(0xFF16A34A),

                        modifier =
                            Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                    )
                } else {

                    Text(

                        text =
                            formatOpeningText(
                                restaurant.openingText
                            ),

                        color = Color(0xFFFF9800),

                        modifier =
                            Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                    )
                }
            }
            item {

                PremiumMenuSearchBar(

                    value = searchQuery,

                    onValueChange = {

                        searchQuery = it
                    }
                )
            }
            if (

                searchQuery.text.isBlank()

                &&

                recommendedItems.isNotEmpty()

            ) {

                item {

                    Column {

                        Text(
                            text = "Recommended",

                            fontSize = 24.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color = Color.Black,

                            modifier = Modifier
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 14.dp
                                )
                        )

                        Column {

                            recommendedItems.forEach { item ->

                                RecommendedItemCard(

                                    item = item,

                                    restaurantId = restaurantId,

                                    restaurantName = restaurant.name,

                                    restaurantOpen =

                                        restaurant.autoOpen &&
                                                restaurant.online &&
                                                !restaurant.temporaryClosed &&
                                                !restaurant.isHoliday,

                                    categoryAvailable =

                                        categoryTimeMap[item.category] != false &&

                                                categoryStockMap[item.category] != false

                                )

                                Spacer(
                                    modifier = Modifier.height(12.dp)
                                )
                            }

                        }

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )
                    }
                }
            }

            visibleCategories.forEach { category ->

                val itemsList =

                    groupedItems[category]

                        ?: return@forEach

                item {

                    Text(

                        text = category,

                        fontSize = 22.sp,

                        fontWeight = FontWeight.Bold,

                        color = Color.Black,

                        modifier = Modifier
                            .padding(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            )

                    )

                }

                val orderedSubCategories =

                    categoryMap[category]

                        ?: emptyList()

                orderedSubCategories.forEach { subCategory ->

                    val subItems =

                        itemsList.filter {

                            if (subCategory == "Others") {

                                it.subCategory.isBlank()

                            } else {

                                it.subCategory == subCategory

                            }

                        }

                    if (subItems.isEmpty()) {

                        return@forEach

                    }
                    if (

                        subCategoryTimeMap[subCategory] == false ||

                        subCategoryVisibleMap[subCategory] == false

                    ) {

                        return@forEach

                    }

                    item {

                        Row(

                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                    collapsedSubCategories =

                                        if (collapsedSubCategories.contains(subCategory)) {

                                            collapsedSubCategories - subCategory

                                        } else {

                                            collapsedSubCategories + subCategory
                                        }

                                }
                                .padding(
                                    start = 24.dp,
                                    end = 16.dp,
                                    top = 8.dp,
                                    bottom = 8.dp
                                ),

                            verticalAlignment = Alignment.CenterVertically

                        ) {

                            Text(

                                text = subCategory,

                                modifier = Modifier.weight(1f),

                                fontSize = 18.sp,

                                fontWeight = FontWeight.SemiBold,

                                color = Color.DarkGray

                            )

                            Icon(

                                imageVector =

                                    if (collapsedSubCategories.contains(subCategory))

                                        Icons.Default.ExpandMore

                                    else

                                        Icons.Default.ExpandLess,

                                contentDescription = null,

                                tint = Color.Gray

                            )
                        }
                    }

                    if (!collapsedSubCategories.contains(subCategory)) {

                    items(subItems) { item ->

                        val cartItem =

                            CartData.items.find {

                                it.item.name == item.name

                            }

                        MenuItemRow(

                            item = item,

                            cartItem = cartItem,

                            restaurantId = restaurantId,

                            restaurantName = restaurant.name,

                            restaurantOpen =

                                restaurant.autoOpen &&

                                        restaurant.online &&

                                        !restaurant.temporaryClosed &&

                                        !restaurant.isHoliday,
                            categoryAvailable =

                                categoryStockMap[item.category] != false,
                            subCategoryAvailable =

                                subCategoryStockMap[item.subCategory] != false,

                            onAdd = {

                                if (

                                    CartData.currentRestaurantId.value.isNotEmpty()

                                    &&

                                    CartData.currentRestaurantId.value != restaurantId

                                ) {

                                    CartData.clearCart()

                                }

                                CartData.addToCart(

                                    restaurantId,

                                    restaurant.name,

                                    item

                                )

                            },

                            onIncrease = {

                                CartData.increase(it)

                            },

                            onDecrease = {

                                CartData.decrease(it)

                            }

                        )

                    }

                    }

                }
            }

            item {

                Spacer(
                    modifier =
                        Modifier.height(
                            100.dp
                        )
                )
            }
        }
        FloatingActionButton(

            onClick = {

                showMenuSheet = true

            },

            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 16.dp,
                    bottom = 90.dp
                ),

            containerColor = Color(0xFFFFF3CD),

            contentColor = Color.Black

        ) {

            Row(

                modifier = Modifier.padding(
                    horizontal = 12.dp
                ),

                verticalAlignment = Alignment.CenterVertically

            ) {

                Icon(
                    Icons.Default.Menu,
                    null
                )

                Spacer(
                    Modifier.width(6.dp)
                )

                Text("Menu")
            }
        }
        if (showMenuSheet) {

            ModalBottomSheet(

                onDismissRequest = {

                    showMenuSheet = false

                },

                sheetState = bottomSheetState,

                containerColor = Color.White

            ) {
                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.70f)

                ) {

                    LazyColumn(

                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),

                        contentPadding = PaddingValues(bottom = 20.dp)

                    ) {
                        // ============================
// Recommended
// ============================

                        if (

                            searchQuery.text.isBlank()

                            &&

                            recommendedItems.isNotEmpty()

                        ) {

                            item {

                                Row(

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {

                                            showMenuSheet = false

                                            coroutineScope.launch {

                                                listState.scrollToItem(3)

                                            }

                                        }
                                        .padding(
                                            horizontal = 20.dp,
                                            vertical = 16.dp
                                        ),

                                    verticalAlignment = Alignment.CenterVertically

                                ) {

                                    Text(

                                        text = "Recommended",

                                        modifier = Modifier.weight(1f),

                                        fontSize = 18.sp,

                                        fontWeight = FontWeight.Bold

                                    )

                                    Text(

                                        text = recommendedItems.size.toString(),

                                        color = Color.Gray,

                                        fontSize = 16.sp

                                    )

                                }

                                HorizontalDivider(
                                    color = Color(0xFFF3F3F3)
                                )

                            }

                        }
                        categoryMap.forEach { (category, subList) ->

                            // Total Items Count

                            val itemCount =
                                menuItems.count {
                                    it.category == category &&
                                            categoryVisibleMap[it.category] != false &&
                                            categoryTimeMap[it.category] != false &&
                                            subCategoryVisibleMap[it.subCategory] != false &&
                                            subCategoryTimeMap[it.subCategory] != false
                                }

                            item {

                                Column {

                                    Row(

                                        modifier = Modifier

                                            .fillMaxWidth()

                                            .clickable {

                                                // Category par touch hote hi direct scroll

                                                showMenuSheet = false

                                                selectedCategory = "All"

                                                scrollToMenuTarget(
                                                    category,
                                                    coroutineScope,
                                                    collapsedSubCategories
                                                )

                                            }

                                            .padding(

                                                horizontal = 20.dp,

                                                vertical = 16.dp

                                            ),

                                        verticalAlignment = Alignment.CenterVertically

                                    ) {

                                        Row(

                                            modifier = Modifier.weight(1f),

                                            verticalAlignment = Alignment.CenterVertically

                                        ) {

                                            Text(

                                                text = category,

                                                fontSize = 18.sp,

                                                fontWeight = FontWeight.Bold

                                            )

                                            if (subList.size > 1) {

                                                Text(

                                                    text = if (expandedCategory == category) "-" else "+",

                                                    modifier = Modifier
                                                        .padding(start = 6.dp)
                                                        .clickable {

                                                            expandedCategory =
                                                                if (expandedCategory == category)
                                                                    ""
                                                                else
                                                                    category

                                                        },

                                                    color = Color(0xFF16A34A),

                                                    fontWeight = FontWeight.Bold,

                                                    fontSize = 20.sp

                                                )

                                            }

                                        }

                                        Text(

                                            text = itemCount.toString(),

                                            color = Color.Gray,

                                            fontSize = 16.sp

                                        )

                                    }

                                    HorizontalDivider(

                                        color = Color(0xFFF3F3F3)

                                    )

                                }

                            }

                            if (

                                subList.size > 1 &&

                                expandedCategory == category

                            ) {

                                items(subList) { sub ->

                                    val subCount =
                                        menuItems.count {
                                            it.category == category &&
                                                    it.subCategory == sub &&
                                                    categoryVisibleMap[it.category] != false &&
                                                    categoryTimeMap[it.category] != false &&
                                                    subCategoryVisibleMap[it.subCategory] != false &&
                                                    subCategoryTimeMap[it.subCategory] != false
                                        }

                                    Row(

                                        modifier = Modifier

                                            .fillMaxWidth()

                                            .clickable {

                                                showMenuSheet = false

                                                selectedCategory = "All"

                                                scrollToMenuTarget(
                                                    sub,
                                                    coroutineScope,
                                                    collapsedSubCategories
                                                )

                                            }

                                            .padding(

                                                start = 34.dp,

                                                end = 20.dp,

                                                top = 14.dp,

                                                bottom = 14.dp

                                            ),

                                        verticalAlignment = Alignment.CenterVertically

                                    ) {

                                        Text(

                                            text = sub,

                                            modifier = Modifier.weight(1f),

                                            fontSize = 16.sp,

                                            color = Color.DarkGray

                                        )

                                        Text(

                                            text = subCount.toString(),

                                            color = Color.Gray

                                        )

                                    }

                                }

                            }

                        }

                        item {

                            Spacer(

                                Modifier.height(90.dp)

                            )

                        }

                    }
                    Surface(

                        shadowElevation = 8.dp,

                        tonalElevation = 8.dp

                    ) {

                        OutlinedButton(

                            onClick = {

                                showMenuSheet = false

                            },

                            modifier = Modifier

                                .fillMaxWidth()

                                .padding(16.dp)

                                .height(54.dp),

                            shape = RoundedCornerShape(16.dp)

                        ) {

                            Icon(

                                Icons.Default.Close,

                                null

                            )

                            Spacer(

                                Modifier.width(8.dp)

                            )

                            Text(

                                "Close",

                                fontWeight = FontWeight.Bold

                            )

                        }

                    }
                }
            }
        }
        Box(

            modifier = Modifier

                .align(Alignment.BottomCenter)

                .navigationBarsPadding()

        ) {

            FloatingCartBar(

                navController

            )

        }
    }
}

@Composable
fun RestaurantHeader(
    restaurant: RestaurantData,
    onBack: () -> Unit
) {

    Box {

        AsyncImage(

            model =
                restaurant.imageUrl,

            contentDescription = null,

            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),

            contentScale =
                ContentScale.Crop
        )

        Box(

            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)

                .background(

                    Brush.verticalGradient(

                        listOf(
                            Color.Transparent,
                            Color.Black.copy(
                                alpha = 0.7f
                            )
                        )
                    )
                )
        )

        IconButton(

            onClick = onBack,

            modifier = Modifier
                .padding(16.dp)
                .background(
                    Color.White,
                    CircleShape
                )
                .size(36.dp)

        ) {

            Icon(

                Icons.Default.ArrowBack,

                contentDescription = null,

                modifier =
                    Modifier.size(20.dp)
            )
        }

        Column(

            modifier = Modifier
                .align(
                    Alignment.BottomStart
                )
                .padding(16.dp)

        ) {

            Text(

                text =
                    restaurant.name,

                color =
                    Color.White,

                fontSize = 26.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Row(

                verticalAlignment =
                    Alignment.CenterVertically

            ) {

                Icon(

                    Icons.Default.Star,

                    contentDescription = null,

                    tint =
                        Color(0xFFFFC107),

                    modifier =
                        Modifier.size(16.dp)
                )

                Spacer(
                    modifier =
                        Modifier.width(4.dp)
                )

                Text(

                    text =
                        "${restaurant.rating} • ${restaurant.deliveryTime}",

                    color =
                        Color.White,

                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun RestaurantInfoSection(
    restaurant: RestaurantData
) {

    Column(

        modifier = Modifier
            .padding(16.dp)

    ) {

        if (
            restaurant.offer.isNotEmpty()
        ) {

            Card(

                colors =
                    CardDefaults.cardColors(

                        containerColor =
                            Color(0xFFE8F5E9)
                    ),

                shape =
                    RoundedCornerShape(12.dp),

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Row(

                    modifier = Modifier
                        .padding(12.dp),

                    verticalAlignment =
                        Alignment.CenterVertically

                ) {

                    Text(
                        text = "🚚",
                        fontSize = 20.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    Text(

                        text =
                            restaurant.offer,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            Color(0xFF2E7D32),

                        fontSize = 14.sp
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )
        }

        Text(

            text =
                restaurant.category,

            color = Color.Gray,

            fontSize = 14.sp,

            maxLines = 2,

            overflow =
                TextOverflow.Ellipsis
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        HorizontalDivider(
            color = Color(0xFFF0F0F0)
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemRow(

    item: MenuItem,

    cartItem: CartItem?,

    restaurantId: String,

    restaurantName: String,

    restaurantOpen: Boolean,

    categoryAvailable: Boolean,

    subCategoryAvailable: Boolean,

    onAdd: () -> Unit,

    onIncrease: (CartItem) -> Unit,

    onDecrease: (CartItem) -> Unit

) {

    var showVariantSheet by remember {

        mutableStateOf(false)
    }
    val basePrice =

        if (item.variants.isNotEmpty()) {

            item.variants.first().price

        } else {

            item.price

        }

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 18.dp
            ),

        verticalAlignment =
            Alignment.Top

    ) {

        Column(

            modifier =
                Modifier
                    .weight(1f)
                    .padding(end = 14.dp)

        ) {

            Box(

                modifier = Modifier
                    .size(16.dp)

                    .border(
                        1.dp,

                        if (item.veg)

                            Color(0xFF16A34A)

                        else

                            Color.Red
                    ),

                contentAlignment =
                    Alignment.Center

            ) {

                Box(

                    modifier = Modifier
                        .size(8.dp)

                        .background(

                            if (item.veg)

                                Color(0xFF16A34A)

                            else

                                Color.Red,

                            CircleShape
                        )
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(

                text = item.name,


                fontSize = 18.sp,

                fontWeight =
                    FontWeight.Bold,

                color = Color.Black
            )

            if (

                item.description.isNotEmpty()

            ) {

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(

                    text =
                        item.description,

                    color = Color.Gray,

                    fontSize = 14.sp
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(

                text = "₹$basePrice",

                fontSize = 18.sp,

                fontWeight = FontWeight.Bold,

                color = Color(0xFF16A34A)

            )
        }

        Box(

            modifier =
                Modifier
                    .size(
                        width = 124.dp,
                        height = 205.dp
                    )
                    .padding(top = 4.dp),

            contentAlignment =
                Alignment.TopCenter

        ) {
            if (
                item.image.isNotEmpty()
            ) {

                AsyncImage(

                    model = item.image,

                    contentDescription = null,

                    modifier = Modifier
                        .size(112.dp)

                        .clip(
                            RoundedCornerShape(18.dp)
                        ),

                    contentScale =
                        ContentScale.Crop
                )
            }

            Column(

                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-14).dp),

                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                if (!restaurantOpen) {

                    Button(

                        onClick = {},

                        enabled = false

                    ) {

                        Text("CLOSED")
                    }
                }

                else if (

                    !item.available ||

                    !categoryAvailable ||

                    !subCategoryAvailable

                ) {

                    Surface(

                        color = Color(0xFFD50000),

                        shape = RoundedCornerShape(50.dp)

                    ) {

                        Text(

                            text = "OUT OF STOCK",

                            color = Color.White,

                            fontWeight = FontWeight.Bold,

                            maxLines = 1,

                            fontSize = 12.sp,

                            modifier = Modifier.padding(
                                horizontal = 18.dp,
                                vertical = 8.dp
                            )
                        )
                    }
                }

                else if (cartItem == null) {

                    Card(

                        modifier = Modifier
                            .height(42.dp)
                            .clickable {

                                if (

                                    item.variants
                                        .isNotEmpty()

                                ) {

                                    showVariantSheet = true
                                }

                                else {

                                    onAdd()
                                }
                            },

                        shape =
                            RoundedCornerShape(14.dp),

                        border = BorderStroke(
                            1.dp,
                            Color(0xFF16A34A)
                        ),

                        colors =
                            CardDefaults.cardColors(
                                containerColor = Color.White
                            ),

                        elevation =
                            CardDefaults.cardElevation(
                                defaultElevation = 4.dp
                            )
                    ) {

                        Box(

                            modifier = Modifier
                                .padding(
                                    horizontal = 22.dp,
                                    vertical = 10.dp
                                ),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(

                                text = "ADD",

                                color =
                                    Color(0xFF16A34A),

                                fontWeight =
                                    FontWeight.Bold,

                                fontSize = 15.sp
                            )
                        }
                    }
                }

                else {

                    Card(

                        shape =
                            RoundedCornerShape(14.dp),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color(0xFF16A34A)
                            ),

                        elevation =
                            CardDefaults.cardElevation(
                                defaultElevation = 6.dp
                            )
                    ) {

                        Row(

                            modifier = Modifier
                                .padding(
                                    horizontal = 4.dp,
                                    vertical = 2.dp
                                ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(

                                text = "-",

                                color = Color.White,

                                fontWeight =
                                    FontWeight.Bold,

                                fontSize = 22.sp,

                                modifier = Modifier
                                    .clickable {

                                        onDecrease(
                                            cartItem
                                        )
                                    }
                                    .padding(
                                        horizontal = 10.dp,
                                        vertical = 4.dp
                                    )
                            )

                            Text(

                                text =
                                    cartItem.quantity
                                        .toString(),

                                color = Color.White,

                                fontWeight =
                                    FontWeight.Bold,

                                fontSize = 16.sp,

                                modifier =
                                    Modifier.padding(
                                        horizontal = 6.dp
                                    )
                            )

                            Text(

                                text = "+",

                                color = Color.White,

                                fontWeight =
                                    FontWeight.Bold,

                                fontSize = 20.sp,

                                modifier = Modifier
                                    .clickable {

                                        onIncrease(
                                            cartItem
                                        )
                                    }
                                    .padding(
                                        horizontal = 10.dp,
                                        vertical = 4.dp
                                    )
                            )
                        }
                    }
                }
                if (item.variants.isNotEmpty()) {

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(

                        text = "More Sizes Available",

                        fontSize = 11.sp,

                        color = Color.Gray

                    )

                    Text(

                        text = "Customisable",

                        fontSize = 11.sp,

                        color = Color(0xFF16A34A),

                        fontWeight = FontWeight.SemiBold

                    )

                }
            }
        }
    }

    if (showVariantSheet) {

        var selectedVariant by remember {

            mutableStateOf<VariantModel?>(
                item.variants.firstOrNull()
            )
        }

        var selectedAddons by remember {

            mutableStateOf(
                listOf<AddonModel>()
            )
        }

        ModalBottomSheet(

            onDismissRequest = {

                showVariantSheet = false
            },

            dragHandle = {},
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),

            sheetMaxWidth = 700.dp,

            containerColor = Color.White

        ) {

            Box(

                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)

            ) {

                Column(

                    modifier = Modifier
                        .fillMaxSize()

                ) {

                    Box(

                        modifier = Modifier
                            .padding(top = 10.dp)
                            .align(Alignment.CenterHorizontally)
                            .width(42.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.LightGray)
                    )

                    LazyColumn(

                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),

                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 20.dp,
                            bottom = 120.dp
                        )

                    ) {

                        item {

                            Text(

                                text = item.name,

                                fontSize = 28.sp,

                                fontWeight = FontWeight.Bold,

                                color = Color.Black
                            )

                            Spacer(
                                modifier = Modifier.height(20.dp)
                            )
                        }

                        if (item.variants.isNotEmpty()) {

                            item {

                                Text(

                                    text = "Choose Size",

                                    fontSize = 18.sp,

                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(
                                    modifier = Modifier.height(14.dp)
                                )
                            }

                            items(item.variants) { variant ->

                                val isSelected =

                                    selectedVariant?.name ==
                                            variant.name
                                val variantCartItem =

                                    CartData.items.find {

                                        it.item.name == item.name &&

                                                it.selectedVariant?.name ==
                                                variant.name
                                    }

                                val variantQty =

                                    variantCartItem?.quantity ?: 0

                                Card(

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 14.dp),

                                    shape =
                                        RoundedCornerShape(18.dp),

                                    border = BorderStroke(

                                        2.dp,

                                        if (isSelected)

                                            Color(0xFF16A34A)

                                        else

                                            Color(0xFFE5E7EB)
                                    ),

                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    )

                                ) {

                                    Row(

                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {

                                                selectedVariant = variant
                                            }
                                            .padding(18.dp),

                                        horizontalArrangement =
                                            Arrangement.SpaceBetween,

                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {

                                        Column {

                                            Text(

                                                text = variant.name,

                                                fontSize = 18.sp,

                                                fontWeight =
                                                    FontWeight.Bold
                                            )

                                            Spacer(
                                                modifier =
                                                    Modifier.height(4.dp)
                                            )

                                            Text(

                                                text =
                                                    "₹${variant.price}",

                                                color =
                                                    Color(0xFF16A34A),

                                                fontWeight =
                                                    FontWeight.Bold,

                                                fontSize = 16.sp
                                            )
                                        }

                                        if (variantQty > 0) {

                                            Row(

                                                verticalAlignment =
                                                    Alignment.CenterVertically,

                                                modifier = Modifier
                                                    .background(

                                                        Color(0xFF16A34A),

                                                        RoundedCornerShape(14.dp)
                                                    )
                                                    .padding(
                                                        horizontal = 6.dp,
                                                        vertical = 2.dp
                                                    )

                                            ) {

                                                IconButton(

                                                    onClick = {

                                                        val existing =

                                                            CartData.items.find {

                                                                it.item.name == item.name &&

                                                                        it.selectedVariant?.name ==
                                                                        variant.name
                                                            }

                                                        if (existing != null) {

                                                            CartData.decrease(existing)
                                                        }
                                                    }

                                                ) {

                                                    Text(

                                                        text = "-",

                                                        color = Color.White
                                                    )
                                                }

                                                val qty = variantQty

                                                Text(

                                                    text = qty.toString(),

                                                    color = Color.White,

                                                    fontWeight =
                                                        FontWeight.Bold
                                                )

                                                IconButton(

                                                    onClick = {

                                                        val existing =

                                                            CartData.items.find {

                                                                it.item.name == item.name &&

                                                                        it.selectedVariant?.name ==
                                                                        variant.name
                                                            }

                                                        if (existing != null) {

                                                            CartData.increase(existing)
                                                        }

                                                        else {
                                                            if (

                                                                CartData.currentRestaurantId.value.isNotEmpty()

                                                                &&

                                                                CartData.currentRestaurantId.value != restaurantId

                                                            ) {

                                                                CartData.clearCart()
                                                            }
                                                            if (

                                                                CartData.currentRestaurantId.value.isNotEmpty()

                                                                &&

                                                                CartData.currentRestaurantId.value != restaurantId

                                                            ) {

                                                                CartData.clearCart()
                                                            }

                                                            CartData.addToCart(

                                                                restaurantId =
                                                                    restaurantId,

                                                                restaurantName =
                                                                    restaurantName,

                                                                item = item,

                                                                selectedVariant =
                                                                    variant,

                                                                selectedAddons =
                                                                    selectedAddons
                                                            )
                                                        }
                                                    }

                                                ) {

                                                    Text(

                                                        text = "+",

                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                        else {

                                            Button(

                                                onClick = {

                                                    if (

                                                        CartData.currentRestaurantId.value.isNotEmpty()

                                                        &&

                                                        CartData.currentRestaurantId.value != restaurantId

                                                    ) {

                                                        CartData.clearCart()
                                                    }

                                                    selectedVariant = variant

                                                    CartData.addToCart(

                                                        restaurantId =
                                                            restaurantId,

                                                        restaurantName =
                                                            restaurantName,

                                                        item = item,

                                                        selectedVariant =
                                                            variant,

                                                        selectedAddons =
                                                            selectedAddons
                                                    )
                                                },

                                                colors =
                                                    ButtonDefaults.buttonColors(

                                                        containerColor =
                                                            Color.White
                                                    ),

                                                border = BorderStroke(

                                                    1.dp,
                                                    Color(0xFF16A34A)
                                                ),

                                                shape =
                                                    RoundedCornerShape(12.dp)

                                            ) {

                                                Text(

                                                    text = "ADD",

                                                    color =
                                                        Color(0xFF16A34A),

                                                    fontWeight =
                                                        FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }


                        if (item.addons.isNotEmpty()) {

                            item {

                                Spacer(
                                    modifier = Modifier.height(10.dp)
                                )

                                Text(

                                    text = "Addons",

                                    fontSize = 18.sp,

                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(
                                    modifier = Modifier.height(12.dp)
                                )
                            }

                            items(item.addons) { addon ->

                                val selected =

                                    selectedAddons.contains(addon)

                                Row(

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {

                                            selectedAddons =

                                                if (selected) {

                                                    selectedAddons
                                                        .filter {
                                                            it != addon
                                                        }
                                                }

                                                else {

                                                    selectedAddons + addon
                                                }
                                        }
                                        .padding(vertical = 14.dp),

                                    horizontalArrangement =
                                        Arrangement.SpaceBetween,

                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Column {

                                        Text(

                                            text = addon.name,

                                            fontWeight =
                                                FontWeight.SemiBold,

                                            fontSize = 16.sp
                                        )

                                        Spacer(
                                            modifier =
                                                Modifier.height(4.dp)
                                        )

                                        Text(

                                            text =
                                                "+ ₹${addon.price}",

                                            color = Color.Gray
                                        )
                                    }

                                    Checkbox(

                                        checked = selected,

                                        onCheckedChange = null
                                    )
                                }

                                HorizontalDivider(
                                    color = Color(0xFFF0F0F0)
                                )
                            }
                        }
                    }

                    val allVariantItems =

                        CartData.items.filter {

                            it.item.name == item.name
                        }

                    val finalPrice =

                        allVariantItems.sumOf {

                            val variantPrice =

                                it.selectedVariant?.price
                                    ?: item.price

                            val addonsPrice =

                                it.selectedAddons.sumOf { addon ->
                                    addon.price
                                }

                            (
                                    variantPrice +
                                            addonsPrice
                                    ) * it.quantity
                        }
                    Box(

                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp)

                    ) {

                        Button(

                            onClick = {

                                showVariantSheet = false
                            },

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),

                            shape =
                                RoundedCornerShape(18.dp),

                            colors = ButtonDefaults.buttonColors(

                                containerColor =
                                    Color(0xFF16A34A)
                            )

                        ) {

                            Text(

                                text =
                                    "ADD ITEM • ₹$finalPrice",

                                fontSize = 18.sp,

                                fontWeight = FontWeight.Bold,

                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    HorizontalDivider(

        modifier = Modifier
            .padding(
                horizontal = 16.dp,
                vertical = 2.dp
            ),

        color = Color(0xFFF0F0F0)
    )
}
@Composable
fun PremiumMenuSearchBar(

    value: TextFieldValue,

    onValueChange: (TextFieldValue) -> Unit

) {

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 10.dp
            ),

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )

    ) {

        BasicTextField(

            value = value,

            onValueChange =
                onValueChange,

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 18.dp,
                    vertical = 16.dp
                ),

            singleLine = true,

            textStyle = TextStyle(
                fontSize = 16.sp,
                color = Color.Black
            ),

            decorationBox = { innerTextField ->

                Row(

                    verticalAlignment =
                        Alignment.CenterVertically

                ) {

                    Icon(

                        imageVector =
                            Icons.Default.Search,

                        contentDescription = null,

                        tint = Color.Gray
                    )

                    Spacer(
                        modifier =
                            Modifier.width(10.dp)
                    )

                    Box {

                        if (
                            value.text.isEmpty()
                        ) {

                            Text(

                                text =
                                    "Search in menu",

                                color =
                                    Color.Gray,

                                fontSize = 15.sp
                            )
                        }

                        innerTextField()
                    }

                    Spacer(
                        modifier =
                            Modifier.weight(1f)
                    )

                    if (
                        value.text.isNotEmpty()
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.Close,

                            contentDescription = null,

                            tint = Color.Gray,

                            modifier = Modifier
                                .clickable {

                                    onValueChange(
                                        TextFieldValue("")
                                    )
                                }
                        )
                    }
                }
            }
        )
    }
}
@Composable
fun RecommendedItemCard(

    item: MenuItem,

    restaurantId: String,

    restaurantName: String,

    restaurantOpen: Boolean,

    categoryAvailable: Boolean = true
) {

    Card(

        modifier = Modifier
            .width(150.dp),

        shape =
            RoundedCornerShape(22.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 5.dp
            )
    ) {

        Column {

            Box {

                AsyncImage(

                    model = item.image,

                    contentDescription = null,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),

                    contentScale =
                        ContentScale.Crop
                )
            }

            Column(

                modifier = Modifier
                    .padding(14.dp)

            ) {

                Text(

                    text = item.name,

                    fontWeight =
                        FontWeight.Bold,

                    fontSize = 15.sp,

                    lineHeight = 20.sp,

                    maxLines = 2,

                    overflow =
                        TextOverflow.Ellipsis,

                    color = Color.Black
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                Text(

                    text =
                        item.description,

                    color = Color.Gray,

                    fontSize = 13.sp,

                    maxLines = 2,

                    overflow =
                        TextOverflow.Ellipsis
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceBetween,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(

                        text =
                            if (
                                item.variants.isNotEmpty()
                            ) {

                                "Starts ₹${item.variants.first().price}"

                            } else {

                                "₹${item.price}"
                            },

                        color =
                            Color(0xFF16A34A),

                        fontWeight =
                            FontWeight.Bold,

                        fontSize = 17.sp
                    )

                    val recommendedCartItem =

                        CartData.items.find {

                            it.item.name ==
                                    item.name
                        }

                    if (!restaurantOpen) {

                        Button(

                            onClick = {},

                            enabled = false

                        ) {

                            Text(

                                text = "CLOSED",

                                maxLines = 1,

                                fontSize = 9.sp,

                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else if (

                        !item.available ||

                        !categoryAvailable

                    ) {

                        Surface(

                            color = Color(0xFFD50000),

                            shape = RoundedCornerShape(50.dp)

                        ) {

                            Text(

                                text = "OUT OF STOCK",

                                color = Color.White,

                                fontWeight = FontWeight.Bold,

                                fontSize = 10.sp,

                                modifier = Modifier.padding(

                                    horizontal = 12.dp,

                                    vertical = 8.dp
                                )
                            )
                        }
                    }
                    else if (
                        recommendedCartItem == null
                    ) {

                        Card(

                            modifier = Modifier
                                .clickable {
                                    if (

                                        CartData.currentRestaurantId.value.isNotEmpty()

                                        &&

                                        CartData.currentRestaurantId.value != restaurantId

                                    ) {

                                        CartData.clearCart()
                                    }

                                    CartData.addToCart(

                                        restaurantId =
                                            restaurantId,

                                        restaurantName =
                                            restaurantName,

                                        item = item
                                    )
                                },

                            shape =
                                RoundedCornerShape(12.dp),

                            border = BorderStroke(
                                1.dp,
                                Color(0xFF16A34A)
                            ),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        Color.White
                                )
                        ) {

                            Text(

                                text = "ADD",

                                modifier = Modifier
                                    .padding(
                                        horizontal = 18.dp,
                                        vertical = 10.dp
                                    ),

                                color =
                                    Color(0xFF16A34A),

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }

                    else {

                        Card(

                            shape =
                                RoundedCornerShape(14.dp),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        Color(0xFF16A34A)
                                )

                        ) {

                            Row(

                                verticalAlignment =
                                    Alignment.CenterVertically,

                                modifier = Modifier
                                    .padding(
                                        horizontal = 6.dp,
                                        vertical = 2.dp
                                    )

                            ) {

                                Text(

                                    text = "-",

                                    color = Color.White,

                                    fontWeight =
                                        FontWeight.Bold,

                                    fontSize = 22.sp,

                                    modifier = Modifier
                                        .clickable {

                                            CartData.decrease(
                                                recommendedCartItem
                                            )
                                        }
                                        .padding(
                                            horizontal = 10.dp,
                                            vertical = 4.dp
                                        )
                                )

                                Text(

                                    text =
                                        recommendedCartItem.quantity
                                            .toString(),

                                    color = Color.White,

                                    fontWeight =
                                        FontWeight.Bold,

                                    fontSize = 16.sp,

                                    modifier =
                                        Modifier.padding(
                                            horizontal = 6.dp
                                        )
                                )

                                Text(

                                    text = "+",

                                    color = Color.White,

                                    fontWeight =
                                        FontWeight.Bold,

                                    fontSize = 20.sp,

                                    modifier = Modifier
                                        .clickable {

                                            CartData.increase(
                                                recommendedCartItem
                                            )
                                        }
                                        .padding(
                                            horizontal = 10.dp,
                                            vertical = 4.dp
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}