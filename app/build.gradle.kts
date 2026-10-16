plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.lastnationstanding.game"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.lastnationstanding.game"
        minSdk = 29
        targetSdk = 35
        versionCode = 11
        versionName = "1.0.0-rc1"
    }
    buildTypes {
        release { isMinifyEnabled = false }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
