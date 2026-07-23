rootProject.name = "craft-engine-client-mod"
include(":neoforge")
include(":compatibility-plugin")
pluginManagement {
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
        kotlin("jvm") version "2.1.20"
    }
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven("https://maven.neoforged.net/releases")
    }
}
