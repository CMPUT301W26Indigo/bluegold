plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
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
    implementation(libs.recyclerview)
    implementation(libs.cardview)

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Activity and Fragment
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)

    // QR Scanning
    implementation(libs.zxing.android.embedded)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)

    // FIX: Use compileOnly for Javadoc documentation.
    // This provides the SDK to the IDE tool without breaking the build/indexing.
    compileOnly(files("${android.sdkDirectory.path}/platforms/android-36/android.jar"))

    // Testing Dependencies
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(libs.activity)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.rules)
    androidTestImplementation(libs.mockk.android)

    // Essential UI & Logic Utilities
    implementation(libs.core.ktx)
    implementation(libs.coordinatorlayout)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Image Loading - Glide
    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    // External Integrations & Logic
    implementation(libs.core)
    implementation(libs.play.services.location)
    implementation(libs.gson)
    implementation(libs.opencsv)
    implementation(libs.commons.validator)
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
