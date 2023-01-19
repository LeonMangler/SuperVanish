/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.hooks;

import de.myzelyam.supervanish.SuperVanish;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public class PlaceholderAPIHook extends PluginHook {

    private final String yes, no, prefix, suffix;

    public PlaceholderAPIHook(SuperVanish superVanish) {
        super(superVanish);
        yes = superVanish.getMessage("PlaceholderIsVanishedYes");
        no = superVanish.getMessage("PlaceholderIsVanishedNo");
        prefix = superVanish.getMessage("PlaceholderVanishPrefix");
        suffix = superVanish.getMessage("PlaceholderVanishSuffix");
        new SVPlaceholderExpansion().register();
    }

    public static String translatePlaceholders(String msg, Player p) {
        return PlaceholderAPI.setPlaceholders((OfflinePlayer) p, msg);
    }

    public class SVPlaceholderExpansion extends PlaceholderExpansion {

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public boolean canRegister() {
            return true;
        }

        @Override
        public String getAuthor() {
            return superVanish.getDescription().getAuthors().toString();
        }

        @Override
        public String getIdentifier() {
            return "supervanish";
        }

        @Override
        public String getVersion() {
            return superVanish.getDescription().getVersion();
        }

        @Override
        public String onRequest(OfflinePlayer op, String id) {
            try {
                Player p;
                if (op instanceof Player)
                    p = (Player) op;
                else
                    p = null;
                if (id.equalsIgnoreCase("isvanished")
                        || id.equalsIgnoreCase("isinvisible")
                        || id.equalsIgnoreCase("vanished")
                        || id.equalsIgnoreCase("invisible"))
                    return p != null && superVanish.getVanishStateMgr().isVanished(p.getUniqueId())
                            ? yes : no;
                if (id.equalsIgnoreCase("vanishprefix"))
                    return p != null && superVanish.getVanishStateMgr().isVanished(p.getUniqueId())
                            ? prefix : "";
                if (id.equalsIgnoreCase("vanishsuffix"))
                    return p != null && superVanish.getVanishStateMgr().isVanished(p.getUniqueId())
                            ? suffix : "";
                if (id.equalsIgnoreCase("onlinevanishedplayers")
                        || id.equalsIgnoreCase("onlinevanished")
                        || id.equalsIgnoreCase("invisibleplayers")
                        || id.equalsIgnoreCase("vanishedplayers")
                        || id.equalsIgnoreCase("hiddenplayers")) {
                    Collection<UUID> onlineVanishedPlayers = superVanish.getVanishStateMgr()
                            .getOnlineVanishedPlayers();
                    String playerListMessage = "";
                    for (UUID uuid : onlineVanishedPlayers) {
                        Player onlineVanished = Bukkit.getPlayer(uuid);
                        if (onlineVanished == null) continue;
                        if (superVanish.getSettings().getBoolean(
                                "IndicationFeatures.LayeredPermissions.HideInvisibleInCommands", false)
                                && !superVanish.hasPermissionToSee(p, onlineVanished)) {
                            continue;
                        }
                        playerListMessage = playerListMessage + onlineVanished.getName() + ", ";
                    }
                    return playerListMessage.length() > 3
                            ? playerListMessage.substring(0, playerListMessage.length() - 2)
                            : playerListMessage;
                }
                if (id.equalsIgnoreCase("playercount")
                        || id.equalsIgnoreCase("onlineplayers")) {
                    int playercount = Bukkit.getOnlinePlayers().size();
                    for (UUID uuid : superVanish.getVanishStateMgr()
                            .getOnlineVanishedPlayers()) {
                        Player onlineVanished = Bukkit.getPlayer(uuid);
                        if (onlineVanished == null) continue;
                        if (p == null || !superVanish.canSee(p, onlineVanished)) playercount--;
                    }
                    return playercount + "";
                }
            } catch (Exception e) {
                superVanish.logException(e);
            }
            return null;
        }
    }
}
