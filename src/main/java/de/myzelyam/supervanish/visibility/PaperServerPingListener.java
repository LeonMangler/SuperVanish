package de.myzelyam.supervanish.visibility;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PaperServerPingListener implements Listener {

    private boolean errorLogged = false;

    private final SuperVanish plugin;

    public PaperServerPingListener(SuperVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onServerListPing(PaperServerListPingEvent e) {
        try {
            final FileConfiguration settings = plugin.getSettings();
            if (!settings.getBoolean("ExternalInvisibility.ServerList.AdjustAmountOfOnlinePlayers")
                    && !settings.getBoolean("ExternalInvisibility.ServerList.AdjustListOfLoggedInPlayers"))
                return;
            Collection<UUID> onlineVanishedPlayers = plugin.getVanishStateMgr().getOnlineVanishedPlayers();
            int vanishedPlayersCount = onlineVanishedPlayers.size(),
                    playerCount = Bukkit.getOnlinePlayers().size();
            if (settings.getBoolean("ExternalInvisibility.ServerList.AdjustAmountOfOnlinePlayers")) {
                e.setNumPlayers(playerCount - vanishedPlayersCount);
            }
            if (settings.getBoolean("ExternalInvisibility.ServerList.AdjustListOfLoggedInPlayers")) {
                List<PlayerProfile> playerSample = e.getPlayerSample();

                playerSample.removeIf(profile -> onlineVanishedPlayers.contains(profile.getId()));
            }
        } catch (Exception er) {
            if (!errorLogged) {
                plugin.logException(er);
                errorLogged = true;
            }
        }
    }
}
