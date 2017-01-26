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
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.comphenix.protocol.PacketType.Play.Server.PLAYER_INFO;

public class TablistPacketMgr extends PacketAdapter {

    private final SuperVanish plugin;

    private final FileConfiguration settings;

    private boolean dontHandle = false;

    public TablistPacketMgr(SuperVanish plugin) {
        //noinspection deprecation
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO);
        this.plugin = plugin;
        settings = plugin.settings;
    }

    public void registerListener() {
        if (!settings.getBoolean("Configuration.Tablist.MarkVanishedPlayersAsSpectators")) return;
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        try {
            if (dontHandle) return;
            List<PlayerInfoData> data = new ArrayList<>(
                    event.getPacket().getPlayerInfoDataLists().read(0));
            int originalSize = data.size();
            Player receiver = event.getPlayer();
            Collection<Player> vanishedPlayers = this.plugin.getOnlineInvisiblePlayers();
            for (PlayerInfoData infoData : new ArrayList<>(data)) {
                for (Player vanishedTabPlayer : vanishedPlayers) {
                    if (infoData.getProfile().getUUID().toString()
                            .equals(vanishedTabPlayer.getUniqueId().toString())) {
                        // if default has changed because of previous modifications of the list then simply
                        // override it using the actual gamemode
                        if (receiver.getUniqueId().toString().equals
                                (vanishedTabPlayer.getUniqueId().toString())) {
                            PlayerInfoData newData = new PlayerInfoData(infoData.getProfile(), infoData
                                    .getLatency(), NativeGameMode.fromBukkit(vanishedTabPlayer.getGameMode()),
                                    infoData.getDisplayName());
                            data.remove(infoData);
                            data.add(newData);
                            continue;
                        }
                        if (receiver.canSee(vanishedTabPlayer)) {
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
            }
            event.getPacket().getPlayerInfoDataLists().write(0, data);
        } catch (Exception ex) {
            this.plugin.printException(ex);
        }
    }

    public void sendGameModeChangePacket(Player receiver, Player change, boolean markAsHidden) {
        if (!settings.getBoolean("Configuration.Tablist.MarkVanishedPlayersAsSpectators")) return;
        dontHandle = true;
        PacketContainer packet = new PacketContainer(PLAYER_INFO);
        // action
        packet.getPlayerInfoAction().write(0, PlayerInfoAction.UPDATE_GAME_MODE);
        List<PlayerInfoData> data = new ArrayList<>();
        int ping = getPing(receiver);
        GameMode gameMode = markAsHidden ? GameMode.SPECTATOR : change.getGameMode();
        data.add(new PlayerInfoData(WrappedGameProfile.fromPlayer(change), ping,
                NativeGameMode.fromBukkit(gameMode),
                WrappedChatComponent.fromText(change.getPlayerListName())));
        // data
        packet.getPlayerInfoDataLists().write(0, data);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(receiver, packet);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Cannot send packet", e);
        }
        dontHandle = false;
    }

    private int getPing(Player p) {
        try {
            Class<?> craftPlayer = Class.forName("org.bukkit.craftbukkit."
                    + getServerVersion() + ".entity.CraftPlayer");
            Method getHandle = craftPlayer.getMethod("getHandle");
            Object entityPlayer = getHandle.invoke(p);
            return (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
        } catch (NoSuchMethodException | InvocationTargetException | ClassNotFoundException
                | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }
}