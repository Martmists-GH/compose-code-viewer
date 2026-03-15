import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.compose.ComposeBuildConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    kotlin("plugin.compose") version "2.3.0"
    id("org.jetbrains.compose") version "1.10.0"

    `maven-publish`
    id("com.vanniktech.maven.publish") version "0.31.0"
}

group = "com.martmists.codeviewer"
version = "1.0.3"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("org.jetbrains.compose.ui:ui:${ComposeBuildConfig.composeVersion}")
    implementation("org.jetbrains.compose.runtime:runtime:${ComposeBuildConfig.composeVersion}")
    implementation("org.jetbrains.compose.foundation:foundation:${ComposeBuildConfig.composeVersion}")
    implementation("org.jetbrains.compose.components:components-resources:${ComposeBuildConfig.composeVersion}")
    implementation("org.jetbrains.compose.animation:animation:${ComposeBuildConfig.composeVersion}")
    implementation("org.jetbrains.compose.animation:animation-graphics:${ComposeBuildConfig.composeVersion}")
    implementation("org.jetbrains.compose.components:components-resources:${ComposeBuildConfig.composeVersion}")
    implementation("org.jetbrains.compose.material3:material3:${ComposeBuildConfig.composeMaterial3Version}")

    implementation("org.jetbrains.kotlin:kotlin-compiler:2.3.0")

    runtimeOnly(compose.desktop.currentOs)
}

kotlin {
    jvmToolchain(21)
}

tasks {
    withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xexplicit-backing-fields",
                "-Xcontext-parameters",
            )
        }
    }
}

if (findProperty("mavenToken") != null) {
    fun MavenPom.configure() {
        name = "Compose Code Viewer"
        description = "Code Viewer library for Compose Multiplatform"
        url = "https://github.com/martmists-gh/compose-code-viewer"

        licenses {
            license {
                name = "3-Clause BSD NON-AI License"
                url = "https://github.com/non-ai-licenses/non-ai-licenses/blob/main/NON-AI-BSD3"
                distribution = "repo"
            }
        }

        developers {
            developer {
                id = "Martmists"
                name = "Martmists"
                url = "https://github.com/martmists-gh"
            }
        }

        scm {
            url = "https://github.com/martmists-gh/compose-code-viewer"
        }
    }

    publishing {
        repositories {
            maven {
                name = "Releases"
                url = uri("https://maven.martmists.com/releases")
                credentials {
                    username = "admin"
                    password = project.ext["mavenToken"]!! as String
                }

            }
        }

        publications {
            withType<MavenPublication> {
                version = project.version as String
                pom {
                    configure()
                }
            }
        }
    }

    mavenPublishing {
        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
        coordinates(group as String, name, version as String)
        signAllPublications()

        pom {
            configure()
        }
    }
}
