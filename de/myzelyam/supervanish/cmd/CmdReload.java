package de.myzelyam.supervanish.cmd;

import org.bukkit.command.CommandSender;

import de.myzelyam.supervanish.SVUtils;
import de.myzelyam.supervanish.config.ConfigCfg;
import de.myzelyam.supervanish.config.MessagesCfg;

public class CmdReload extends SVUtils {

	public CmdReload(CommandSender p, String[] args, String label) {
		if (canDo(p, CommandAction.RELOAD)) {
			// messages
			plugin.mcfg = new MessagesCfg();
			plugin.mcfg.saveDefaultConfig();
			plugin.msgs = plugin.mcfg.getConfig();
			// config
			plugin.ccfg = new ConfigCfg();
			plugin.ccfg.saveDefaultConfig();
			plugin.cfg = plugin.ccfg.getConfig();
			p.sendMessage(convertString(getMsg("ConfigReloadedMessage"), p));
		}
	}
}
