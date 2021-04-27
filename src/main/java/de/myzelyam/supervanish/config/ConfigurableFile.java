/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.config;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class ConfigurableFile implements PluginFile<FileConfiguration> {

    private final SuperVanish plugin;
    private final String name;
    private File file;
    private FileConfiguration fileConfiguration;
    private FileConfiguration defaultFileConfiguration;

    public ConfigurableFile(String name, SuperVanish plugin) {
        this.name = name + ".yml";
        this.plugin = plugin;
        setup();
    }

    @Override
    public String getName() {
        return name;
    }

    private void setup() {
        file = new File(plugin.getDataFolder(), name);
        try (Reader reader = new InputStreamReader(plugin.getResource(name))) {
            defaultFileConfiguration = YamlConfiguration.loadConfiguration(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        save();
    }

    @Override
    public void reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            reload();
        }
        return fileConfiguration;
    }

    @Override
    public void save() {
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
    }

    public FileConfiguration getDefaultConfig() {
        return defaultFileConfiguration;
    }
}
