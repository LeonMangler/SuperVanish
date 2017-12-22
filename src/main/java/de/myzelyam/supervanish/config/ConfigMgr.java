/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.config;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.logging.Level;

import lombok.Data;

import static de.myzelyam.supervanish.SuperVanish.*;

@Data
public class ConfigMgr {

    private final SuperVanish plugin;
    private final FileMgr fileMgr;
    private boolean settingsUpdateRequired, messagesUpdateRequired;
    private FileConfiguration settings, messages, playerData;
    private ConfigurableFile messagesFile, settingsFile;
    private StorageFile playerDataFile;

    public ConfigMgr(SuperVanish plugin) {
        this.plugin = plugin;
        fileMgr = new FileMgr(plugin);
    }

    @SuppressWarnings("unchecked")
    public void prepareFiles() {
        // messages
        messagesFile = (ConfigurableFile) fileMgr.addFile("messages", FileMgr.FileType.CONFIG);
        messages = messagesFile.getConfig();
        // settings
        settingsFile = (ConfigurableFile) fileMgr.addFile("config", FileMgr.FileType.CONFIG);
        settings = settingsFile.getConfig();
        // data
        playerDataFile = (StorageFile) fileMgr.addFile("data", FileMgr.FileType.STORAGE);
        playerData = playerDataFile.getConfig();
        playerData.addDefault("InvisiblePlayers", Collections.emptyList());
        playerData.options().copyDefaults(true);
        playerData.options().header("SuperVanish v" + plugin.getDescription().getVersion() + " - Data file");
        playerDataFile.save();

        checkFilesForLeftOvers();
    }

    public void checkFilesForLeftOvers() {
        try {
            String currentSettingsVersion = settings.getString("ConfigVersion");
            String newestVersion = plugin.getDescription().getVersion();
            String currentMessagesVersion = messages.getString("MessagesVersion");
            messagesUpdateRequired = fileRequiresRecreation(currentMessagesVersion, false);
            settingsUpdateRequired = fileRequiresRecreation(currentSettingsVersion, true);
            if (newestVersion.equals(currentSettingsVersion))
                settingsUpdateRequired = false;
            if (newestVersion.equals(currentMessagesVersion))
                messagesUpdateRequired = false;
            if (settingsUpdateRequired || messagesUpdateRequired) {
                String currentVersion = plugin.getDescription().getVersion();
                boolean isDismissed = playerData.getBoolean("PlayerData.CONSOLE.dismissed."
                        + currentVersion.replace(".", "_"), false);
                if (!isDismissed) plugin.log(Level.WARNING, "At least one config file is outdated, " +
                        "it's recommended to regenerate it using '/sv recreatefiles'");
            }
            if (currentSettingsVersion.startsWith("1.5.") || currentSettingsVersion.startsWith("1.4.")) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "You have a very outdated " +
                        "config file, your settings will not work until you regenerate your SV-files " +
                        "using /sv recreatefiles");
            }
        } catch (Exception e) {
            plugin.logException(e);
        }
    }

    private boolean fileRequiresRecreation(String currentVersion, boolean isSettingsFile) {
        if (currentVersion == null) return true;
        for (String ignoredVersion : isSettingsFile ? NON_REQUIRED_SETTINGS_UPDATES
                : NON_REQUIRED_MESSAGES_UPDATES) {
            if (currentVersion.equalsIgnoreCase(ignoredVersion)) return false;
        }
        return true;
    }

    public FileMgr getFileMgr() {
        return fileMgr;
    }
}
