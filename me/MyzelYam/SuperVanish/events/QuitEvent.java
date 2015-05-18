package me.MyzelYam.SuperVanish.events;

import java.util.List;

import me.MyzelYam.SuperVanish.SVUtils;
import me.MyzelYam.SuperVanish.hider.ActionBarManager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;

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
					showPlayer(p);
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
						&& cfg.getBoolean("Configuration.Messages.DisplayActionBarsToInvisiblePlayers")) {
					ActionBarManager.getInstance(plugin).removeActionBar(p);
				}
			}
		} catch (Exception er) {
			plugin.printException(er);
		}
	}
}