plugins {
    id("java")
}

tasks.clean {
    delete("$rootDir/target")
}

subprojects {

    apply {
        plugin("java")
        plugin("java-library")
    }

    repositories {
        mavenCentral()
    }

    tasks.processResources {
        filteringCharset = "UTF-8"
    }

    tasks.jar {
        destinationDirectory.set(rootProject.file("target"))
    }
}
