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
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ServerListPacketListener extends PacketAdapter {

    private final SuperVanish plugin;

    public ServerListPacketListener(SuperVanish plugin) {
        //noinspection deprecation
        super(plugin, ListenerPriority.NORMAL, PacketType.Status.Server.OUT_SERVER_INFO);
        this.plugin = plugin;
    }

    public static void register(SuperVanish plugin) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new ServerListPacketListener(plugin));
    }

    public static boolean isEnabled(SuperVanish plugin) {
        final FileConfiguration config = plugin.getSettings();
        return config.getBoolean(
                "ExternalInvisibility.ServerList.AdjustAmountOfOnlinePlayers")
                || config.getBoolean(
                "ExternalInvisibility.ServerList.AdjustListOfLoggedInPlayers");
    }

    @Override
    public void onPacketSending(PacketEvent e) {
        try {
            final FileConfiguration settings = plugin.getSettings();
            if (!settings.getBoolean("ExternalInvisibility.ServerList.AdjustAmountOfOnlinePlayers")
                    && !settings.getBoolean("ExternalInvisibility.ServerList.AdjustListOfLoggedInPlayers"))
                return;
            WrappedServerPing ping = e.getPacket().getServerPings().read(0);
            Collection<UUID> onlineVanishedPlayers = plugin.getVanishStateMgr().getOnlineVanishedPlayers();
            int vanishedPlayersCount = plugin.getVanishStateMgr().getOnlineVanishedPlayers().size(),
                    playerCount = Bukkit.getOnlinePlayers().size();
            if (settings.getBoolean("ExternalInvisibility.ServerList.AdjustAmountOfOnlinePlayers")) {
                ping.setPlayersOnline(playerCount - vanishedPlayersCount);
            }
            if (settings.getBoolean("ExternalInvisibility.ServerList.AdjustListOfLoggedInPlayers")) {
                List<WrappedGameProfile> wrappedGameProfiles = new ArrayList<>(ping.getPlayers());
                Iterator<WrappedGameProfile> iterator = wrappedGameProfiles.iterator();
                while (iterator.hasNext()) {
                    if (onlineVanishedPlayers.contains(iterator.next().getUUID())) {
                        iterator.remove();
                    }
                }
                ping.setPlayers(wrappedGameProfiles);
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }
}