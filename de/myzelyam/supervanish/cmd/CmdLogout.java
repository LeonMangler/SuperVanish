package de.myzelyam.supervanish.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import de.myzelyam.supervanish.SVUtils;

public class CmdLogout extends SVUtils {

	public CmdLogout(CommandSender p, String[] args, String label) {
		if (canDo(p, CommandAction.LOGOUT)) {
			Bukkit.broadcastMessage(convertString(getMsg("VanishMessage"), p));
		}
	}
}
