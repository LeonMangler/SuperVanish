/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.hooks;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.utils.FoliaUtil;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.UUID;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;


public class MVdWPlaceholderAPIHook extends PluginHook {

    private final String yes, no, prefix, suffix;

    public MVdWPlaceholderAPIHook(SuperVanish superVanish) {
        super(superVanish);
        yes = superVanish.getMessage("PlaceholderIsVanishedYes");
        no = superVanish.getMessage("PlaceholderIsVanishedNo");
        prefix = superVanish.getMessage("PlaceholderVanishPrefix");
        suffix = superVanish.getMessage("PlaceholderVanishSuffix");
    }

    @Override
    public void onPluginEnable(Plugin plugin) {
        PlaceholderAPI.registerPlaceholder(superVanish, "supervanish_isvanished",
                e -> {
                    try {
                        Player p = e.getPlayer();
                        if (p == null) return no;
                        try {
                            return superVanish.getVanishStateMgr().isVanished(p.getUniqueId()) ? yes
                                    : no;
                        } catch (Exception er) {
                            superVanish.logException(er);
                        }
                        return null;
                    } catch (Throwable t) {
                        return handlePlaceholderFailure(t, no);
                    }
                });
        PlaceholderAPI.registerPlaceholder(superVanish, "supervanish_vanishprefix",
                e -> {
                    try {
                        Player p = e.getPlayer();
                        if (p == null) return "";
                        try {
                            return superVanish.getVanishStateMgr().isVanished(p.getUniqueId()) ? prefix : "";
                        } catch (Exception er) {
                            superVanish.logException(er);
                        }
                        return null;
                    } catch (Throwable t) {
                        return handlePlaceholderFailure(t, "");
                    }
                });
        PlaceholderAPI.registerPlaceholder(superVanish, "supervanish_vanishsuffix",
                e -> {
                    try {
                        Player p = e.getPlayer();
                        if (p == null) return "";
                        try {
                            return superVanish.getVanishStateMgr().isVanished(p.getUniqueId()) ? suffix : "";
                        } catch (Exception er) {
                            superVanish.logException(er);
                        }
                        return null;
                    } catch (Throwable t) {
                        return handlePlaceholderFailure(t, "");
                    }
                });
        PlaceholderAPI.registerPlaceholder(superVanish, "supervanish_vanishedplayers",
                e -> {
                    try {
                        Player p = e.getPlayer();
                        Collection<UUID> onlineVanishedPlayers = superVanish.getVanishStateMgr()
                                .getOnlineVanishedPlayers();
                        String playerListMessage = "";
                        for (UUID uuid : onlineVanishedPlayers) {
                            if (p != null && superVanish.getSettings().getBoolean(
                                    "IndicationFeatures.LayeredPermissions.HideInvisibleInCommands", false)
                                    && superVanish.getVisibilityChanger().getHider().isHidden(uuid, p)) {
                                continue;
                            }
                            String name = superVanish.getPlayerData().getString(
                                    "PlayerData." + uuid + ".information.name", uuid.toString());
                            playerListMessage = playerListMessage + name + ", ";
                        }
                        return playerListMessage.length() > 3
                                ? playerListMessage.substring(0, playerListMessage.length() - 2)
                                : playerListMessage;
                    } catch (Throwable t) {
                        return handlePlaceholderFailure(t, "");
                    }
                });
        PlaceholderAPI.registerPlaceholder(superVanish, "supervanish_playercount",
                e -> {
                    try {
                        Player p = e.getPlayer();
                        int playercount = FoliaUtil.onlinePlayersSnapshot().size();
                        for (UUID uuid : superVanish.getVanishStateMgr()
                                .getOnlineVanishedPlayers()) {
                            if (p == null || superVanish.getVisibilityChanger().getHider()
                                    .isHidden(uuid, p))
                                playercount--;
                        }
                        return playercount + "";
                    } catch (Throwable t) {
                        return handlePlaceholderFailure(t, FoliaUtil.onlinePlayersSnapshot().size() + "");
                    }
                });
    }

    private String handlePlaceholderFailure(Throwable throwable, String fallback) {
        if (!(throwable instanceof LinkageError || throwable instanceof ConcurrentModificationException)) {
            superVanish.logException(throwable);
        }
        return fallback;
    }
}
