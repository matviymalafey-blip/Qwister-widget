plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "ua.qwister.schoolwidget"
    compileSdk = 36
    defaultConfig {
        applicationId = "ua.qwister.schoolwidget"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "0.3.0"
    }
}
