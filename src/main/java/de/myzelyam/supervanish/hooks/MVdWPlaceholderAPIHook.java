/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.hooks;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.UUID;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;


public class MVdWPlaceholderAPIHook extends PluginHook {

    public MVdWPlaceholderAPIHook(SuperVanish superVanish) {
        super(superVanish);
    }

    @Override
    public void onPluginEnable(Plugin plugin) {
        PlaceholderAPI.getCustomPlaceholders().remove("supervanish_isvanished");
        PlaceholderAPI.getCustomPlaceholders().remove("supervanish_vanishedplayers");
        PlaceholderAPI.getCustomPlaceholders().remove("supervanish_playercount");
        PlaceholderAPI.registerPlaceholder(superVanish, "supervanish_isvanished",
                new PlaceholderReplacer() {
                    @Override
                    public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
                        try {
                            Player p = e.getPlayer();
                            if (p == null) return "No";
                            try {
                                return superVanish.getVanishStateMgr().isVanished(p.getUniqueId()) ? "Yes"
                                        : "No";
                            } catch (Exception er) {
                                superVanish.logException(er);
                            }
                            return null;
                        } catch (Throwable t) {
                            if (!(t instanceof NoClassDefFoundError || t instanceof
                                    ConcurrentModificationException))
                                superVanish.logException(t);
                            return "No";
                        }
                    }
                });
        PlaceholderAPI.registerPlaceholder(superVanish, "supervanish_vanishedplayers",
                new PlaceholderReplacer() {
                    @Override
                    public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
                        try {
                            Player p = e.getPlayer();
                            Collection<UUID> onlineVanishedPlayers = superVanish.getVanishStateMgr()
                                    .getOnlineVanishedPlayers();
                            String playerListMessage = "";
                            for (UUID uuid : onlineVanishedPlayers) {
                                Player onlineVanished = Bukkit.getPlayer(uuid);
                                if (onlineVanished == null) continue;
                                if (p != null && superVanish.getSettings().getBoolean(
                                        "IndicationFeatures.LayeredPermissions.HideInvisibleInCommands", false)
                                        && !superVanish.hasPermissionToSee(p, onlineVanished)) {
                                    continue;
                                }
                                playerListMessage = playerListMessage + onlineVanished.getName() + ", ";
                            }
                            return playerListMessage.length() > 3
                                    ? playerListMessage.substring(0, playerListMessage.length() - 2)
                                    : playerListMessage;
                        } catch (Throwable t) {
                            if (!(t instanceof NoClassDefFoundError || t instanceof
                                    ConcurrentModificationException))
                                superVanish.logException(t);
                            return "";
                        }
                    }
                });
        PlaceholderAPI.registerPlaceholder(superVanish, "supervanish_playercount",
                new PlaceholderReplacer() {
                    @Override
                    public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
                        try {
                            Player p = e.getPlayer();
                            int playercount = Bukkit.getOnlinePlayers().size();
                            for (UUID uuid : superVanish.getVanishStateMgr()
                                    .getOnlineVanishedPlayers()) {
                                Player onlineVanished = Bukkit.getPlayer(uuid);
                                if (onlineVanished == null) continue;
                                if (p == null || !superVanish.canSee(p, onlineVanished))
                                    playercount--;
                            }
                            return playercount + "";
                        } catch (Throwable t) {
                            if (!(t instanceof NoClassDefFoundError || t instanceof
                                    ConcurrentModificationException))
                                superVanish.logException(t);
                            return Bukkit.getOnlinePlayers().size() + "";
                        }
                    }
                });
    }
}
