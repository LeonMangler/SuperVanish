plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("com.gradleup.shadow") version "9.4.1"
}

group = "de.myzelyam"
version = "6.4.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.essentialsx.net/releases")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
    maven("https://jitpack.io")
    maven("https://repo.citizensnpcs.co/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.mikeprimm.com/")
    maven("https://libraries.minecraft.net")
    maven("https://repo.mvdw-software.com/content/groups/public/")
    maven("https://repo.repsy.io/mvn/jannyboy11/minecraft")
    maven("https://maven.pkg.github.com/Jannyboy11/InvSee-plus-plus")
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    implementation("ca.spottedleaf:concurrentutil:0.0.3") {
        exclude(group = "it.unimi.dsi", module = "fastutil")
    }
    implementation("org.bstats:bstats-bukkit:3.1.0")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    compileOnly("net.essentialsx:EssentialsX:2.21.1") {
        exclude(group = "org.spigotmc", module = "spigot-api")
        exclude(group = "org.bukkit", module = "bukkit")
        exclude(group = "org.bstats", module = "bstats-bukkit")
    }

    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
        exclude(group = "org.bukkit", module = "craftbukkit")
    }

    // Citizens
    compileOnly("net.citizensnpcs:citizensapi:2.0.30-SNAPSHOT")

    // TrailGUI
    compileOnly("com.github.SinnDevelopment:TrailGUI:37659dda03")

    // Dynmap
    compileOnly("us.dynmap:dynmap-api:3.1") {
        exclude(group = "org.bukkit", module = "bukkit")
    }

    // MVdWPlaceholderAPI
    compileOnly("be.maximvdw:MVdWPlaceholderAPI:3.1.1-SNAPSHOT") {
        exclude(group = "org.spigotmc", module = "spigot")
    }

    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.11.6")

    // Brigadier
    compileOnly("com.mojang:brigadier:1.0.18")

    // OpenInv
    compileOnly("com.github.Jikoo:OpenInv:5.1.12")

    // InvSee++
    compileOnly("com.janboerman.invsee:invsee-plus-plus_plugin:0.30.5-SNAPSHOT")
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("**/*.yml") {
        expand(project.properties)
    }
}

tasks.shadowJar {
    archiveClassifier.set("")

    relocate(
        "ca.spottedleaf.concurrentutil",
        "de.myzelyam.supervanish.libs.concurrentutil"
    )
    relocate(
        "org.bstats",
        "de.myzelyam.supervanish.libs.bstats"
    )
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

configurations.configureEach {
    resolutionStrategy.capabilitiesResolution.withCapability("org.spigotmc:spigot-api") {
        selectHighestVersion()
    }
    resolutionStrategy.capabilitiesResolution.withCapability("org.bukkit:bukkit") {
        selectHighestVersion()
    }
}
