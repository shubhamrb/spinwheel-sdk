plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.shubh.spinwheel.sample"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.shubh.spinwheel.sample"
        minSdk = 21
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation(project(":spinwheel"))
    implementation("androidx.appcompat:appcompat:1.8.0")
}
