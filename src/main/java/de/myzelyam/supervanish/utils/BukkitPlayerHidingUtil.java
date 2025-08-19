package de.myzelyam.supervanish.utils;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.entity.Player;

public class BukkitPlayerHidingUtil {

    private BukkitPlayerHidingUtil() {
    }

    public static void hidePlayer(Player player, Player viewer, SuperVanish plugin) {
        if (isNewPlayerHidingAPISupported(plugin))
            viewer.hidePlayer(plugin, player);
        else
            //noinspection deprecation
            viewer.hidePlayer(player);
    }

    public static void showPlayer(Player player, Player viewer, SuperVanish plugin) {
        if (isNewPlayerHidingAPISupported(plugin))
            viewer.showPlayer(plugin, player);
        else
            //noinspection deprecation
            viewer.showPlayer(player);
    }

    public static boolean isNewPlayerHidingAPISupported(SuperVanish plugin) {
        return plugin.getVersionUtil().isOneDotXOrHigher(19);
    }
}
