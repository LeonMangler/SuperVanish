/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.hider;

import de.myzelyam.api.vanish.VanishAPI;
import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ForcedInvisibilityTask extends BukkitRunnable {

    private final SuperVanish plugin;

    public ForcedInvisibilityTask(SuperVanish plugin) {
        this.plugin = plugin;
    }

    public void start() throws IllegalStateException {
        runTaskTimer(plugin, 1, 1);
    }

    @Override
    public void run() {
        for (Player hidden : plugin.getOnlineInvisiblePlayers()) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (hidden == onlinePlayer) continue;
                if (!VanishAPI.canSee(onlinePlayer, hidden))
                    onlinePlayer.hidePlayer(hidden);
            }
        }
    }
}
