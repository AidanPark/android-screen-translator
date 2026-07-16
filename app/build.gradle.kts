import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)
}

val admobAppId = if (gradle.startParameter.taskNames.any { it.contains("Debug") }) {
    "ca-app-pub-xxxxxxxxxxxxxxxx~xxxxxxxxxx"
} else {
    "ca-app-pub-xxxxxxxxxxxxxxxx~xxxxxxxxxx"
}

android {
    namespace = "com.galaxy.airviewdictionary"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.galaxy.airviewdictionary"
        minSdk = 26
        targetSdk = 36
        versionCode = 20501
        versionName = "2.5.1"
        manifestPlaceholders["ADMOB_APP_ID"] = admobAppId
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    bundle {
        abi { enableSplit = true } // ABI별로 APK를 나누기
        language { enableSplit = true } // 언어별로 APK를 나누기
        density { enableSplit = true } // 해상도별로 APK를 나누기
    }
    signingConfigs {
        create("release") {
            keyAlias = project.property("KEY_ALIAS") as String
            keyPassword = project.property("KEY_PASSWORD") as String
            storeFile = file(rootProject.file(project.property("KEYSTORE_FILE") as String))
            storePassword = project.property("KEY_PASSWORD") as String
        }
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            firebaseCrashlytics {
                mappingFileUploadEnabled = true
            }
            signingConfig = signingConfigs.getByName("release")
        }
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.admob)
    implementation(libs.app.review)
//    implementation(libs.app.update)

    implementation(libs.androidx.appcompat)

    // Architecture Components
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.service)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.compiler)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.dynamicanimation)
    implementation(libs.compose.foundation)

    implementation(libs.androidx.annotation)

    // datastore
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)

    // Hilt
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // screen spec
    implementation(libs.androidx.window)

    // theme
    implementation(libs.material)
    // icons
    implementation(libs.material.icons.extended)

    implementation(libs.google.mlkit.text.recognition)
    implementation(libs.google.mlkit.text.recognition.chinese)
    implementation(libs.google.mlkit.text.recognition.devanagari)
    implementation(libs.google.mlkit.text.recognition.japanese)
    implementation(libs.google.mlkit.text.recognition.korean)
//    implementation(libs.google.gms.mlkit.text.recognition)
//    implementation(libs.google.gms.mlkit.text.recognition.chinese)
//    implementation(libs.google.gms.mlkit.text.recognition.devanagari)
//    implementation(libs.google.gms.mlkit.text.recognition.japanese)
//    implementation(libs.google.gms.mlkit.text.recognition.korean)
    implementation(libs.google.mlkit.language.id)

    implementation(libs.google.mlkit.translate)
    implementation(libs.deepl.api)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.appcheck)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)

    implementation(libs.gson)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.squareup.retrofit2.retrofit)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.squareup.retrofit2.converter.gson)
    implementation(libs.coil.compose)
    implementation(libs.reorderable)
    implementation(libs.exoplayer)

    implementation(libs.timber)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.monitor)
}










