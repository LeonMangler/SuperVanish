package de.myzelyam.supervanish.utils;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.entity.Player;

public class BukkitPlayerHidingUtil {

    private BukkitPlayerHidingUtil() {
    }

    public static void hidePlayer(Player player, Player viewer, SuperVanish plugin) {
        viewer.hidePlayer(plugin, player);
    }

    public static void showPlayer(Player player, Player viewer, SuperVanish plugin) {
        viewer.showPlayer(plugin, player);
    }

    public static boolean isNewPlayerHidingAPISupported(SuperVanish plugin) {
        return true;
    }
}
