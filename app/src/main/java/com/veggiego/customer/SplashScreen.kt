package com.veggiego.customer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.ui.text.style.TextAlign

@Composable
fun SplashScreen(
    navController: NavController
) {

    var startAnimation by remember {
        mutableStateOf(false)
    }

    val alphaAnim by animateFloatAsState(

        targetValue =
            if (startAnimation) 1f else 0f,

        animationSpec =
            tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            ),

        label = ""
    )

    LaunchedEffect(Unit) {

        startAnimation = true

        delay(2500)

        navController.navigate("home") {

            popUpTo("splash") {
                inclusive = true
            }
        }
    }

    Box(

        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E7D32)),

        contentAlignment = Alignment.Center

    ) {

    }
}