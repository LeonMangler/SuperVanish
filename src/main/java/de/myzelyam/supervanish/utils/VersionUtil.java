package de.myzelyam.supervanish.utils;

import de.myzelyam.supervanish.SuperVanish;

import java.util.regex.Pattern;

public class VersionUtil {

    private final SuperVanish plugin;
    private final String minecraftVersion;

    public VersionUtil(SuperVanish plugin) {
        this.plugin = plugin;
        // saves versions in the format x.x.x (e.g. 1.20.1)
        minecraftVersion = plugin.getServer().getBukkitVersion().split(Pattern.quote("-"))[0];
    }

    public int compareVersions(String version1, String version2) {
        String[] levels1 = version1.split("\\.");
        String[] levels2 = version2.split("\\.");
        int length = Math.max(levels1.length, levels2.length);
        for (int i = 0; i < length; i++) {
            Integer v1 = i < levels1.length ? Integer.parseInt(levels1[i]) : 0;
            Integer v2 = i < levels2.length ? Integer.parseInt(levels2[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0) {
                return compare;
            }
        }
        return 0;
    }

    public boolean isMinecraftAtLeast(String version) {
        return compareVersions(minecraftVersion, version) >= 0;
    }
}
