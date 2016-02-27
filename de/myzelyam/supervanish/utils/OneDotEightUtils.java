package de.myzelyam.supervanish.utils;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class OneDotEightUtils {
    public static boolean isSpectator(Player p) {
        return p.getGameMode() == GameMode.SPECTATOR;
    }

    public static boolean isPressurePlate(Material material) {
        return material == Material.STONE_PLATE || material == Material.WOOD_PLATE ||
                material == Material.GOLD_PLATE || material == Material.IRON_PLATE;
    }
}
