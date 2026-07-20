plugins {
    id("net.neoforged.moddev") version "2.0.142"
}

version = property("project_version")!!
group = property("project_group")!!

base {
    archivesName.set("craft-engine-neoforge-mod")
}

repositories {
    maven("https://maven.shedaniel.me/")
    maven("https://repo.momirealms.net/releases")
}

neoForge {
    version = property("neo_version") as String

    runs {
        create("client") {
            client()
        }
    }

    mods {
        create("craftengine") {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    compileOnly("me.shedaniel.cloth:cloth-config-neoforge:${property("cloth_version")}")
    val sparrowYaml = "net.momirealms:sparrow-yaml:1.0.8"
    jarJar(implementation(sparrowYaml)!!)
    add("additionalRuntimeClasspath", sparrowYaml)
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to project.version)
    }
}

tasks.jar {
    archiveFileName.set("craft-engine-neoforge-mod-${project.version}+mc1.21.8.jar")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}
