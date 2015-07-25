package de.myzelyam.supervanish.cmd;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.myzelyam.supervanish.SVUtils;

public class CmdVanish extends SVUtils {

	public CmdVanish(CommandSender s, String[] args, String label) {
		if (canDo(s, CommandAction.VANISH_SELF)) {
			Player p = (Player) s;
			if (args.length == 0) {
				if (getInvisiblePlayers().contains(p.getUniqueId().toString()))
					showPlayer(p);
				else
					hidePlayer(p);
			} else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("off")) {
					if (!getInvisiblePlayers().contains(
							p.getUniqueId().toString())) {
						p.sendMessage(convertString(
								getMsg("OnReappearWhileVisible"), p));
						return;
					}
					showPlayer(p);
				} else if (args[0].equalsIgnoreCase("on")) {
					if (getInvisiblePlayers().contains(
							p.getUniqueId().toString())) {
						p.sendMessage(convertString(
								getMsg("OnVanishWhileInvisible"), p));
						return;
					}
					hidePlayer(p);
				}
			}
		}
	}
}
