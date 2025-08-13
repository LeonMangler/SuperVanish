/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.features;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.common.collect.ImmutableList;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.api.vanish.PostPlayerHideEvent;
import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.comphenix.protocol.PacketType.Play.Server.PLAYER_INFO;

/**
 * This is currently unused on Minecraft 1.19 or higher
 */
public class VanishIndication extends Feature {
    private boolean suppressErrors = false;

    public VanishIndication(SuperVanish plugin) {
        super(plugin);
    }

    @Override
    public boolean isActive() {
        return !plugin.getVersionUtil().isOneDotXOrHigher(19)
                && plugin.getSettings().getBoolean("IndicationFeatures.MarkVanishedPlayersAsSpectators");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVanish(PostPlayerHideEvent e) {
        Player p = e.getPlayer();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!plugin.getVisibilityChanger().getHider().isHidden(p, onlinePlayer) && p != onlinePlayer) {
                sendPlayerInfoChangeGameModePacket(onlinePlayer, p, true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReappear(PlayerShowEvent e) {
        final Player p = e.getPlayer();
        for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!plugin.getVisibilityChanger().getHider().isHidden(p, onlinePlayer) && p != onlinePlayer) {
                delay(() -> sendPlayerInfoChangeGameModePacket(onlinePlayer, p, false));
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        delay(() -> {
            // tell others that p is a spectator
            if (plugin.getVanishStateMgr().isVanished(p.getUniqueId()))
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (!plugin.getVisibilityChanger().getHider().isHidden(p, onlinePlayer)
                            && p != onlinePlayer) {
                        sendPlayerInfoChangeGameModePacket(onlinePlayer, p, true);
                    }
                }
            // tell p that others are spectators
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!plugin.getVanishStateMgr().isVanished(onlinePlayer.getUniqueId())) continue;
                if (!plugin.getVisibilityChanger().getHider().isHidden(onlinePlayer, p)
                        && p != onlinePlayer) {
                    sendPlayerInfoChangeGameModePacket(p, onlinePlayer, true);
                }
            }
        });
    }

    @Override
    public void onEnable() {
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        try {
                            // multiple events share same packet object
                            event.setPacket(event.getPacket().shallowClone());
                            List<PlayerInfoData> infoDataList = new ArrayList<>(
                                    event.getPacket().getPlayerInfoDataLists().read(0));
                            Player receiver = event.getPlayer();
                            for (PlayerInfoData infoData : ImmutableList.copyOf(infoDataList)) {
                                try {
                                    if (!VanishIndication.this.plugin.getVisibilityChanger().getHider()
                                            .isHidden(infoData.getProfile().getUUID(), receiver)
                                            && VanishIndication.this.plugin.getVanishStateMgr()
                                            .isVanished(infoData.getProfile().getUUID())) {
                                        if (!receiver.getUniqueId().equals(infoData.getProfile().getUUID()))
                                            if (infoData.getGameMode() != EnumWrappers.NativeGameMode.SPECTATOR) {
                                                int latency;
                                                try {
                                                    latency = infoData.getLatency();
                                                } catch (NoSuchMethodError e) {
                                                    latency = 21;
                                                }
                                                if (event.getPacket().getPlayerInfoAction().read(0)
                                                        != EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE) {
                                                    continue;
                                                }
                                                PlayerInfoData newData = new PlayerInfoData(infoData.getProfile(),
                                                        latency, EnumWrappers.NativeGameMode.SPECTATOR,
                                                        infoData.getDisplayName());
                                                infoDataList.remove(infoData);
                                                infoDataList.add(newData);
                                            }
                                    }
                                } catch (UnsupportedOperationException ignored) {
                                }
                            }
                            event.getPacket().getPlayerInfoDataLists().write(0, infoDataList);
                        } catch (Exception | NoClassDefFoundError e) {
                            if (!suppressErrors) {
                                VanishIndication.this.plugin.logException(e);
                                plugin.getLogger().warning("IMPORTANT: Please make sure that you are using the latest " +
                                        "dev-build of ProtocolLib and that your server is up-to-date! This error likely " +
                                        "happened inside of ProtocolLib code which is out of SuperVanish's control. It's part " +
                                        "of an optional feature module and can be removed safely by disabling " +
                                        "MarkVanishedPlayersAsSpectators in the config file. Please report this " +
                                        "error if you can reproduce it on an up-to-date server with only latest " +
                                        "ProtocolLib and latest SV installed.");
                                suppressErrors = true;
                            }
                        }
                    }
                });
    }

    private void sendPlayerInfoChangeGameModePacket(Player p, Player change, boolean spectator) {
        PacketContainer packet = new PacketContainer(PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE);
        List<PlayerInfoData> data = new ArrayList<>();
        int ping = ThreadLocalRandom.current().nextInt(20) + 15;
        data.add(new PlayerInfoData(WrappedGameProfile.fromPlayer(change), ping,
                spectator ? EnumWrappers.NativeGameMode.SPECTATOR
                        : EnumWrappers.NativeGameMode.fromBukkit(change.getGameMode()),
                WrappedChatComponent.fromText(change.getPlayerListName())));
        packet.getPlayerInfoDataLists().write(0, data);
    ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
    }
}
