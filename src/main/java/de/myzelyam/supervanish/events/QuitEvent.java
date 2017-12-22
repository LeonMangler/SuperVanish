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
import de.myzelyam.supervanish.features.Broadcast;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;

public class QuitEvent implements EventExecutor, Listener {

    private final SuperVanish plugin;

    public QuitEvent(SuperVanish plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Listener l, Event event) {
        try {
            if (event instanceof PlayerQuitEvent) {
                PlayerQuitEvent e = (PlayerQuitEvent) event;
                FileConfiguration config = plugin.getConfig();
                Player p = e.getPlayer();
                // if is invisible
                if (plugin.getVanishStateMgr().isVanished(p.getUniqueId())) {
                    // remove action bar
                    if (plugin.getActionBarMgr() != null && plugin.getSettings().getBoolean(
                            "MessageOptions.DisplayActionBar")) {
                        plugin.getActionBarMgr().removeActionBar(p);
                    }
                    // check auto-reappear-option
                    boolean noMsg = false;
                    if (plugin.getSettings().getBoolean("VanishStateFeatures.ReappearOnQuit")
                            || plugin.getSettings().getBoolean("VanishStateFeatures.CheckPermissionOnQuit")
                            && !CommandAction.VANISH_SELF.checkPermission(p, plugin)) {
                        plugin.getVanishStateMgr().setVanishedState(p.getUniqueId(), p.getName(), false, null);
                        // collision
                        try {
                            //noinspection deprecation
                            p.getPlayer().spigot().setCollidesWithEntities(true);
                        } catch (NoClassDefFoundError | NoSuchMethodError ignored) {
                        }
                        // check if it should handle the quit msg
                        if (!config.getBoolean("MessageOptions.ReappearOnQuitHideLeaveMsg"))
                            noMsg = true;
                    }
                    // check remove-quit-msg option
                    if (!noMsg && config.getBoolean("MessageOptions.HideRealJoinQuitMessages")) {
                        e.setQuitMessage(null);
                        Broadcast.announceSilentQuit(p, plugin);
                    }
                }
                // remove VanishPlayer
                plugin.removeVanishPlayer(plugin.getVanishPlayer(p));
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }
}