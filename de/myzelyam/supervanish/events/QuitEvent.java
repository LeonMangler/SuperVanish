package de.myzelyam.supervanish.events;

import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;

import de.myzelyam.supervanish.SVUtils;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.hider.ActionBarManager;

public class QuitEvent extends SVUtils implements EventExecutor, Listener {

	@Override
	public void execute(Listener l, Event event) throws EventException {
		try {
			if (event instanceof PlayerQuitEvent) {
				PlayerQuitEvent e = (PlayerQuitEvent) event;
				FileConfiguration config = plugin.getConfig();
				List<String> vpl = pd.getStringList("InvisiblePlayers");
				Player p = e.getPlayer();
				// check auto-reappear-option
				if (cfg.getBoolean("Configuration.Players.ReappearOnQuit")
						&& isHidden(p)) {
					// remove from vanished list
					vpl.remove(p.getUniqueId().toString());
					plugin.pd.set("InvisiblePlayers", vpl);
					plugin.spd();
					// remove leave msg?
					if (cfg.getBoolean("Configuration.Players.ReappearOnQuitHandleLeaveMsg")
							&& config
									.getBoolean("Configuration.Messages.HideNormalJoinAndLeaveMessagesWhileInvisible")) {
						e.setQuitMessage(null);
					}
					return;
				}
				// check remove-quit-msg-option
				if (config
						.getBoolean("Configuration.Messages.HideNormalJoinAndLeaveMessagesWhileInvisible")
						&& vpl.contains(p.getUniqueId().toString())) {
					e.setQuitMessage(null);
				}
				// remove action bar
				if (plugin.getServer().getPluginManager()
						.getPlugin("ProtocolLib") != null
						&& cfg.getBoolean("Configuration.Messages.DisplayActionBarsToInvisiblePlayers")
						&& !SuperVanish.SERVER_IS_ONE_DOT_SEVEN) {
					ActionBarManager.getInstance(plugin).removeActionBar(p);
				}
			}
		} catch (Exception er) {
			plugin.printException(er);
		}
	}
}