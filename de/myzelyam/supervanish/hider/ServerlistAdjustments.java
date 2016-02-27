package de.myzelyam.supervanish.hider;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import de.myzelyam.supervanish.SVUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ServerlistAdjustments extends SVUtils {

    public static void setupProtocolLib() {
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
                                WrappedServerPing ping = e.getPacket()
                                        .getServerPings().read(0);
                                List<String> invisiblePlayers = playerData
                                        .getStringList("InvisiblePlayers");
                                int invisiblePlayersCount = 0;
                                int onlinePlayersCount = Bukkit.getOnlinePlayers().size();
                                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                    if (invisiblePlayers.contains(onlinePlayer.getUniqueId().toString())) {
                                        invisiblePlayersCount++;
                                    }
                                }
                                if (settings.getBoolean("Configuration.Serverlist.AdjustAmountOfOnlinePlayers")) {
                                    ping.setPlayersOnline(onlinePlayersCount - invisiblePlayersCount);
                                }
                                if (settings.getBoolean("Configuration.Serverlist.AdjustListOfLoggedInPlayers")) {
                                    List<WrappedGameProfile> wrappedGameProfiles = new ArrayList<>();
                                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                        WrappedGameProfile profile = WrappedGameProfile
                                                .fromPlayer(onlinePlayer);
                                        if (!invisiblePlayers.contains(onlinePlayer.getUniqueId()
                                                .toString()))
                                            wrappedGameProfiles.add(profile);
                                    }
                                    ping.setPlayers(wrappedGameProfiles);
                                }
                            }
                        } catch (Exception er) {
                            ServerlistAdjustments.plugin.printException(er);
                        }
                    }
                });
    }
}