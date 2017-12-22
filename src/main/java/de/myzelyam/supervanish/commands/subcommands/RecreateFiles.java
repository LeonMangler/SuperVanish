/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.commands.subcommands;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.commands.CommandAction;
import de.myzelyam.supervanish.commands.SubCommand;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class RecreateFiles extends SubCommand {

    public RecreateFiles(SuperVanish plugin) {
        super(plugin);
    }

    @Override
    public void execute(Command cmd, CommandSender p, String[] args, String label) {
        if (canDo(p, CommandAction.RECREATE_FILES, true)) {
            String updates;
            String changes;
            if (plugin.getConfigMgr().isSettingsUpdateRequired()
                    && plugin.getConfigMgr().isMessagesUpdateRequired()) {
                updates = "the config and the messages file";
                changes = "all settings and messages";
            } else if (plugin.getConfigMgr().isSettingsUpdateRequired()) {
                updates = "the config";
                changes = "all settings";
            } else {
                updates = "the messages file";
                changes = "all messages";
            }
            if (args.length != 2) {
                if (!plugin.getConfigMgr().isSettingsUpdateRequired()
                        && !plugin.getConfigMgr().isMessagesUpdateRequired()) {
                    plugin.sendMessage(p, "NoConfigUpdateAvailable", p);
                    return;
                }
                plugin.sendMessage(p, plugin.getMessage("UpdateWarning").replace("%changes%", changes)
                        .replace("%updates%", updates), p);
                return;
            }
            if (args[1].equalsIgnoreCase("confirm") || args[1].equalsIgnoreCase("force")) {
                if (!plugin.getConfigMgr().isSettingsUpdateRequired()
                        && !plugin.getConfigMgr().isMessagesUpdateRequired()
                        && !args[1].equalsIgnoreCase("force")) {
                    plugin.sendMessage(p, "NoConfigUpdateAvailable", p);
                    return;
                }
                if (args[1].equalsIgnoreCase("force")) {
                    updates = "the config and the messages file";
                    changes = "all settings and messages";
                }
                // delete necessary files
                boolean success = false;
                if (plugin.getConfigMgr().isSettingsUpdateRequired()
                        || args[1].equalsIgnoreCase("force")) {
                    File file = new File(plugin.getDataFolder().getPath() + File.separator + "config.yml");
                    success = file.delete();
                    plugin.getConfigMgr().getSettingsFile().save();
                    plugin.getConfigMgr().getSettingsFile().reload();
                    plugin.getConfigMgr().setSettings(plugin.getConfigMgr().getSettingsFile().getConfig());
                }
                if (plugin.getConfigMgr().isMessagesUpdateRequired()
                        || args[1].equalsIgnoreCase("force")) {
                    File file2 = new File(plugin.getDataFolder().getPath() + File.separator + "messages.yml");
                    success &= file2.delete();
                    plugin.getConfigMgr().getMessagesFile().save();
                    plugin.getConfigMgr().getMessagesFile().reload();
                    plugin.getConfigMgr().setMessages(plugin.getConfigMgr().getMessagesFile().getConfig());

                }
                if (!success) {
                    p.sendMessage(ChatColor.RED +
                            "Cannot update config, failed to delete file. Are the file permissions valid?");
                    return;
                }
                plugin.sendMessage(p, plugin.getMessage("RecreatedConfig")
                        .replace("%changes%", changes)
                        .replace("%updates%", updates), p);
                // update update-information
                plugin.getConfigMgr().checkFilesForLeftOvers();
            } else if (args[1].equalsIgnoreCase("dismiss")) {
                String currentVersion = plugin.getDescription().getVersion();
                boolean isDismissed =
                        plugin.getPlayerData().getBoolean("PlayerData." + (p instanceof Player ? ((Player) p)
                                .getUniqueId()
                                .toString() : "CONSOLE") + ".dismissed." + currentVersion.replace(".", "_"), false);
                plugin.getPlayerData().set("PlayerData." + (p instanceof Player ? ((Player) p).getUniqueId().toString()
                        : "CONSOLE") + ".dismissed." + currentVersion.replace(".", "_"), !isDismissed);
                plugin.getConfigMgr().getPlayerDataFile().save();
                if (!isDismissed)
                    plugin.sendMessage(p, "DismissedRecreationWarning", p);
                else
                    plugin.sendMessage(p, "UndismissedRecreationWarning", p);
            }
        }
    }
}
