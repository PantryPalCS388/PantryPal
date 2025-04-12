buildscript {
    val agp_version by extra("8.2.0")  // Ensure you are using AGP 8.2.0
    repositories {
        google()  // Make sure this is included to access Firebase and Android SDKs
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:$agp_version")
        classpath("com.google.gms:google-services:4.4.2")  // Ensure this is the correct version of the plugin

    }
}

plugins {

    id("com.android.application") version "8.2.0" apply false  // Match the AGP version with the one above
    id("com.android.library") version "8.2.0" apply false  // Keep the version consistent with the app plugin
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false  // This is fine, as it aligns with Kotlin plugin version
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
    id("com.google.gms.google-services") version "4.4.2" apply false  // Ensure this is consistent with the dependency in the app-level build.gradle
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
