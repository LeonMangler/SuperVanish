/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ServerListPacketListener extends PacketAdapter {

    private final SuperVanish plugin;

    private final FileConfiguration settings;

    public ServerListPacketListener(SuperVanish plugin) {
        //noinspection deprecation
        super(plugin, ListenerPriority.NORMAL, PacketType.Status.Server.OUT_SERVER_INFO);
        this.plugin = plugin;
        settings = plugin.settings;
    }

    public void register() {
        if ((!settings.getBoolean("Configuration.Serverlist.AdjustAmountOfOnlinePlayers"))
                && (!settings.getBoolean("Configuration.Serverlist.AdjustListOfLoggedInPlayers")))
            return;
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    @Override
    public void onPacketSending(PacketEvent e) {
        try {
            WrappedServerPing serverPing = e.getPacket()
                    .getServerPings().read(0);
            Collection<Player> invisiblePlayers = this.plugin.getOnlineInvisiblePlayers();
            int invisiblePlayersCount = invisiblePlayers.size();
            int onlinePlayersCount = Bukkit.getOnlinePlayers().size();
            if (settings.getBoolean("Configuration.Serverlist.AdjustAmountOfOnlinePlayers")) {
                serverPing.setPlayersOnline(onlinePlayersCount - invisiblePlayersCount);
            }
            if (settings.getBoolean("Configuration.Serverlist.AdjustListOfLoggedInPlayers")) {
                List<WrappedGameProfile> wrappedGameProfiles = new ArrayList<>(serverPing.getPlayers());
                Iterator<WrappedGameProfile> iterator = wrappedGameProfiles.iterator();
                while (iterator.hasNext()) {
                    WrappedGameProfile profile = iterator.next();
                    for (Player onlineInvisiblePlayer : invisiblePlayers) {
                        if (profile.getUUID().equals(onlineInvisiblePlayer.getUniqueId())) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                serverPing.setPlayers(wrappedGameProfiles);
            }
        } catch (Exception ex) {
            plugin.printException(ex);
        }
    }
}