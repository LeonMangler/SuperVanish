/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.visibility;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import de.myzelyam.supervanish.SuperVanish;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ActionBarMgr {

    private final SuperVanish plugin;
    private final List<Player> actionBars = new ArrayList<>();

    public ActionBarMgr(SuperVanish plugin) {
        this.plugin = plugin;
        startTask();
    }

    private void startTask() {
        new BukkitRunnable() {

            @Override
            public void run() {
                for (Player p : actionBars) {
                    try {
                        sendActionBar(p, plugin.replacePlaceholders(
                                plugin.getMessage("ActionBarMessage"), p));
                    } catch (Exception e) {
                        cancel();
                        plugin.logException(e);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 2 * 20);
    }

    private void sendActionBar(Player p, String bar) {
        String json = "{\"text\": \"" + ChatColor.translateAlternateColorCodes('&', bar) + "\"}";
        WrappedChatComponent msg = WrappedChatComponent.fromJson(json);
        PacketContainer chatMsg = new PacketContainer(PacketType.Play.Server.CHAT);
        chatMsg.getChatComponents().write(0, msg);
        if (plugin.getVersionUtil().isOneDotXOrHigher(12))
            try {
                chatMsg.getChatTypes().write(0, EnumWrappers.ChatType.GAME_INFO);
            } catch (NoSuchMethodError e) {
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText
                        ("SuperVanish: Please update ProtocolLib"));
            }
        else
            chatMsg.getBytes().write(0, (byte) 2);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, chatMsg);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Cannot send packet " + chatMsg, e);
        }
    }

    public void addActionBar(Player p) {
        actionBars.add(p);
    }

    public void removeActionBar(Player p) {
        actionBars.remove(p);
    }
}
