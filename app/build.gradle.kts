plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "com.example.nohomeworkapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.nohomeworkapp"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    // ... your existing dependencies ...

    // Image loading (Coil) – used in the UI for preview (though we used bitmap directly, keep for flexibility)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Google ML Kit Text Recognition (on‑device OCR)
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")

    // Optional: if you need Chinese/Japanese/Korean text support, add:
    // implementation("com.google.android.gms:play-services-mlkit-text-recognition-chinese:16.0.0")

    // EXIF interface – to handle image orientation when loading from URI
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // Coroutines – needed for `viewModelScope` and `Dispatchers` (if not already present)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ViewModel integration with Compose – if you don't already have it
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    
    
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

}
