package de.myzelyam.supervanish.events;

import de.myzelyam.supervanish.SVUtils;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.hider.ActionBarManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;

import java.util.List;

public class QuitEvent extends SVUtils implements EventExecutor, Listener {

    @Override
    public void execute(Listener listener, Event event) throws EventException {
        try {
            if (event instanceof PlayerQuitEvent) {
                PlayerQuitEvent e = (PlayerQuitEvent) event;
                FileConfiguration config = plugin.getConfig();
                List<String> invisiblePlayers = playerData.getStringList("InvisiblePlayers");
                Player p = e.getPlayer();
                // check auto-reappear option
                if (SVUtils.settings.getBoolean("Configuration.Players.ReappearOnQuit")
                        && isHidden(p)) {
                    showPlayer(p, true);
                    if (SVUtils.settings.getBoolean("Configuration.Players.ReappearOnQuitHandleLeaveMsg")
                            && config
                            .getBoolean("Configuration.Messages.HideNormalJoinAndLeaveMessagesWhileInvisible")) {
                        e.setQuitMessage(null);
                    }
                    return;
                }
                // check remove-quit-msg option
                if (config
                        .getBoolean("Configuration.Messages.HideNormalJoinAndLeaveMessagesWhileInvisible")
                        && invisiblePlayers.contains(p.getUniqueId().toString())) {
                    e.setQuitMessage(null);
                }
                // remove action bar
                if (plugin.getServer().getPluginManager()
                        .getPlugin("ProtocolLib") != null
                        && SVUtils.settings.getBoolean("Configuration.Messages.DisplayActionBarsToInvisiblePlayers")
                        && !SuperVanish.SERVER_IS_ONE_DOT_SEVEN) {
                    ActionBarManager.getInstance(plugin).removeActionBar(p);
                }
            }
        } catch (Exception er) {
            plugin.printException(er);
        }
    }
}