plugins {

    id("com.android.application")

    id("org.jetbrains.kotlin.android")

    id("com.google.gms.google-services")
}

android {

    namespace = "com.veggiego.customer"

    compileSdk = 35

    defaultConfig {

        applicationId =
            "com.veggiego.customer"

        minSdk = 24

        targetSdk = 35

        versionCode = 12

        versionName = "2.1"

        buildConfigField(
            "String",
            "GOOGLE_ROUTES_API_KEY",
            "\"${project.findProperty("GOOGLE_ROUTES_API_KEY")?.toString() ?: ""}\""
        )

        manifestPlaceholders["GOOGLE_ROUTES_API_KEY"] =
            project.findProperty("GOOGLE_ROUTES_API_KEY")?.toString() ?: ""

        buildConfigField(
            "String",
            "GOOGLE_MAPS_API_KEY",
            "\"${project.findProperty("GOOGLE_MAPS_API_KEY")?.toString() ?: ""}\""
        )

        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] =
            project.findProperty("GOOGLE_MAPS_API_KEY")?.toString() ?: ""
    }
    buildTypes {

        release {

            isMinifyEnabled = false
        }
    }

    buildFeatures {

        compose = true

        buildConfig = true
    }

    composeOptions {

        kotlinCompilerExtensionVersion =
            "1.5.1"
    }

    kotlinOptions {

        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(
        "androidx.core:core-ktx:1.12.0"
    )

    implementation(
        "androidx.lifecycle:lifecycle-runtime-ktx:2.6.2"
    )

    implementation(
        "androidx.activity:activity-compose:1.8.2"
    )

    implementation(
        platform(
            "androidx.compose:compose-bom:2024.02.00"
        )
    )

    implementation(
        "androidx.compose.ui:ui"
    )

    implementation(
        "androidx.compose.material3:material3"
    )

    implementation(
        "androidx.compose.material:material-icons-extended"
    )

    implementation(
        "androidx.navigation:navigation-compose:2.7.7"
    )

    implementation(
        "io.coil-kt:coil-compose:2.5.0"
    )

    implementation(
        platform(
            "com.google.firebase:firebase-bom:33.1.0"
        )
    )

    implementation(
        "com.google.firebase:firebase-firestore-ktx"
    )

    implementation(
        "com.google.firebase:firebase-auth-ktx"
    )

    implementation(
        "com.google.firebase:firebase-messaging-ktx"
    )
    implementation("androidx.compose.animation:animation")

    implementation(
        "com.squareup.retrofit2:retrofit:2.9.0"
    )

    implementation(
        "com.squareup.retrofit2:converter-gson:2.9.0"
    )

    implementation(
        "com.google.code.gson:gson:2.10.1"
    )

    implementation(
        "com.google.android.gms:play-services-location:21.2.0"
    )

    implementation(
        "com.google.maps.android:maps-compose:4.3.0"
    )

    implementation(
        "com.google.android.gms:play-services-maps:18.2.0"
    )

    implementation(
        "com.google.maps.android:android-maps-utils:3.8.2"
    )

    implementation(
        "com.google.android.libraries.places:places:3.5.0"
    )

    implementation(
        "com.google.android.material:material:1.12.0"
    )
}