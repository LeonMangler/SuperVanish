/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.hider;

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
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServerListPacketListener {

    private final SuperVanish plugin;

    private final FileConfiguration settings;

    public ServerListPacketListener(SuperVanish plugin) {
        this.plugin = plugin;
        settings = plugin.settings;
    }

    public void registerListener() {
        if ((!settings
                .getBoolean("Configuration.Serverlist.AdjustAmountOfOnlinePlayers"))
                && (!settings.getBoolean("Configuration.Serverlist.AdjustListOfLoggedInPlayers")))
            return;
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.NORMAL,
                        PacketType.Status.Server.OUT_SERVER_INFO) {

                    @Override
                    public void onPacketSending(PacketEvent e) {
                        try {
                            if (e.getPacketType() == PacketType.Status.Server.OUT_SERVER_INFO) {
                                WrappedServerPing serverPing = e.getPacket()
                                        .getServerPings().read(0);
                                Collection<Player> invisiblePlayers = ServerListPacketListener.this.
                                        plugin.getOnlineInvisiblePlayers();
                                int invisiblePlayersCount = invisiblePlayers.size();
                                int onlinePlayersCount = Bukkit.getOnlinePlayers().size();
                                if (settings.getBoolean("Configuration.Serverlist.AdjustAmountOfOnlinePlayers")) {
                                    serverPing.setPlayersOnline(onlinePlayersCount - invisiblePlayersCount);
                                }
                                if (settings.getBoolean("Configuration.Serverlist.AdjustListOfLoggedInPlayers")) {
                                    List<WrappedGameProfile> wrappedGameProfiles = new ArrayList<>();
                                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                        WrappedGameProfile profile = WrappedGameProfile
                                                .fromPlayer(onlinePlayer);
                                        if (!invisiblePlayers.contains(onlinePlayer))
                                            wrappedGameProfiles.add(profile);
                                    }
                                    serverPing.setPlayers(wrappedGameProfiles);
                                }
                            }
                        } catch (Exception ex) {
                            ServerListPacketListener.this.plugin.printException(ex);
                        }
                    }
                });
    }
}