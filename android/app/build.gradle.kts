import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.gwangy.nassspandroidsample"
    compileSdk = 35

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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

        // 보안 키 주입 (local.properties에 없으면 기본값 사용)
        buildConfigField("String", "NAP_MEDIA_KEY", "\"${localProperties.getProperty("napssp.media_key") ?: "10771"}\"")
        buildConfigField("String", "NAP_ADUNIT_BANNER", "\"${localProperties.getProperty("napssp.adunit_banner") ?: "104704"}\"")
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

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("io.github.nasmedia-tech:admixer-ssp:1.0.21")
    implementation("com.google.android.gms:play-services-ads-identifier:18.3.0")

    // Optional mediations from the vendor guide:
    // implementation("io.github.nasmedia-tech:admixer-admanager:1.0.14")
    // implementation("io.github.nasmedia-tech:admixer-adfit:1.0.10")
    // implementation("io.github.nasmedia-tech:admixer-pangle:1.0.10")
    // implementation("io.github.nasmedia-tech:admixer-applovin:1.0.8")
    // implementation("io.github.nasmedia-tech:admixer-unity:1.0.6")
}
