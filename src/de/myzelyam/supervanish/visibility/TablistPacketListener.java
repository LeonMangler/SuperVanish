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
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.PlayerInfoData;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TablistPacketListener extends PacketAdapter {

    private final SuperVanish plugin;

    private final FileConfiguration settings;

    public TablistPacketListener(SuperVanish plugin) {
        //noinspection deprecation
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO);
        this.plugin = plugin;
        settings = plugin.settings;
    }

    public void register() {
        if (!settings.getBoolean("Configuration.Tablist.MarkVanishedPlayersAsSpectators")) return;
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        try {
            List<PlayerInfoData> data = new ArrayList<>(
                    event.getPacket().getPlayerInfoDataLists().read(0));
            int originalSize = data.size();
            Player receiver = event.getPlayer();
            Collection<Player> vanishedPlayers = this.plugin.getOnlineInvisiblePlayers();
            for (PlayerInfoData infoData : new ArrayList<>(data)) {
                for (Player vanishedTabPlayer : vanishedPlayers) {
                    // is infoData player a vanished player?
                    if (infoData.getProfile().getUUID().toString()
                            .equals(vanishedTabPlayer.getUniqueId().toString())) {
                        // vanishedTabPlayer = the player who's shown in the data
                        if (receiver.canSee(vanishedTabPlayer) && !receiver.getUniqueId().equals
                                (vanishedTabPlayer.getUniqueId())) {
                            // receiver IS allowed to see vanishedTabPlayer: show in tab as vanished
                            if (infoData.getGameMode() == NativeGameMode.CREATIVE
                                    || infoData.getGameMode() == NativeGameMode.SURVIVAL
                                    || infoData.getGameMode() == NativeGameMode.ADVENTURE) {
                                PlayerInfoData newData = new PlayerInfoData(infoData.getProfile(), infoData
                                        .getLatency(), NativeGameMode.SPECTATOR, infoData.getDisplayName());
                                data.remove(infoData);
                                data.add(newData);
                            }
                        }
                        break;
                    }
                }
                event.getPacket().getPlayerInfoDataLists().write(0, data);
            }
        } catch (Exception ex) {
            this.plugin.printException(ex);
        }
    }
}