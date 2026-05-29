/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.features;

import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.api.vanish.PostPlayerHideEvent;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.utils.FoliaUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.UUID;

public class Broadcast extends Feature {

    public Broadcast(SuperVanish plugin) {
        super(plugin);
    }

    public static void announceSilentJoin(Player vanished, SuperVanish plugin) {
        if (plugin.getSettings().getBoolean("MessageOptions.AnnounceRealJoinQuitToAdmins", true)) {
            FoliaUtil.forEachOnlinePlayer(plugin, onlinePlayer -> {
                if (vanished == onlinePlayer)
                    return;
                if (plugin.canSee(onlinePlayer, vanished)) {
                    plugin.sendMessage(onlinePlayer, "SilentJoinMessageForAdmins", vanished, onlinePlayer);
                }
            });
        }
    }

    public static void announceSilentDeath(Player p, SuperVanish plugin, String deathMessage) {
        if (plugin.getSettings().getBoolean("MessageOptions.AnnounceDeathToAdmins", true)) {
            FoliaUtil.forEachOnlinePlayer(plugin, onlinePlayer -> {
                if (p == onlinePlayer)
                    return;
                if (plugin.canSee(onlinePlayer, p)) {
                    String message = plugin.getMessage("SilentDeathMessage")
                            .replace("%deathmsg%", deathMessage);
                    plugin.sendMessage(onlinePlayer, message, p, onlinePlayer);
                }
            });
        }
    }

    public static void announceSilentQuit(Player p, SuperVanish plugin) {
        if (plugin.getSettings().getBoolean("MessageOptions.AnnounceRealJoinQuitToAdmins", true)) {
            UUID playerUuid = p.getUniqueId();
            int playerUsePermissionLevel = plugin.getCachedUsePermissionLevel(playerUuid);
            FoliaUtil.forEachOnlinePlayer(plugin, onlinePlayer -> {
                if (playerUuid.equals(onlinePlayer.getUniqueId()))
                    return;
                if (plugin.hasPermissionToSee(onlinePlayer, playerUuid, playerUsePermissionLevel)) {
                    plugin.sendMessage(onlinePlayer, "SilentQuitMessageForAdmins", p, onlinePlayer);
                }
            });
        }
    }

    @Override
    public boolean isActive() {
        return plugin.getSettings().getBoolean("MessageOptions.FakeJoinQuitMessages.BroadcastFakeQuitOnVanish")
                || plugin.getSettings().getBoolean("MessageOptions.FakeJoinQuitMessages" +
                ".BroadcastFakeQuitOnReappear");
    }

    @EventHandler
    public void onVanish(PostPlayerHideEvent e) {
        final Player p = e.getPlayer();
        if (plugin.getSettings().getBoolean("MessageOptions.FakeJoinQuitMessages.BroadcastFakeQuitOnVanish")
                && !e.isSilent()) {
            FoliaUtil.forEachOnlinePlayer(plugin, onlinePlayer -> {
                if (!plugin.canSee(onlinePlayer, p)) {
                    if (!plugin.getSettings().getBoolean(
                            "MessageOptions.FakeJoinQuitMessages.SendMessageOnlyToAdmins"))
                        plugin.sendMessage(onlinePlayer, "VanishMessage", p, onlinePlayer);
                } else if (!plugin.getSettings().getBoolean(
                        "MessageOptions.FakeJoinQuitMessages.SendMessageOnlyToUsers"))
                    if (!plugin.getSettings().getBoolean(
                            "MessageOptions.FakeJoinQuitMessages.AnnounceVanishReappearToAdmins"))
                        plugin.sendMessage(onlinePlayer, "VanishMessage", p, onlinePlayer);
                    else if (onlinePlayer == p && !plugin.getSettings().getBoolean(
                            "MessageOptions.FakeJoinQuitMessages.SendMessageOnlyToAdmins"))
                        plugin.sendMessage(onlinePlayer, "VanishMessage", p, onlinePlayer);
                    else if (onlinePlayer != p)
                        plugin.sendMessage(onlinePlayer, "VanishMessageWithPermission", p, onlinePlayer);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReappear(PlayerShowEvent e) {
        Player p = e.getPlayer();
        if (plugin.getSettings().getBoolean(
                "MessageOptions.FakeJoinQuitMessages.BroadcastFakeJoinOnReappear") && !e.isSilent()) {
            FoliaUtil.forEachOnlinePlayer(plugin, onlinePlayer -> {
                if (!plugin.canSee(onlinePlayer, p)) {
                    if (!plugin.getSettings().getBoolean(
                            "MessageOptions.FakeJoinQuitMessages.SendMessageOnlyToAdmins"))
                        plugin.sendMessage(onlinePlayer, "ReappearMessage", p, onlinePlayer);
                } else if (!plugin.getSettings().getBoolean(
                        "MessageOptions.FakeJoinQuitMessages.SendMessageOnlyToUsers"))
                    if (!plugin.getSettings().getBoolean(
                            "MessageOptions.FakeJoinQuitMessages.AnnounceVanishReappearToAdmins"))
                        plugin.sendMessage(onlinePlayer, "ReappearMessageWithPermission", p, onlinePlayer);
                    else if (onlinePlayer == p && !plugin.getSettings().getBoolean(
                            "MessageOptions.FakeJoinQuitMessages.SendMessageOnlyToAdmins"))
                        plugin.sendMessage(onlinePlayer, "ReappearMessage", p, onlinePlayer);
                    else if (onlinePlayer != p)
                        plugin.sendMessage(onlinePlayer, "ReappearMessageWithPermission", p, onlinePlayer);
            });
        }
    }
}
