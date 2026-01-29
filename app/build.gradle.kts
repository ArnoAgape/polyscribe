import com.android.build.gradle.BaseExtension
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.gms.google-services")
    id("org.sonarqube") version "7.2.0.6526"
}

android {
    namespace = "com.arnoagape.polyscribe"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.arnoagape.polyscribe"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        compose = true
    }
}

// Test reports with JaCoCo
val androidExtension = extensions.getByType<BaseExtension>()
val jacocoTestReport by tasks.registering(JacocoReport::class) {
    dependsOn("testDebugUnitTest", "createDebugCoverageReport")
    group = "Reporting"
    description = "Generate Jacoco coverage reports"

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val debugTree = fileTree(layout.buildDirectory.dir("/tmp/kotlin-classes/debug"))
    val mainSrc = androidExtension.sourceSets.getByName("main").java.srcDirs

    classDirectories.setFrom(debugTree)
    sourceDirectories.setFrom(files(mainSrc))
    executionData.setFrom(fileTree(layout.buildDirectory) {
        include("**/*.exec", "**/*.ec")
    })
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.material)
    implementation(libs.googleid)

    // Allows API < 26
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Firebase & Firestore
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.ui.auth)
    implementation (libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.ui.storage)
    implementation(libs.play.services.auth)
    implementation(libs.datastore.preferences)
    implementation(libs.androidx.datastore)
    implementation (libs.androidx.credentials)

    //DI
    implementation(libs.hilt)
    implementation(libs.material3)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.accompanist.permissions)
    testImplementation(libs.datastore.preferences)

    // Images
    implementation(libs.coil.compose)

    // Tests
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation (libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

}