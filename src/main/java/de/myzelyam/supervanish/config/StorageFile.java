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

public class StorageFile implements PluginFile<FileConfiguration> {

    private final SuperVanish plugin;
    private final String name;
    private File file;
    private FileConfiguration config;

    public StorageFile(String name, SuperVanish plugin) {
        this.name = name;
        this.plugin = plugin;
        setup();
    }

    @Override
    public String getName() {
        return name;
    }

    private void setup() {
        file = new File(plugin.getDataFolder().getPath() + File.separator
                + name + ".yml");
        config = YamlConfiguration.loadConfiguration(file);
        save();
    }

    @Override
    public void reload() {
        setup();
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    @Override
    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
