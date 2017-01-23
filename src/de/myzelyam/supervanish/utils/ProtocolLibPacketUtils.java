/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.InvocationTargetException;

import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_EFFECT;
import static com.comphenix.protocol.PacketType.Play.Server.REMOVE_ENTITY_EFFECT;

public class ProtocolLibPacketUtils {

    public static final int INFINITE_POTION_LENGTH = 32767;

    private final SuperVanish plugin;

    public ProtocolLibPacketUtils(SuperVanish plugin) {
        this.plugin = plugin;
    }

    public void sendActionBar(Player p, String bar) {
        String json = "{\"text\": \""
                + ChatColor.translateAlternateColorCodes('&', bar) + "\"}";
        WrappedChatComponent msg = WrappedChatComponent.fromJson(json);
        PacketContainer chatMsg = new PacketContainer(
                PacketType.Play.Server.CHAT);
        chatMsg.getChatComponents().write(0, msg);
        chatMsg.getBytes().write(0, (byte) 2);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, chatMsg);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Cannot send packet " + chatMsg, e);
        }
    }

    public void sendAddPotionEffect(Player p, PotionEffect effect) {
        PacketContainer packet = new PacketContainer(ENTITY_EFFECT);
        @SuppressWarnings("deprecation")
        int effectID = effect.getType().getId();
        int amplifier = effect.getAmplifier();
        int duration = effect.getDuration();
        int entityID = p.getEntityId();
        packet.getIntegers().write(0, entityID);
        packet.getBytes().write(0, (byte) effectID);
        packet.getBytes().write(1, (byte) amplifier);
        if (!plugin.isOneDotXOrHigher(8))
            packet.getShorts().write(1, (short) duration);
        else
            packet.getIntegers().write(1, duration);
        // hide particles in 1.8 or higher
        if (plugin.isOneDotXOrHigher(8))
            packet.getBytes().write(2, (byte) 0);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Cannot send packet", e);
        }
    }

    public void sendRemovePotionEffect(Player p, PotionEffectType type) {
        PacketContainer packet = new PacketContainer(REMOVE_ENTITY_EFFECT);
        int entityID = p.getEntityId();
        // 1.7 or below
        if (!plugin.isOneDotXOrHigher(8)) {
            @SuppressWarnings("deprecation")
            int effectID = type.getId();
            packet.getIntegers().write(0, entityID);
            packet.getBytes().write(0, (byte) effectID);
        } else if (plugin.isOneDotX(8)) {
            // 1.8
            @SuppressWarnings("deprecation")
            int effectID = type.getId();
            packet.getIntegers().write(0, entityID);
            packet.getIntegers().write(1, effectID);
        } else {
            // 1.9 or higher
            packet.getEffectTypes().write(0, type);
            packet.getIntegers().write(0, entityID);
        }
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Cannot send packet", e);
        }
    }
}
