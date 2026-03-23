# SuperVanish
Website: https://www.spigotmc.org/resources/supervanish-be-invisible.1331/

Bukkit-Plugin for Spigot/CraftBukkit (modded Minecraft server)

Allows server admins to be completely invisible and undetectable for other players, which helps them with their administrative work.

Please note that I currently do not have much time to dedicate to this plugin and as such can not do extensive testing of pull requests. I appreciate pull requests, but please keep them as small and simple as possible. Please report issues on SpigotMC via PM!

## Folia Support
SuperVanish now detects Folia at runtime and routes internal scheduled work through a Folia-aware scheduler facade while keeping a single jar for regular Paper/Spigot servers.

Guaranteed core behavior on Folia:
- Vanish and reappear command flows
- Join/quit visibility restoration
- Action bar refresh scheduling
- Night vision refresh scheduling
- Update checker scheduling

Features that are intentionally reduced on Folia:
- ProtocolLib packet listener integrations that depend on global main-thread style execution are disabled with an explicit startup log message
- Server list packet adjustments through ProtocolLib are disabled with an explicit startup log message
- Silent chest packet adapter is disabled with an explicit startup log message

When running optional integrations such as ProtocolLib or Essentials on Folia, verify that those plugins themselves support Folia. SuperVanish only guarantees its own core scheduling and visibility logic.

## Maven Repository
```
 <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>

    <dependencies>
        <!-- SuperVanish -->
        <dependency>
            <groupId>com.github.LeonMangler</groupId>
            <artifactId>SuperVanish</artifactId>
            <version>6.2.18-3</version>
        </dependency>
    </dependencies>
```
