plugins {
    alias(libs.plugins.android.application)
    //alias(libs.plugins.google.services)
    // The commented out id() was what Firebase said to add,
    //  but caused a build error.
    //id("com.android.application") // This line was commented out in lab 05
    id("com.google.gms.google-services")
}

android {
    namespace = "com.eventlottery"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.eventlottery"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    // AndroidX Core & UI
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.gridlayout)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Activity and Fragment
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // QR Scanning
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
    implementation("com.google.firebase:firebase-analytics")

    // The rest of the Firebase dependencies are from the Figma transfer
    // These files may or may not be necessary.
//    implementation("com.google.firebase:firebase-auth")
//    implementation("com.google.firebase:firebase-firestore")
//    implementation("com.google.firebase:firebase-storage")
//    implementation("com.google.firebase:firebase-messaging")
//    implementation(libs.activity)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Below this comment is all dependencies from the build.gradle file.
    // These may be ones generated from the Figma transfer and may not be
    // necessary.

//    implementation("androidx.core:core-ktx:1.12.0")
//    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
//    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
//    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
//    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
//
//    // Using annotationProcessor for Glide since this is a Java project
//    implementation("com.github.bumptech.glide:glide:4.16.0")
//    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
//
//    implementation("com.google.zxing:core:3.5.2")
//    implementation("com.google.android.gms:play-services-location:21.1.0")
//    implementation("com.google.android.gms:play-services-maps:18.2.0")
//    implementation("com.google.code.gson:gson:2.10.1")
//    implementation("com.squareup.retrofit2:retrofit:2.9.0")
//    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
//    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
//    implementation("com.opencsv:opencsv:5.9")
//    implementation("commons-validator:commons-validator:1.9.0")
//    implementation("androidx.work:work-runtime-ktx:2.9.0")
//    implementation("androidx.datastore:datastore-preferences:1.0.0")
//    implementation("com.jakewharton.timber:timber:5.0.1")
//
//    androidTestImplementation("androidx.test:runner:1.5.2")
//    androidTestImplementation("androidx.test:rules:1.5.0")
//    testImplementation("io.mockk:mockk:1.13.8")
//    androidTestImplementation("io.mockk:mockk-android:1.13.8")
//    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
