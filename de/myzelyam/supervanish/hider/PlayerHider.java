package de.myzelyam.supervanish.hider;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.myzelyam.api.vanish.VanishAPI;
import de.myzelyam.supervanish.SVUtils;

public class PlayerHider extends SVUtils {

	protected void showToAll(Player p) {
		for (Player ap2 : Bukkit.getOnlinePlayers()) {
			ap2.showPlayer(p);
		}
	}

	protected void hideToAll(Player p) {
		for (Player ap2 : Bukkit.getOnlinePlayers()) {
			if (ap2.hasPermission("sv.see")
					&& cfg.getBoolean("Configuration.Players.EnableSeePermission")) {
			} else {
				ap2.hidePlayer(p);
			}
		}
	}

	protected void hideAllInvisibleTo(Player p) {
		if (p.hasPermission("sv.see")
				&& cfg.getBoolean("Configuration.Players.EnableSeePermission"))
			return;
		for (Player ap2 : Bukkit.getOnlinePlayers()) {
			if (VanishAPI.getInvisiblePlayers().contains(
					ap2.getUniqueId().toString()))
				p.hidePlayer(ap2);
		}
	}
}
