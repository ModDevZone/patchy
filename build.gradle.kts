plugins {
    java
    idea
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "zone.moddev"
version = "1.0"

application {
    applicationName = "yuki"
    mainClass = "zone.moddev.yuki.Yuki"
    applicationDefaultJvmArgs = listOf("--enable-preview")
    executableDir = "run"
}

val jdaVersion = "6.0.0-rc.2"
val chewUtilsVersion = "2.1"
val discordWebhooksVersion = "0.8.4"
val logbackVersion = "1.5.18"
val lombokVersion = "1.18.38"
val fastUtilVersion = "8.5.12"
val gsonVersion = "2.13.1"
val curseforgeApiVersion = "2.3.3"
val args4jVersion = "2.33"
val botCommandsVersion = "3.0.0-beta.3"
val configurateVersion = "4.2.0"

val sqliteJdbcVersion = "3.50.3.0"
val flywayCoreVersion = "11.10.4"
val jdbi3CoreVersion = "3.49.5"
val jdbi3SqlObjectVersion = "3.49.5"
val jetbrainsAnnotationsVersion = "26.0.2"

repositories {
    mavenCentral()
    maven {
        name = "fabric"
        url  = uri("https://maven.fabricmc.net")
    }

    maven {
        name = "jda-chewtils"
        url = uri("https://m2.chew.pro/snapshots")
    }

    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    //TODO Go through these and see what we didn't actually need...
    // ----- Core Bot Dependencies ----- //
    implementation("net.dv8tion:JDA:${jdaVersion}")
    implementation("com.matyrobbrt:JDA-Chewtils:$chewUtilsVersion")
    implementation("club.minnced:discord-webhooks:$discordWebhooksVersion")
    implementation("org.projectlombok:lombok:$lombokVersion")
    implementation("it.unimi.dsi:fastutil:$fastUtilVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("io.github.matyrobbrt:curseforgeapi:$curseforgeApiVersion")
    implementation("args4j:args4j:$args4jVersion")
    implementation("io.github.freya022:BotCommands:$botCommandsVersion")

    // ----- Logging & Configuration ----- //
    implementation("ch.qos.logback:logback-classic:${logbackVersion}")
    implementation("org.spongepowered:configurate-hocon:${configurateVersion}")

    // ----- Database & Storage ----- //
    implementation("org.xerial:sqlite-jdbc:$sqliteJdbcVersion")
    implementation("org.flywaydb:flyway-core:$flywayCoreVersion")
    implementation("org.jdbi:jdbi3-core:$jdbi3CoreVersion")
    implementation("org.jdbi:jdbi3-sqlobject:$jdbi3SqlObjectVersion")
    compileOnly("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    // Set this to the version of Java you want to use,
    // the minimum required for JDA is 1.8
    sourceCompatibility = "21"
    targetCompatibility = "21"
}