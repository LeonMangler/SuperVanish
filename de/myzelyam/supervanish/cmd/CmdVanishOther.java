package de.myzelyam.supervanish.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.myzelyam.supervanish.SVUtils;

public class CmdVanishOther extends SVUtils {

	public CmdVanishOther(CommandSender s, String[] args, String label) {
		if (canDo(s, CommandAction.VANISH_OTHER)) {
			if (args.length < 2) {
				s.sendMessage(convertString(getMsg("InvalidUsageMessage"), s));
				return;
			}
			boolean hide = false;
			switch (args[0]) {
			case "on":
				hide = true;
			}
			Player p = Bukkit.getPlayer(args[1]);
			if (p == null) {
				s.sendMessage(convertString(getMsg("PlayerNotOnlineMessage"), s));
				return;
			}
			// check
			if (hide
					&& getInvisiblePlayers().contains(
							p.getUniqueId().toString())) {
				s.sendMessage(convertString(getMsg("AlreadyInvisibleMessage"),
						p));
				return;
			} else if (!hide
					&& !getInvisiblePlayers().contains(
							p.getUniqueId().toString())) {
				s.sendMessage(convertString(getMsg("AlreadyVisibleMessage"), p));
				return;
			}
			// hide
			if (hide) {
				hidePlayer(p);
				s.sendMessage(convertString(getMsg("HideOtherMessage"), p));
				// show
			} else {
				showPlayer(p);
				s.sendMessage(convertString(getMsg("ShowOtherMessage"), p));
			}
		}
	}
}
