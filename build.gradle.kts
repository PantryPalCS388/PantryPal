buildscript {
    val agp_version by extra("8.6.0")
}
plugins {
    id("com.android.application") version "8.6.0" apply false
    id("com.android.library") version "7.4.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.21"
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}