plugins {
    kotlin("jvm") version "1.7.10"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    repositories {
        mavenCentral()
        google()
    }
}
