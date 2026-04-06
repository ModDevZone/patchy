import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.copyright
import org.jetbrains.gradle.ext.runConfigurations

plugins {
    java
    idea
    application
    id("com.gradleup.shadow") version "9.4.1"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.4.1"
}

group = "zone.moddev.patchy"

application {
    applicationName = "Patchy"
    mainClass = "zone.moddev.patchy.Patchy"
    executableDir = "run"
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
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
        name = "Jitpack"
        url = uri("https://jitpack.io")
    }
}

val jdaVersion = "6.3.1"
val jetbrainsAnnotations = "26.1.0"
val jacksonDatabindVersion = "3.1.1"
val jacksonAnnotationsVersion = "2.21"
val dotenvVersion = "3.2.0"
val logbackVersion = "1.5.32"
val slf4jVersion = "2.0.17"
val gsonVersion = "2.13.2"
val jdbi3CoreVersion = "3.52.0"
val jdbi3SqlObjectVersion = "3.52.0"
val sqliteVersion = "3.51.3.0"
val hikariVersion = "7.0.2"
val fastUtilVersion = "8.5.18"

dependencies {
    implementation("net.dv8tion:JDA:${jdaVersion}")
    implementation("org.jetbrains:annotations:${jetbrainsAnnotations}")
    //TODO Try and remove this dependency, JDA has replaced it's features so it is discontinued and it has a few minor security flaws
    implementation("club.minnced:discord-webhooks:0.8.4")
    implementation("tools.jackson.core:jackson-databind:${jacksonDatabindVersion}")
    implementation("com.fasterxml.jackson.core:jackson-annotations:${jacksonAnnotationsVersion}")
    implementation("io.github.cdimascio:dotenv-java:${dotenvVersion}")
    implementation("ch.qos.logback:logback-classic:${logbackVersion}")
    implementation("org.slf4j:slf4j-api:${slf4jVersion}")
    implementation("com.google.code.gson:gson:${gsonVersion}")
    implementation("org.jdbi:jdbi3-core:${jdbi3CoreVersion}")
    implementation("org.jdbi:jdbi3-sqlobject:${jdbi3SqlObjectVersion}")
    implementation("org.xerial:sqlite-jdbc:${sqliteVersion}")
    implementation("com.zaxxer:HikariCP:${hikariVersion}")
    implementation("it.unimi.dsi:fastutil:${fastUtilVersion}")
}

idea {
    project {
        settings {
            copyright {
                val patchyCopyrightProfileName = "Patchy"
                useDefault = patchyCopyrightProfileName

                profiles.create(patchyCopyrightProfileName) {
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
