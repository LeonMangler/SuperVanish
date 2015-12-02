package de.myzelyam.supervanish.hooks;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import de.myzelyam.api.vanish.VanishAPI;

import ca.jamiesinn.trailgui.Listeners;
import ca.jamiesinn.trailgui.Main;

public abstract class TrailGUIHook {

	public static void replaceMoveListener() {
		final Plugin trailgui = Bukkit.getPluginManager().getPlugin("TrailGUI");
		if (trailgui == null || !(trailgui instanceof Main)) {
			Bukkit.getLogger().log(Level.WARNING,
					"[SuperVanish] Failed to hook into TrailGUI. (PluginNotFound)");
			return;
		}
		PlayerMoveEvent.getHandlerList().unregister(trailgui);
		final Listeners trailGUIListeners = new Listeners((Main) trailgui);
		trailgui.getServer().getPluginManager().registerEvents(new Listener() {

			@EventHandler
			public void onPlayerMove(PlayerMoveEvent event) {
				if (Bukkit.getPluginManager().isPluginEnabled("SuperVanish")
						&& VanishAPI.getInvisiblePlayers().contains(
								event.getPlayer().getUniqueId().toString()))
					return;
				trailGUIListeners.onPlayerMove(event);
			}
		}, trailgui);
	}
}
