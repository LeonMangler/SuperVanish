/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
