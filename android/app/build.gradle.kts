import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val vendorSdkEnabled = providers.gradleProperty("vendorSdkEnabled")
    .orElse(providers.environmentVariable("NAPSSP_VENDOR_SDK_ENABLED"))
    .map { value ->
        value.equals("true", ignoreCase = true) ||
            value == "1" ||
            value.equals("yes", ignoreCase = true)
    }
    .getOrElse(false)

android {
    namespace = "com.gwangy.nassspandroidsample"
    compileSdk = 35

    // [최종 해결] JVM Toolchain을 사용하여 전체 컴파일 환경을 17로 강제 고정
    kotlin {
        jvmToolchain(17)
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
        buildConfig = true
    }

    val localProperties = Properties()
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }

    defaultConfig {
        applicationId = "com.gwangy.nassspandroidsample"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "NAP_MEDIA_KEY", "\"${localProperties.getProperty("napssp.media_key") ?: "10771"}\"")
        buildConfigField("String", "NAP_ADUNIT_BANNER", "\"${localProperties.getProperty("napssp.adunit_banner") ?: "104704"}\"")
        buildConfigField("boolean", "VENDOR_SDK_ENABLED", vendorSdkEnabled.toString())
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation(platform("androidx.compose:compose-bom:2026.04.01"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("io.github.nasmedia-tech:admixer-ssp:1.0.23")
    implementation("com.google.android.gms:play-services-ads-identifier:18.9.0")

    // Mediation
    implementation("io.github.nasmedia-tech:admixer-admanager:1.0.14")
    implementation("io.github.nasmedia-tech:admixer-adfit:1.0.10")
    implementation("io.github.nasmedia-tech:admixer-pangle:1.0.10")
    implementation("com.pangle.global:pag-sdk:8.0.0.4")
    implementation("io.github.nasmedia-tech:admixer-applovin:1.0.8")
    implementation("io.github.nasmedia-tech:admixer-unity:1.0.6")
}
