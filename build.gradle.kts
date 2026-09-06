// Top-level build file — configuration common to all sub-projects goes here.

buildscript {
    // AGP 9.x has built-in Kotlin support and ships with an older Kotlin Gradle
    // Plugin by default. Pin a newer, consistent Kotlin + KSP toolchain here.
    // Keep these in sync with `kotlin` / `ksp` in gradle/libs.versions.toml.
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.3.11")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
