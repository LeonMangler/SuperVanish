package de.myzelyam.supervanish.utils;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.entity.Player;

public class BukkitPlayerHidingUtil {

    private BukkitPlayerHidingUtil() {
    }

    public static void hidePlayer(Player player, Player viewer, SuperVanish plugin) {
        if (isNewPlayerHidingAPISupported(viewer))
            viewer.hidePlayer(plugin, player);
        else
            //noinspection deprecation
            viewer.hidePlayer(player);
    }

    public static void showPlayer(Player player, Player viewer, SuperVanish plugin) {
        if (isNewPlayerHidingAPISupported(viewer))
            viewer.showPlayer(plugin, player);
        else
            //noinspection deprecation
            viewer.showPlayer(player);
    }

    public static boolean isNewPlayerHidingAPISupported(Player testPlayer) {
        return false; // New API doesn't seem to work with SuperVanish
    /*
        Class<? extends Player> playerClass = testPlayer.getClass();
        try {
            playerClass.getMethod("hidePlayer", Plugin.class, Player.class);
        } catch (NoSuchMethodException e) {
            return false;
        }
        return true;*/
    }
}
