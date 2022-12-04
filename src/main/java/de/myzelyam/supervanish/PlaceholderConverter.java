/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

import de.myzelyam.supervanish.hooks.PlaceholderAPIHook;
import de.myzelyam.supervanish.utils.Validation;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.maximvdw.placeholderapi.PlaceholderAPI;

public class PlaceholderConverter {

    private final SuperVanish plugin;

    public PlaceholderConverter(SuperVanish plugin) {
        this.plugin = plugin;
    }

    public String replacePlaceholders(String msg, Object... additionalPlayerInfo) {
        Validation.checkIsTrue("Failed to replace variables (Illegal arguments)",
                msg != null, additionalPlayerInfo != null);
        //noinspection ConstantConditions
        Validation.checkIsTrue(additionalPlayerInfo.length > 0);
        // check vararg
        final List<Object> additionalPlayerInfoList = Arrays
                .asList(additionalPlayerInfo);
        Object unspecifiedPlayer = additionalPlayerInfoList.get(0);
        String unspecifiedOtherPlayersName = null;
        if (additionalPlayerInfoList.size() > 1
                && (additionalPlayerInfoList.get(1) instanceof String
                || additionalPlayerInfoList.get(1) instanceof Player)) {
            unspecifiedOtherPlayersName = (String) (additionalPlayerInfoList
                    .get(1) instanceof Player
                    ? ((Player) additionalPlayerInfoList.get(1)).getName()
                    : additionalPlayerInfoList.get(1));
        }
        //noinspection ConstantConditions
        msg = msg.replace("\\n", "\n");
        // replace sender specific variables
        replaceVariables:
        {
            if (unspecifiedPlayer instanceof OfflinePlayer
                    && !(unspecifiedPlayer instanceof Player)) {
                // offline player
                OfflinePlayer specifiedPlayer = (OfflinePlayer) unspecifiedPlayer;
                // MVdWPlaceholderAPI
                if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")
                        && plugin.getSettings().getBoolean("HookOptions.EnableMVdWPlaceholderAPIHook", true)) {
                    String replaced = PlaceholderAPI.replacePlaceholders(specifiedPlayer, msg);
                    msg = replaced == null ? msg : replaced;
                }
                // replace essentials nick names
                if (Bukkit.getPluginManager()
                        .getPlugin("Essentials") != null) {
                    msg = msg.replace("%nick%", specifiedPlayer.getName());
                }
                // replace general variables
                msg = msg.replace("%d%", specifiedPlayer.getName())
                        .replace("%p%", specifiedPlayer.getName())
                        .replace("%tab%", specifiedPlayer.getName());
                // replace other player's name if possible
                msg = msg.replace("%other%", unspecifiedOtherPlayersName != null
                        ? unspecifiedOtherPlayersName : "UNKNOWN");
                break replaceVariables;
            }
            if (unspecifiedPlayer instanceof Player) {
                // player
                Player specifiedPlayer = (Player) unspecifiedPlayer;
                // PlaceholderAPI
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")
                        && plugin.getSettings().getBoolean("HookOptions.EnablePlaceholderAPIHook", true)) {
                    String replaced = PlaceholderAPIHook.translatePlaceholders(msg, specifiedPlayer);
                    //noinspection ConstantConditions
                    msg = replaced == null ? msg : replaced;
                }
                // MVdWPlaceholderAPI
                if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")
                        && plugin.getSettings().getBoolean("HookOptions.EnableMVdWPlaceholderAPIHook", true)) {
                    String replaced = PlaceholderAPI.replacePlaceholders(specifiedPlayer, msg);
                    msg = replaced == null ? msg : replaced;
                }
                // replace essentials nick names
                if (Bukkit.getPluginManager().getPlugin("Essentials") != null) {
                    Essentials ess = (Essentials) Bukkit.getServer()
                            .getPluginManager().getPlugin("Essentials");
                    User u = ess.getUser(specifiedPlayer);
                    if (u != null)
                        if (u.getNickname() != null)
                            msg = msg.replace("%nick%", u.getNickname());
                }
                // replace vault info
                if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                    RegisteredServiceProvider<Permission> permService = plugin.getServer()
                            .getServicesManager().getRegistration(Permission.class);
                    RegisteredServiceProvider<Chat> chatService = plugin.getServer()
                            .getServicesManager().getRegistration(Chat.class);
                    Permission permAPI = permService != null ? permService.getProvider() : null;
                    Chat chatAPI = chatService != null ? chatService.getProvider() : null;
                    try {
                        if (permAPI != null) {
                            String group = permAPI.getPrimaryGroup(specifiedPlayer);
                            if (group != null)
                                msg = msg.replace("%group%", group);
                        }
                        if (chatAPI != null) {
                            String prefix = chatAPI.getPlayerPrefix(specifiedPlayer);
                            String suffix = chatAPI.getPlayerSuffix(specifiedPlayer);
                            if (prefix != null) {
                                msg = msg.replace("%prefix%", prefix);
                            }
                            if (suffix != null) {
                                msg = msg.replace("%suffix%", suffix);
                            }
                        }
                    } catch (UnsupportedOperationException ignored) {
                    }
                }
                // replace general variables
                msg = msg.replace("%d%", "" + specifiedPlayer.getDisplayName())
                        .replace("%p%", "" + specifiedPlayer.getName())
                        .replace("%tab%",
                                "" + specifiedPlayer.getPlayerListName());
                // replace other player's name if possible
                msg = msg.replace("%other%", unspecifiedOtherPlayersName != null
                        ? unspecifiedOtherPlayersName : "UNKNOWN");

                break replaceVariables;
            }
            if (unspecifiedPlayer instanceof CommandSender) {
                // console
                // replace general variables
                msg = msg.replace("%d%", "Console").replace("%p%", "Console")
                        .replace("%tab%", "Console");
                // replace other player's name if possible
                msg = msg.replace("%other%", unspecifiedOtherPlayersName != null
                        ? unspecifiedOtherPlayersName : "UNKNOWN");
            }
        }
        // convert color codes
        if (plugin.getVersionUtil().isOneDotXOrHigher(16)) {
            Pattern pattern = Pattern.compile("&?#[a-fA-F0-9]{6}");
            Matcher matcher = pattern.matcher(msg);

            while (matcher.find()) {
                String color = msg.substring(matcher.start(), matcher.end());
                msg = msg.replace(color, net.md_5.bungee.api.ChatColor.of(color.replace("&", "")) + "");
                matcher = pattern.matcher(msg);
            }
        }
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        return msg;
    }
}
