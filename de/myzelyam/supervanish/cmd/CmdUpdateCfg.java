package de.myzelyam.supervanish.cmd;

import java.io.File;

import org.bukkit.command.CommandSender;

import de.myzelyam.supervanish.SVUtils;

public class CmdUpdateCfg extends SVUtils {

	public CmdUpdateCfg(CommandSender p, String[] args, String label) {
		if (canDo(p, CommandAction.UPDATE_CFG)) {
			// get changes and updates
			String updates = "";
			String changes = "";
			if (plugin.requiresCfgUpdate && plugin.requiresMsgsUpdate) {
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
				if ((!plugin.requiresCfgUpdate) && (!plugin.requiresMsgsUpdate)) {
					p.sendMessage(convertString(
							getMsg("NoConfigUpdateAvailableMessage"), p));
					return;
				}
				p.sendMessage(convertString(getMsg("UpdateWarningMessage"), p)
						.replace("%changes", changes).replace("%updates",
								updates));
				return;
			}
			if (args[1].equalsIgnoreCase("confirm")) {
				if ((!plugin.requiresCfgUpdate) && (!plugin.requiresMsgsUpdate)) {
					p.sendMessage(convertString(
							getMsg("NoConfigUpdateAvailableMessage"), p));
					return;
				}
				// delete necessary files
				if (plugin.requiresCfgUpdate) {
					File file = new File(plugin.getDataFolder().getPath()
							+ File.separator + "config.yml");
					file.delete();
					plugin.ccfg.saveDefaultConfig();
				}
				if (plugin.requiresMsgsUpdate) {
					File file2 = new File(plugin.getDataFolder().getPath()
							+ File.separator + "messages.yml");
					file2.delete();
					plugin.mcfg.saveDefaultConfig();
				}
				p.sendMessage(convertString(getMsg("UpdatedConfigMessage"), p)
						.replace("%changes", changes).replace("%updates",
								updates));
				// update update-information
				plugin.checkConfig();
			}
		}
	}
}
