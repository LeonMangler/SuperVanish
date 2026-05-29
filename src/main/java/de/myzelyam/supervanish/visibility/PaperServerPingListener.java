package de.myzelyam.supervanish.visibility;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.utils.FoliaUtil;
import de.myzelyam.supervanish.utils.OneTimeWarning;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PaperServerPingListener implements Listener {

    private final SuperVanish plugin;
    private final OneTimeWarning warning;

    public PaperServerPingListener(SuperVanish plugin) {
        this.plugin = plugin;
        warning = new OneTimeWarning(plugin, null);
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
                    playerCount = FoliaUtil.onlinePlayersSnapshot().size();
            if (settings.getBoolean("ExternalInvisibility.ServerList.AdjustAmountOfOnlinePlayers")) {
                e.setNumPlayers(playerCount - vanishedPlayersCount);
            }
            if (settings.getBoolean("ExternalInvisibility.ServerList.AdjustListOfLoggedInPlayers")) {
                List<PlayerProfile> playerSample = e.getPlayerSample();

                playerSample.removeIf(profile -> onlineVanishedPlayers.contains(profile.getId()));
            }
        } catch (Exception er) {
            warning.log(er);
        }
    }
}
