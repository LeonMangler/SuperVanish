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

public class ForcedInvisibilityTask {

    private final SuperVanish plugin;

    private boolean taskStarted = false;

    public ForcedInvisibilityTask(SuperVanish plugin) {
        this.plugin = plugin;
        startTask();
    }

    private void startTask() throws IllegalStateException {
        if (taskStarted) throw new IllegalStateException("Task has already been started");
        new BukkitRunnable() {

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
        }.runTaskTimer(plugin, 1, 1);
        taskStarted = true;
    }
}
