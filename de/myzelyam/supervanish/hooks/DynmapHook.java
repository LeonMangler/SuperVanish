package de.myzelyam.supervanish.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.dynmap.bukkit.DynmapPlugin;

import de.myzelyam.supervanish.SuperVanish;

public abstract class DynmapHook {

	public static SuperVanish plugin = (SuperVanish) Bukkit.getPluginManager()
			.getPlugin("SuperVanish");

	public static void adjustVisibility(Player p, boolean hide) {
		try {
			DynmapPlugin plugin = (DynmapPlugin) Bukkit.getPluginManager()
					.getPlugin("dynmap");
			if (hide) {
				plugin.setPlayerVisiblity(p.getName(), false);
				plugin.sendBroadcastToWeb("", p.getName() + " quit");
			} else {
				plugin.setPlayerVisiblity(p.getName(), true);
				plugin.sendBroadcastToWeb("", p.getName() + " joined");
			}
		} catch (Exception e) {
			plugin.printException(e);
		}
	}
}
