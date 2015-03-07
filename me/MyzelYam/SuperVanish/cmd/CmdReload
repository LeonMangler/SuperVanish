package me.MyzelYam.SuperVanish.cmd;

import me.MyzelYam.SuperVanish.SVUtils;
import me.MyzelYam.SuperVanish.config.ConfigCfg;
import me.MyzelYam.SuperVanish.config.MessagesCfg;

import org.bukkit.command.CommandSender;

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
