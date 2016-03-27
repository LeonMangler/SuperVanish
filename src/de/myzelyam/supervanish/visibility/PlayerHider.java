/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.visibility;

import de.myzelyam.api.vanish.VanishAPI;
import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerHider {

    private final SuperVanish plugin;

    public PlayerHider(SuperVanish plugin) {
        this.plugin = plugin;

    }

    public void showToAll(Player player) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            viewer.showPlayer(player);
        }
    }

    public void hideToAll(Player player) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.hasPermission("sv.see")
                    || !plugin.settings.getBoolean("Configuration.Players.EnableSeePermission")) {
                viewer.hidePlayer(player);
            }
        }
    }

    public void hideAllInvisibleTo(Player viewer) {
        if (viewer.hasPermission("sv.see")
                && plugin.settings.getBoolean("Configuration.Players.EnableSeePermission"))
            return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (VanishAPI.getInvisiblePlayers().contains(
                    player.getUniqueId().toString()))
                viewer.hidePlayer(player);
        }
    }
}
