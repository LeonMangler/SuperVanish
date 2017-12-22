/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.config;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.SuperVanishPlugin;

import java.util.HashMap;
import java.util.Map;

public class FileMgr {

    private final SuperVanishPlugin plugin;
    private Map<String, PluginFile<?>> files;

    public FileMgr(SuperVanishPlugin plugin) {
        this.plugin = plugin;
        files = new HashMap<>();
    }

    public PluginFile<?> addFile(String name, FileType type) {
        if (name == null)
            throw new IllegalArgumentException("The file name cannot be null!");
        if (type == FileType.STORAGE) {
            StorageFile file = new StorageFile(name, (SuperVanish) plugin);
            files.put(name, file);
            return file;
        } else if (type == FileType.CONFIG) {
            ConfigurableFile file = new ConfigurableFile(name, (SuperVanish) plugin);
            files.put(name, file);
            return file;
        } else {
            throw new IllegalArgumentException("The FileType cannot be null!");
        }
    }

    public void reloadFile(String fileName) {
        PluginFile<?> file = files.get(fileName);
        if (file != null)
            file.reload();
        else
            throw new IllegalArgumentException("Specified file doesn't exist!");
    }

    public void reloadAll() {
        for (String fileName : files.keySet())
            reloadFile(fileName);
    }

    public enum FileType {
        STORAGE, CONFIG
    }
}
