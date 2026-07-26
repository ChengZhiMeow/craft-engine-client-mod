version = property("project_version")!!
group = property("project_group")!!

base {
    archivesName.set("craft-engine-via-compatibility")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.momirealms.net/releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("net.momirealms:craft-engine-core:26.7.4")
    compileOnly("net.momirealms:craft-engine-bukkit:26.7.4")
    compileOnly("io.netty:netty-buffer:4.1.118.Final")
    compileOnly("io.netty:netty-transport:4.1.118.Final")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testImplementation("net.momirealms:craft-engine-core:26.7.4")
    testImplementation("com.google.code.gson:gson:2.13.2")
    testImplementation("io.netty:netty-buffer:4.1.118.Final")
    testImplementation("io.netty:netty-transport:4.1.118.Final")
    testImplementation("io.netty:netty-codec:4.1.118.Final")
    testImplementation("it.unimi.dsi:fastutil:8.5.15")
    testRuntimeOnly("org.joml:joml:1.10.8")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("paper-plugin.yml") {
        expand("version" to project.version)
    }
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
}

tasks.test {
    useJUnitPlatform()
}

val verifyNoDirectIgniteLinkage = tasks.register("verifyNoDirectIgniteLinkage") {
    group = "verification"
    description = "Prevents Paper plugin classes from linking directly to Ignite-isolated classes."
    dependsOn(tasks.jar)

    doLast {
        val forbiddenInternalName = "net/momirealms/craftengine/realblock/"
        val jarFile = tasks.jar.get().archiveFile.get().asFile
        val violations = zipTree(jarFile)
            .matching { include("**/*.class") }
            .files
            .filter { classFile ->
                classFile.readBytes().toString(Charsets.ISO_8859_1).contains(forbiddenInternalName)
            }
        check(violations.isEmpty()) {
            "Paper plugin contains direct Ignite class linkage: " +
                    violations.joinToString { it.name }
        }
    }
}

tasks.check {
    dependsOn(verifyNoDirectIgniteLinkage)
}
