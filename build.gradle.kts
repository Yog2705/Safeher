//top level
plugins {
    id("com.android.application") version "8.3.1" apply false  // Use latest AGP version (supports compileSdk 34+)
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
}

buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.3.15")
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
