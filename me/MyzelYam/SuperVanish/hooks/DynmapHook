package me.MyzelYam.SuperVanish.hooks;

import me.MyzelYam.SuperVanish.SuperVanish;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.dynmap.bukkit.DynmapPlugin;

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
