/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.listeners;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.features.Broadcast;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.scheduler.BukkitRunnable;

public class JoinListener implements EventExecutor, Listener {

    private final SuperVanish plugin;

    public JoinListener(SuperVanish plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Listener l, Event event) {
        try {
            if (event instanceof PlayerJoinEvent) {
                PlayerJoinEvent e = (PlayerJoinEvent) event;
                final Player p = e.getPlayer();
                // hide others
                for (Player onlinePlayer : Bukkit.getOnlinePlayers())
                    if (plugin.getVanishStateMgr().isVanished(onlinePlayer.getUniqueId())
                            && !plugin.hasPermissionToSee(p, onlinePlayer))
                        plugin.getVisibilityChanger().getHider().setHidden(onlinePlayer, p, true);
                // vanished:
                if (plugin.getVanishStateMgr().isVanished(p.getUniqueId())) {
                    // hide self
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers())
                        if (!plugin.hasPermissionToSee(onlinePlayer, p))
                            plugin.getVisibilityChanger().getHider().setHidden(p, onlinePlayer, true);
                    // Join message
                    if (plugin.getSettings().getBoolean("MessageOptions.HideRealJoinQuitMessages")) {
                        e.setJoinMessage(null);
                        Broadcast.announceSilentJoin(p, plugin);
                    }
                    // reminding message
                    if (plugin.getSettings().getBoolean("MessageOptions.RemindVanishedOnJoin")) {
                        plugin.sendMessage(p, "RemindingMessage", p);
                    }
                    // re-add action bar
                    if (plugin.getActionBarMgr() != null && plugin.getSettings().getBoolean(
                            "MessageOptions.DisplayActionBar")) {
                        plugin.getActionBarMgr().addActionBar(p);
                    }
                    // sleep state
                    p.setSleepingIgnored(true);
                    // adjust fly
                    if (plugin.getSettings().getBoolean("InvisibilityFeatures.Fly.Enable")) {
                        p.setAllowFlight(true);
                    }
                    // metadata
                    p.setMetadata("vanished", new FixedMetadataValue(plugin, true));
                } else {
                    // not vanished:
                    // metadata
                    p.removeMetadata("vanished", plugin);
                }
                // not necessarily vanished:
                // recreate files msg
                if ((p.hasPermission("sv.recreatecfg") || p.hasPermission("sv.recreatefiles"))
                        && (plugin.getConfigMgr().isSettingsUpdateRequired()
                        || plugin.getConfigMgr().isMessagesUpdateRequired())) {
                    String currentVersion = plugin.getDescription().getVersion();
                    boolean isDismissed =
                            plugin.getPlayerData().getBoolean("PlayerData." + p.getUniqueId() + ".dismissed."
                                    + currentVersion.replace(".", "_"), false);
                    if (!isDismissed)
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                plugin.sendMessage(p, "RecreationRequiredMsg", p);
                            }
                        }.runTaskLater(plugin, 1);
                }
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }
}