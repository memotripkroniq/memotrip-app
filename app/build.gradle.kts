plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)

    // Google Services (Google Login)
    id("com.google.gms.google-services")
}

// ===============================================
//  🔐 Load signing variables from gradle.properties
// ===============================================
val RELEASE_STORE_FILE: String by project
val RELEASE_STORE_PASSWORD: String by project
val RELEASE_KEY_ALIAS: String by project
val RELEASE_KEY_PASSWORD: String by project

android {
    namespace = "com.example.memotrip_kroniq"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.memotrip_kroniq"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ==========================================================
    //  🔐 SIGNING CONFIGS (debug + release)
    // ==========================================================
    signingConfigs {

        // Debug keystore — zabudovaný v Android Studiu
        getByName("debug") {
            storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }

        // Release keystore — tvoje produkční
        create("release") {
            storeFile = file(RELEASE_STORE_FILE)
            storePassword = RELEASE_STORE_PASSWORD
            keyAlias = RELEASE_KEY_ALIAS
            keyPassword = RELEASE_KEY_PASSWORD
        }
    }

    // Povolit BuildConfig
    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" // nebo přesně dle BOM
    }


    // ==========================================================
    //  🔥 PRODUCT FLAVORS (jen staging + production)
    // ==========================================================
    flavorDimensions += "environment"

    productFlavors {

        // 🟠 STAGING (Railway backend)
        create("staging") {
            dimension = "environment"
            buildConfigField(
                "String",
                "BASE_URL",
                "\"https://memotrip-bff-production.up.railway.app/\""
            )
        }

        // 🟢 PRODUCTION
        create("production") {
            dimension = "environment"
            buildConfigField(
                "String",
                "BASE_URL",
                "\"https://api.memotrip.app/\""
            )
        }
    }

    // Source sets – jen staging & prod
    //sourceSets {
    //    getByName("staging") { java.srcDir("src/staging/java") }
    //    getByName("production") { java.srcDir("src/production/java") }
    //}

    // ==========================================================
    //  🔥 BUILD TYPES
    // ==========================================================
    buildTypes {

        // 🌟 DEBUG = stagingDebug build type
        debug {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")

            // Debug má vlastní BASE_URL, aby bylo jasné logování
            buildConfigField(
                "String",
                "BASE_URL",
                "\"https://memotrip-bff-production.up.railway.app/\""
            )
        }

        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

kotlin {
    jvmToolchain(17)
}


dependencies {
    // ===== Jetpack Compose =====
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.compose.runtime)
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)

    // ===== Navigation =====
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.32.0")

    // ===== Media3 (video player) =====
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")

    // ===== Retrofit =====
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // ===== Datastore =====
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ===== Google Login / Firebase Auth =====
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-auth")

    // ===== Coil pro nahraní fotky Add Trip field =====
    implementation("io.coil-kt:coil-compose:2.6.0")

    // ===== Ktor core =====
    implementation("io.ktor:ktor-client-core:2.3.7")

    // ===== Engine (Android) =====
    implementation("io.ktor:ktor-client-okhttp:2.3.7")

    // =====JSON serialization =====
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

}
