/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.events;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;

import java.util.Collection;

public class QuitEvent implements EventExecutor, Listener {

    private final SuperVanish plugin;

    public QuitEvent(SuperVanish plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration getSettings() {
        return plugin.settings;
    }

    @Override
    public void execute(Listener listener, Event event) throws EventException {
        try {
            if (event instanceof PlayerQuitEvent) {
                PlayerQuitEvent e = (PlayerQuitEvent) event;
                FileConfiguration config = plugin.getConfig();
                Collection<Player> onlineInvisiblePlayers = plugin.getOnlineInvisiblePlayers();
                Player p = e.getPlayer();
                // check auto-reappear option
                if (getSettings().getBoolean("Configuration.Players.ReappearOnQuit")
                        && onlineInvisiblePlayers.contains(p)) {
                    plugin.getVisibilityAdjuster().showPlayer(p, true);
                    if (getSettings().getBoolean("Configuration.Players.ReappearOnQuitHandleLeaveMsg")
                            && config
                            .getBoolean("Configuration.Messages.HideNormalJoinAndLeaveMessagesWhileInvisible")) {
                        e.setQuitMessage(null);
                    }
                    return;
                }
                // check remove-quit-msg option
                if (config
                        .getBoolean("Configuration.Messages.HideNormalJoinAndLeaveMessagesWhileInvisible")
                        && onlineInvisiblePlayers.contains(p)) {
                    e.setQuitMessage(null);
                }
                // remove action bar
                if (plugin.getServer().getPluginManager()
                        .getPlugin("ProtocolLib") != null
                        && getSettings().getBoolean("Configuration.Messages.DisplayActionBarsToInvisiblePlayers")
                        && !SuperVanish.SERVER_IS_ONE_DOT_SEVEN) {
                    plugin.getActionBarMgr().removeActionBar(p);
                }
            }
        } catch (Exception er) {
            plugin.printException(er);
        }
    }
}