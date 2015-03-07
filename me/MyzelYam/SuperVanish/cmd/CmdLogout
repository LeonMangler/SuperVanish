package me.MyzelYam.SuperVanish.cmd;

import me.MyzelYam.SuperVanish.SVUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class CmdLogout extends SVUtils {

	public CmdLogout(CommandSender p, String[] args, String label) {
		if (canDo(p, CommandAction.LOGOUT)) {
			Bukkit.broadcastMessage(convertString(getMsg("VanishMessage"), p));
		}
	}
}
