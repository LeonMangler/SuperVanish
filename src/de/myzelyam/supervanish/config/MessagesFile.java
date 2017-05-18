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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class MessagesFile {

    private final String fileName;
    private SuperVanish plugin = (SuperVanish) Bukkit.getPluginManager()
            .getPlugin("SuperVanish");
    private File messages;

    private FileConfiguration fileConfiguration, defaultFileConfiguration;

    public MessagesFile() {
        this.fileName = "messages.yml";
        File dataFolder = plugin.getDataFolder();
        if (dataFolder == null)
            throw new IllegalStateException();
        this.messages = new File(plugin.getDataFolder(), fileName);
        try (Reader reader = new InputStreamReader(plugin.getResource(fileName))) {
            defaultFileConfiguration = YamlConfiguration.loadConfiguration(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        fileConfiguration = YamlConfiguration.loadConfiguration(messages);
        InputStream defConfigStream = plugin.getResource(fileName);
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

    public FileConfiguration getDefaultConfig() {
        return defaultFileConfiguration;
    }

    public void saveDefaultConfig() {
        if (!messages.exists()) {
            this.plugin.saveResource(fileName, false);
        }
    }
}
