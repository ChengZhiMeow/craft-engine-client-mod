plugins {
    `java-library`
    `maven-publish`
}

version = property("project_version")!!
group = property("project_group")!!

base {
    archivesName.set("craft-engine-real-block-api")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "craft-engine-real-block-api"

            pom {
                name.set("CraftEngine Real Block API")
                description.set("Public collision API for the CraftEngine Real Block Paper plugin.")
                licenses {
                    license {
                        name.set("GNU General Public License v3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "reposiliteRepositoryReleases"
            url = uri("https://repo.xmxcraft.cn/releases")
            credentials {
                username = providers.gradleProperty("reposiliteRepositoryReleasesUsername")
                    .orElse(providers.environmentVariable("REPOSILITE_USERNAME"))
                    .orElse("xmxcraft")
                    .get()
                password = providers.gradleProperty("reposiliteRepositoryReleasesPassword")
                    .orElse(providers.environmentVariable("REPOSILITE_PASSWORD"))
                    .orNull
            }
            authentication {
                create<org.gradle.authentication.http.BasicAuthentication>("basic")
            }
        }
    }
}
