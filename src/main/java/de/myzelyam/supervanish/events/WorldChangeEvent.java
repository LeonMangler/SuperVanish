/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.events;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.commands.CommandAction;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldChangeEvent implements Listener {

    private final SuperVanish plugin;

    public WorldChangeEvent(SuperVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        try {
            final Player p = e.getPlayer();
            if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId()))
                return;
            // check auto-reappear option
            if (plugin.getSettings().getBoolean("VanishStateFeatures.ReappearOnWorldChange")
                    || plugin.getSettings().getBoolean("VanishStateFeatures.CheckPermissionOnWorldChange")
                    && !CommandAction.VANISH_SELF.checkPermission(p, plugin)) {
                plugin.getVisibilityChanger().showPlayer(p);
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }
}