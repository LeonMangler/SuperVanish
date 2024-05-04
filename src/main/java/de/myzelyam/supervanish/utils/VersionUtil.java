package de.myzelyam.supervanish.utils;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Bukkit;

public class VersionUtil {

    private final SuperVanish plugin;
    private final int[] version;

    public VersionUtil(SuperVanish plugin) {
        this.plugin = plugin;
        this.version = new int[3];
        String[] bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.");
        for (int i=0; i < Math.min(bukkitVersion.length, 3); i++) {
            this.version[i] = Integer.parseInt(bukkitVersion[i]);
        }
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

    public boolean isOneDotX(int minorRelease) {
        return minorRelease == this.version[1];
    }

    public boolean isOneDotXOrHigher(int minorRelease) {
        return this.isOneDotXOrHigher(minorRelease, 0);
    }
    public boolean isOneDotXOrHigher(int minorRelease, int build) {
        if (this.version[0] > 1) return true;
        if (this.version[1] > minorRelease) return true;
        return this.version[1] == minorRelease && this.version[2] >= build;
    }
}
