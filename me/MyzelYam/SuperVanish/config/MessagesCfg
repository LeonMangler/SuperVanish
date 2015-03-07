package me.MyzelYam.SuperVanish.config;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import me.MyzelYam.SuperVanish.SuperVanish;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class MessagesCfg {

	public SuperVanish plugin = (SuperVanish) Bukkit.getPluginManager()
			.getPlugin("SuperVanish");

	private final String messagesFile;

	private File messages;

	private FileConfiguration fileConfiguration;

	public MessagesCfg() {
		this.messagesFile = "messages.yml";
		File dataFolder = plugin.getDataFolder();
		if (dataFolder == null)
			throw new IllegalStateException();
		this.messages = new File(plugin.getDataFolder(), messagesFile);
	}

	public void reloadConfig() {
		fileConfiguration = YamlConfiguration.loadConfiguration(messages);
		InputStream defConfigStream = plugin.getResource(messagesFile);
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration
					.loadConfiguration(new InputStreamReader(defConfigStream));
			fileConfiguration.setDefaults(defConfig);
		}
	}

	public FileConfiguration getConfig() {
		if (fileConfiguration == null) {
			this.reloadConfig();
		}
		return fileConfiguration;
	}

	public void saveDefaultConfig() {
		if (!messages.exists()) {
			this.plugin.saveResource(messagesFile, false);
		}
	}
}
