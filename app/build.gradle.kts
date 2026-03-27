plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    tasks.withType<Test>{
        useJUnitPlatform()
    }

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
            isMinifyEnabled = false
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {
    // AndroidX Core & UI
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.gridlayout)
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.cardview:cardview:1.0.0")

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Activity and Fragment
    implementation("androidx.activity:activity-ktx:1.13.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")

    // QR Scanning
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")

    // FIX: Use compileOnly for Javadoc documentation.
    // This provides the SDK to the IDE tool without breaking the build/indexing.
    compileOnly(files("${android.sdkDirectory.path}/platforms/android-36/android.jar"))

    // Testing Dependencies
    testImplementation(libs.junit)
    testImplementation("io.mockk:mockk:1.14.9")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.7.0")
    androidTestImplementation(libs.activity)
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("io.mockk:mockk-android:1.14.9")

    // Essential UI & Logic Utilities
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.3.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    // Image Loading - Glide
    implementation("com.github.bumptech.glide:glide:5.0.5")
    annotationProcessor("com.github.bumptech.glide:compiler:5.0.5")

    // External Integrations & Logic
    implementation("com.google.zxing:core:3.5.4")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("com.opencsv:opencsv:5.12.0")
    implementation("commons-validator:commons-validator:1.10.1")
}

// This task is only required to generate a folder full of HTML documentation files
tasks.register<Javadoc>("generateJavadoc") {
    // 1. Point to your Java source files
    source = fileTree("src/main/java")

    // 2. Build the classpath including the Android SDK and all project dependencies
    val androidJar = files(android.bootClasspath)
    val projectDependencies = configurations.getByName("debugCompileClasspath")
    classpath = androidJar + projectDependencies

    // 3. Set output location
    setDestinationDir(file("${layout.buildDirectory.get()}/outputs/javadoc"))

    // 4. Configure Javadoc options
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
        links("https://developer.android.com/reference")
        encoding = "UTF-8"
        charSet = "UTF-8"
    }

    // 5. Exclude auto-generated files
    exclude("**/R.java", "**/BuildConfig.java")
}
