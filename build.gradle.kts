plugins {
    java
}

group = "de.myzelyam"
version = "6.2.6-2"

repositories {
    mavenCentral()
    maven {
        name = "MVdW Public Repositories"
        url = uri("https://repo.mvdw-software.com/content/groups/public/")
        content {
            includeGroup("be.maximvdw")
        }
    }
    maven {
        name = "spigotmc-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        content {
            includeGroup("org.spigotmc")
        }
    }
    maven {
        name = "eseentials-repo"
        url = uri("https://repo.essentialsx.net/releases")
        content {
            includeGroup("net.essentialsx")
        }
    }
    maven {
        name = "dmulloy2-repo"
        url = uri("https://repo.dmulloy2.net/nexus/repository/public/")
        content {
            includeGroup("com.comphenix.protocol")
            includeGroup("com.comphenix.executors")
        }
    }
    maven {
        name = "citizens-repo"
        url = uri("https://repo.citizensnpcs.co/")
        content {
            includeGroup("net.citizensnpcs")
        }
    }
    maven {
        name = "placeholderapi-repo"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        content {
            includeGroup("me.clip")
        }
    }
    maven("https://jitpack.io")
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.22")
    compileOnly("org.projectlombok:lombok:1.18.22")

    // Spigot API
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    // Essentials
    compileOnly("net.essentialsx:EssentialsX:2.19.0") {
        exclude("org.bstats", "bstats-bukkit")
        exclude("io.papermc", "paperlib")
    }
    // ProtocolLib
    compileOnly("com.comphenix.protocol:ProtocolLib-API:4.4.0")
    // Vault API
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    // Citizens API
    compileOnly("net.citizensnpcs:citizensapi:2.0.28-SNAPSHOT")
    // TrailGUI
    compileOnly(files("dependencies/TrailGUI-5.15-SNAPSHOT.jar"))
    // Dynmap
    compileOnly(files("dependencies/Dynmap-3.0-beta-4-spigot.jar"))
    // SuperTrails
    compileOnly(files("dependencies/SuperTrails.jar"))
    // MVdWPlaceholderAPI
    compileOnly("be.maximvdw:MVdWPlaceholderAPI:3.1.1-SNAPSHOT") {
        exclude("org.spigotmc", "spigot")
    }
    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.10.10") {
        exclude("net.kyori", "adventure-platform-bukkit")
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
