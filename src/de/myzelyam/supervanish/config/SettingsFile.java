/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.config;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SettingsFile {

    private final String configFile;
    private SuperVanish plugin = (SuperVanish) Bukkit.getPluginManager()
            .getPlugin("SuperVanish");
    private File config;

    private FileConfiguration fileConfiguration;

    public SettingsFile() {
        this.configFile = "config.yml";
        File dataFolder = plugin.getDataFolder();
        if (dataFolder == null)
            throw new IllegalStateException();
        this.config = new File(plugin.getDataFolder(), configFile);
    }

    public void reloadConfig() {
        fileConfiguration = YamlConfiguration.loadConfiguration(config);
        InputStream defConfigStream = plugin.getResource(configFile);
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
        if (!config.exists()) {
            this.plugin.saveResource(configFile, false);
        }
    }
}
