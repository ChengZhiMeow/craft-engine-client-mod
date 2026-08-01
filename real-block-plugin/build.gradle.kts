version = property("project_version")!!
group = property("project_group")!!

base {
    archivesName.set("[默米-资源包引擎-真方块]craft-engine-real-block-paper")
}

val embeddedApi = configurations.create("embeddedApi") {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.momirealms.net/releases/")
}

dependencies {
    implementation(project(":real-block-api"))
    embeddedApi(project(":real-block-api"))
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

tasks.jar {
    dependsOn(":real-block-api:jar")
    from({ embeddedApi.map(::zipTree) }) {
        exclude("META-INF/MANIFEST.MF")
    }
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
        val forbiddenInternalNames = listOf(
            "net/momirealms/craftengine/realblock/BlockStateSettingsBridge",
            "net/momirealms/craftengine/realblock/CraftEngineBlockStateFactory",
            "net/momirealms/craftengine/realblock/RealBlockRegistryBridge",
            "net/momirealms/craftengine/realblock/RealBlockStateRuntime",
            "net/momirealms/craftengine/realblock/RegistryOccupancy",
            "net/momirealms/craftengine/realblock/StateDefinitionFactoryAccess",
            "net/momirealms/craftengine/realblock/StateHolderMixinSupport",
            "net/momirealms/craftengine/realblock/mixin/"
        )
        val jarFile = tasks.jar.get().archiveFile.get().asFile
        val violations = zipTree(jarFile)
            .matching { include("**/*.class") }
            .files
            .filter { classFile ->
                val classBytes = classFile.readBytes().toString(Charsets.ISO_8859_1)
                forbiddenInternalNames.any(classBytes::contains)
            }
        check(violations.isEmpty()) {
            "Real Block Paper plugin contains direct Ignite class linkage: " +
                    violations.joinToString { it.name }
        }
    }
}

val verifyNoViaLinkage = tasks.register("verifyNoViaLinkage") {
    group = "verification"
    description = "Keeps the Real Block Paper plugin independent from ViaVersion and ViaBackwards."
    dependsOn(tasks.jar)

    doLast {
        val forbiddenInternalNames = listOf(
            "com/viaversion/",
            "net/momirealms/craftengine/viacompat/"
        )
        val jarFile = tasks.jar.get().archiveFile.get().asFile
        val violations = zipTree(jarFile)
            .matching { include("**/*.class") }
            .files
            .filter { classFile ->
                val classBytes = classFile.readBytes().toString(Charsets.ISO_8859_1)
                forbiddenInternalNames.any(classBytes::contains)
            }
        check(violations.isEmpty()) {
            "Real Block Paper plugin contains Via compatibility linkage: " +
                    violations.joinToString { it.name }
        }
    }
}

val verifyEmbeddedApi = tasks.register("verifyEmbeddedApi") {
    group = "verification"
    description = "Ensures the Paper runtime JAR contains the public Real Block API."
    dependsOn(tasks.jar)

    doLast {
        val jarFile = tasks.jar.get().archiveFile.get().asFile
        val requiredEntries = setOf(
            "net/momirealms/craftengine/realblock/api/BlockCollision.class",
            "net/momirealms/craftengine/realblock/api/CollisionBox.class",
            "net/momirealms/craftengine/realblock/api/CraftEngineRealBlockApi.class"
        )
        val entries = zipTree(jarFile).matching {
            include("net/momirealms/craftengine/realblock/api/*.class")
        }.files.mapTo(mutableSetOf()) { file ->
            "net/momirealms/craftengine/realblock/api/${file.name}"
        }
        check(entries.containsAll(requiredEntries)) {
            "Real Block Paper plugin is missing public API classes: ${requiredEntries - entries}"
        }
    }
}

tasks.check {
    dependsOn(verifyNoDirectIgniteLinkage, verifyNoViaLinkage, verifyEmbeddedApi)
}
