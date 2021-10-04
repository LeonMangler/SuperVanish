# SuperVanish
Website: https://www.spigotmc.org/resources/supervanish-be-invisible.1331/

Bukkit-Plugin for Spigot/CraftBukkit (modded Minecraft server)

Allows server admins to be completely invisible and undetectable for other players, which helps them with their administrative work.

Feel free to create Pull Requests if you'd like to improve SuperVanish! Please report issues on SpigotMC via PM!

## Maven Repository
```xml
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>

<dependency>
	<!-- SuperVanish -->
	<groupId>com.github.LeonMangler</groupId>
	<artifactId>SuperVanish</artifactId>
	<version>6.2.6-2</version>
	<scope>provided</scope>
</dependency>
```
```gradle
repositories {
	maven { url 'https://jitpack.io' }
}
dependencies {
	compileOnly 'com.github.LeonMangler:SuperVanish:6.2.6-2'
}
```
