// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    kotlin("android") version "1.9.10" apply false
    alias(libs.plugins.androidApplication) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}