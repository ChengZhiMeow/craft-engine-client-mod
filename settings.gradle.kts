rootProject.name = "craft-engine-client-mod"
include(":neoforge")
include(":via-compatibility-plugin")
include(":real-block-api")
include(":real-block-plugin")
include(":ignite-mod")
pluginManagement {
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
        id("io.papermc.paperweight.userdev") version "2.0.0-SNAPSHOT"
        kotlin("jvm") version "2.1.20"
    }
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven("https://maven.neoforged.net/releases")
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}
