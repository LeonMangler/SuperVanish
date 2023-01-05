package de.myzelyam.supervanish.utils;

import de.myzelyam.supervanish.SuperVanish;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.utility.MinecraftVersion;

public class VersionUtil {

    private final SuperVanish plugin;
    private final String version;

    public VersionUtil(SuperVanish plugin) {
        this.plugin = plugin;
        version = plugin.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
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

    public boolean isOneDotX(int majorRelease) {
        return version.contains("v1_" + majorRelease + "_R");
    }

    public boolean isOneDotXOrHigher(int majorRelease) {
        for (int i = majorRelease; i < 20; i++)
            if (version.contains("v1_" + i + "_R")) return true;
        return version.contains("v2_");
    }

    private static final MinecraftVersion mcSpigot1993 = MinecraftVersion.fromServerVersion("3632-Spigot-d90018e-d67777f (MC: 1.19.3)");    // MC Version 1.19.3+
    public static int playerInfoDataListsOffset() {
        // detect MC 1.19.3+
        if( ProtocolLibrary.getProtocolManager().getMinecraftVersion().isAtLeast(mcSpigot1993) ) {
            return 1;
        } else {
            return 0;
        }
    }
}
