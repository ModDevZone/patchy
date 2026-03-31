import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.copyright

plugins {
    java
    idea
    application
    id("com.gradleup.shadow") version "9.3.1"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.3"
}

group = "zone.moddev.patchy"

application {
    applicationName = "Patchy"
    mainClass = "zone.moddev.patchy.Patchy"
    executableDir = "run"
}

tasks.shadowJar {
    archiveBaseName.set("patchy")
}

repositories {
    mavenCentral()

    maven {
        name = "jda-chewtils"
        url = uri("https://m2.chew.pro/snapshots")
    }

    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }
}

val jdaVersion = "6.3.1"
val logbackVersion = "1.5.32"
val jetbrainsAnnotationsVersion = "26.0.2"
val jacksonVersion = "3.1.0"
val gsonVersion = "2.13.2"

dependencies {
    implementation("net.dv8tion:JDA:${jdaVersion}")
    implementation("ch.qos.logback:logback-classic:${logbackVersion}")
    implementation("tools.jackson.core:jackson-databind:${jacksonVersion}")
    implementation("tools.jackson.core:jackson-annotations:${jacksonVersion}")
    implementation("com.google.code.gson:gson:${gsonVersion}")
    compileOnly("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
}

idea {
    project {
        settings {
            copyright {
                val yukiCopyrightProfileName = "Patchy"
                useDefault = yukiCopyrightProfileName

                profiles.create(yukiCopyrightProfileName) {
                    notice = file("gradle/copyright-header.txt").readText(Charsets.UTF_8)
                }
            }
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    jar {
        manifest {
            attributes["Main-Class"] = "zone.moddev.patchy.Patchy"
        }
    }

    build {
        dependsOn(shadowJar)
    }
}