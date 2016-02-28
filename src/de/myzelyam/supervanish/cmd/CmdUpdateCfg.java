package de.myzelyam.supervanish.cmd;

import de.myzelyam.supervanish.SVUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.File;

public class CmdUpdateCfg extends SVUtils {

    public CmdUpdateCfg(CommandSender player, String[] args, String label) {
        if (canDo(player, CommandAction.UPDATE_CFG)) {
            // get changes and updates
            String updates;
            String changes;
            if (plugin.requiresCfgUpdate && plugin.requiresMsgUpdate) {
                updates = "the config and the messages";
                changes = "all settings and messages";
            } else if (plugin.requiresCfgUpdate) {
                updates = "the config";
                changes = "all settings";
            } else {
                updates = "the messages";
                changes = "all messages";
            }
            if (args.length != 2) {
                if ((!plugin.requiresCfgUpdate) && (!plugin.requiresMsgUpdate)) {
                    player.sendMessage(convertString(
                            getMsg("NoConfigUpdateAvailableMessage"), player));
                    return;
                }
                player.sendMessage(convertString(getMsg("UpdateWarningMessage"), player)
                        .replace("%changes", changes).replace("%updates",
                                updates));
                return;
            }
            if (args[1].equalsIgnoreCase("confirm")) {
                if (!plugin.requiresCfgUpdate && !plugin.requiresMsgUpdate) {
                    player.sendMessage(convertString(
                            getMsg("NoConfigUpdateAvailableMessage"), player));
                    return;
                }
                // delete necessary files
                boolean success = true;
                if (plugin.requiresCfgUpdate) {
                    File configFile = new File(plugin.getDataFolder().getPath()
                            + File.separator + "config.yml");
                    success = configFile.delete();
                    plugin.settingsFile.saveDefaultConfig();
                    plugin.settingsFile.reloadConfig();
                    plugin.settings = plugin.settingsFile.getConfig();
                }
                if (plugin.requiresMsgUpdate) {
                    File messagesFile = new File(plugin.getDataFolder().getPath()
                            + File.separator + "messages.yml");
                    success &= messagesFile.delete();
                    plugin.messagesFile.saveDefaultConfig();
                    plugin.messagesFile.reloadConfig();
                    plugin.messages = plugin.messagesFile.getConfig();
                }
                if (success)
                    player.sendMessage(convertString(getMsg("UpdatedConfigMessage"), player)
                            .replace("%changes", changes).replace("%updates",
                                    updates));
                else
                    player.sendMessage(ChatColor.DARK_RED + "[PV] ERROR: Failed to delete old config file.");
                // update update-information
                plugin.checkConfig();
            }
        }
    }
}
