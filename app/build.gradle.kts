plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.productscanner"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.productscanner"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    buildFeatures {
        viewBinding = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    dependencies {
        implementation ("androidx.core:core-ktx:1.10.0")
        implementation ("androidx.appcompat:appcompat:1.6.1")
        implementation ("com.google.android.material:material:1.9.0")
        implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
        implementation ("androidx.camera:camera-core:1.2.3")
        implementation ("androidx.camera:camera-camera2:1.2.3")
        implementation ("androidx.camera:camera-lifecycle:1.2.3")
        implementation ("androidx.camera:camera-view:1.2.3")
        implementation ("com.google.firebase:firebase-storage-ktx:20.1.0")
        implementation ("com.google.firebase:firebase-firestore-ktx:24.3.0")
        implementation ("com.squareup.retrofit2:retrofit:2.9.0")
        implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
        testImplementation ("junit:junit:4.13.2")
        androidTestImplementation ("androidx.test.ext:junit:1.1.5")
        androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    }

    // Retrofit for networking
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")

    //GSON Converter
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // Glide for image loading
    implementation ("com.github.bumptech.glide:glide:4.16.0")

}