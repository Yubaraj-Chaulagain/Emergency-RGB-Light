plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.rgb.emergencylight"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rgb.emergencylight"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
