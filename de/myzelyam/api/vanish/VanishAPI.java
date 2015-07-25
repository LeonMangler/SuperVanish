package de.myzelyam.api.vanish;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.myzelyam.supervanish.SVUtils;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.config.ConfigCfg;
import de.myzelyam.supervanish.config.MessagesCfg;

public class VanishAPI {

	private static SuperVanish plugin;

	static {
		Plugin bplugin = Bukkit.getPluginManager().getPlugin("SuperVanish");
		if (bplugin == null || !(bplugin instanceof SuperVanish)) {
			Bukkit.getConsoleSender()
					.sendMessage(
							"§c[SuperVanish] A plugin will fail to use the api, since SuperVanish isn't loaded!");
			Bukkit.getConsoleSender()
					.sendMessage(
							"§c[SuperVanish] The author should add SuperVanish as a (soft-)dependency to the plugin.yml file");
			Bukkit.getConsoleSender()
					.sendMessage(
							"§c[SuperVanish] to make sure SuperVanish is loaded when trying to use the api!");
			throw new RuntimeException("API is unavailable!");
		}
		plugin = (SuperVanish) bplugin;
	}

	/**
	 * @return A Stringlist of the UUID's of all hidden players
	 */
	public static List<String> getInvisiblePlayers() {
		return plugin.pd.getStringList("InvisiblePlayers");
	}

	/**
	 * @param p
	 *            - the player.
	 * @return TRUE if the player is invisible, FALSE otherwise.
	 */
	public static boolean isInvisible(Player p) {
		if (p == null)
			return false;
		return plugin.pd.getStringList("InvisiblePlayers").contains(
				p.getUniqueId().toString());
	}

	/**
	 * Hides a player using SuperVanish
	 * 
	 * @param p
	 *            - the player.
	 */
	public static void hidePlayer(Player p) {
		new SVUtils().hidePlayer(p);
	}

	/**
	 * * Shows a player using SuperVanish
	 * 
	 * @param p
	 *            - the player.
	 */
	public static void showPlayer(Player p) {
		new SVUtils().showPlayer(p);
	}

	public static FileConfiguration getConfiguration() {
		return plugin.cfg;
	}

	public static FileConfiguration getMessages() {
		return plugin.msgs;
	}

	public static FileConfiguration getPlayerData() {
		return plugin.pd;
	}

	public static void reloadConfig() {
		// messages
		plugin.mcfg = new MessagesCfg();
		plugin.mcfg.saveDefaultConfig();
		plugin.msgs = plugin.mcfg.getConfig();
		// config
		plugin.ccfg = new ConfigCfg();
		plugin.ccfg.saveDefaultConfig();
		plugin.cfg = plugin.ccfg.getConfig();
	}
}