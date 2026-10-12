plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

// Coordinates users will put in their build.gradle:  implementation("com.shubh.spinwheel:spinwheel:0.0.1")
// Change GROUP to something you own (e.g. io.github.<username> or com.smytten).
val sdkGroup = "com.shubh.spinwheel"
val sdkArtifact = "spinwheel"
val sdkVersion = "0.0.1"

android {
    namespace = "com.shubh.spinwheel"
    compileSdk = 37
    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        release { isMinifyEnabled = false }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.19.1")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = sdkGroup
            artifactId = sdkArtifact
            version = sdkVersion
            afterEvaluate { from(components["release"]) }
            pom {
                name.set("SpinWheel SDK")
                description.set("A customizable spin-the-wheel reward view for Android.")
                url.set("https://github.com/YOUR_USER/spinwheel-sdk")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer { id.set("YOUR_USER"); name.set("Your Name"); email.set("you@example.com") }
                }
                scm {
                    connection.set("scm:git:https://github.com/YOUR_USER/spinwheel-sdk.git")
                    url.set("https://github.com/YOUR_USER/spinwheel-sdk")
                }
            }
        }
    }
    repositories {
        // ./gradlew :spinwheel:publishReleasePublicationToLocalRepoRepository  -> build/repo (for testing / Central bundle)
        maven { name = "LocalRepo"; url = uri(rootProject.layout.buildDirectory.dir("repo")) }
    }
}
