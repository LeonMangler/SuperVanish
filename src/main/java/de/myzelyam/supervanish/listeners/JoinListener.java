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
import org.bukkit.plugin.EventExecutor;

import java.util.HashMap;
import java.util.Map;

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
                final boolean vanished = plugin.getVanishStateMgr().isVanished(p.getUniqueId());
                final java.util.List<Player> snapshot = new java.util.ArrayList<>(Bukkit.getOnlinePlayers());

                final Map<java.util.UUID, Boolean> joinerCanSeeMap = new HashMap<>(snapshot.size());
                final Map<java.util.UUID, Boolean> onlineCanSeeJoinerMap = new HashMap<>(snapshot.size());

                for (Player onlinePlayer : snapshot) {
                    if (onlinePlayer.getUniqueId().equals(p.getUniqueId())) continue;
                    joinerCanSeeMap.put(onlinePlayer.getUniqueId(), plugin.hasPermissionToSee(p, onlinePlayer));
                    if (vanished) {
                        onlineCanSeeJoinerMap.put(onlinePlayer.getUniqueId(), plugin.hasPermissionToSee(onlinePlayer, p));
                    }
                }

                for (Player onlinePlayer : snapshot) {
                    if (onlinePlayer.getUniqueId().equals(p.getUniqueId())) continue;

                    if (plugin.getVanishStateMgr().isVanished(onlinePlayer.getUniqueId())
                            && !joinerCanSeeMap.get(onlinePlayer.getUniqueId())) {
                        plugin.getVisibilityChanger().getHider().setHidden(onlinePlayer, p, true);
                    }
                    if (vanished && !onlineCanSeeJoinerMap.get(onlinePlayer.getUniqueId())) {
                        plugin.getVisibilityChanger().getHider().setHidden(p, onlinePlayer, true);
                    }
                }

                // vanished:
                if (vanished) {
                    // Join message
                    if (plugin.getSettings().getBoolean("MessageOptions.HideRealJoinQuitMessages")) {
                        e.setJoinMessage(null);
                        Broadcast.announceSilentJoin(p, plugin);
                    }
                    // reminding message
                    if (plugin.getSettings().getBoolean("MessageOptions.RemindVanishedOnJoin")) {
                        SuperVanish.getScheduler().runTaskLater(() -> {
                            if (plugin.getSettings().getBoolean("MessageOptions.RemindVanishedOnJoin")
                                    && plugin.getVanishStateMgr().isVanished(p.getUniqueId())) {
                                plugin.sendMessage(p, plugin.getMessage("RemindingMessage"), p);
                            }
                        }, 2L);
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
                    plugin.setVanishMetadata(p, true);
                } else {
                    // not vanished:
                    // metadata
                    plugin.setVanishMetadata(p, false);
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
                                                SuperVanish.getScheduler().runTaskLater(() -> {
                          plugin.sendMessage(p, "RecreationRequiredMsg", p);
                        }, 1L);
                }
                if (plugin.getPacketListener() != null) {
                    plugin.getPacketListener().injectPlayer(p);
                }
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }
}