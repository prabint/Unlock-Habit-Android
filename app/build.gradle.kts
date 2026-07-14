import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.baselineprofile)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

configure<ApplicationExtension> {
    namespace = "prabin.timsina.unlockhabit"
    compileSdk = 37
    defaultConfig {
        applicationId = "prabin.timsina.unlockhabit"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            val keystoreProperties = Properties()
            if (keystorePropertiesFile.exists()) {
                FileInputStream(keystorePropertiesFile).use {
                    keystoreProperties.load(it)
                }
            }

            val storeFilePath = System.getenv("LSR_RELEASE_STORE_FILE")
                ?: keystoreProperties.getProperty("LSR_RELEASE_STORE_FILE")

            val keyAliasValue = System.getenv("LSR_RELEASE_KEY_ALIAS")
                ?: keystoreProperties.getProperty("LSR_RELEASE_KEY_ALIAS")

            val storePasswordValue = System.getenv("LSR_RELEASE_STORE_PASSWORD")
                ?: keystoreProperties.getProperty("LSR_RELEASE_STORE_PASSWORD")

            val keyPasswordValue = System.getenv("LSR_RELEASE_KEY_PASSWORD")
                ?: keystoreProperties.getProperty("LSR_RELEASE_KEY_PASSWORD")

            if (
                storeFilePath != null &&
                keyAliasValue != null &&
                storePasswordValue != null &&
                keyPasswordValue != null
            ) {
                storeFile = file(storeFilePath)
                keyAlias = keyAliasValue
                storePassword = storePasswordValue
                keyPassword = keyPasswordValue
            }
        }
    }
    buildTypes {
        debug {
            isDebuggable = true
            versionNameSuffix = ".debug"
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Baseline
    "baselineProfile"(project(":baselineprofile"))
    implementation(libs.androidx.profileinstaller)

    // Data Store
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)

    // Debugging
    implementation(libs.timber)

    // Hilt
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Material
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material3)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))

    // Accompanist
    implementation(libs.accompanist.permissions)

    // Navigation
    implementation(libs.androidx.lifecycle.viewmodel.navigation)
    implementation(libs.androidx.material.adaptive.navigation.suite.android)
    implementation(libs.androidx.material.navigation)
    implementation(libs.androidx.navigation.runtime)
    implementation(libs.androidx.navigation.ui)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.jetbrains.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.ui.tooling)
}