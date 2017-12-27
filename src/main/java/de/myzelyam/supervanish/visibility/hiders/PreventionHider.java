/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.visibility.hiders;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.utils.BukkitPlayerHidingUtil;

import org.bukkit.entity.Player;

public class PreventionHider extends PlayerHider implements Runnable {

    private int taskId;

    public PreventionHider(SuperVanish plugin) {
        super(plugin);
        taskId = plugin.getServer().getScheduler().runTaskTimer(plugin, this, 1, 1).getTaskId();
    }

    @Override
    public boolean setHidden(Player player, Player viewer, boolean hidden) {
        if (super.setHidden(player, viewer, hidden)) {
            if (hidden) BukkitPlayerHidingUtil.hidePlayer(player, viewer, plugin);
            else BukkitPlayerHidingUtil.showPlayer(player, viewer, plugin);
            return true;
        }
        return false;
    }

    @Override
    public boolean supportsShowInTab() {
        return false;
    }

    @Override
    public String getName() {
        return "Prevention";
    }

    @Override
    public void run() {
        for (Player hidden : playerHiddenFromPlayersMap.keySet()) {
            if (BukkitPlayerHidingUtil.isNewPlayerHidingAPISupported(hidden)) {
                plugin.getServer().getScheduler().cancelTask(taskId);
                return;
            }
            for (Player viewer : playerHiddenFromPlayersMap.get(hidden)) {
                BukkitPlayerHidingUtil.hidePlayer(hidden, viewer, plugin);
            }
        }
    }
}
