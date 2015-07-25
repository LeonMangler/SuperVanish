package de.myzelyam.supervanish.cmd;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.myzelyam.supervanish.SVUtils;

public class CmdToggleItemPickups extends SVUtils {

	public CmdToggleItemPickups(CommandSender s, String[] args, String label) {
		if (canDo(s, CommandAction.TOGGLE_ITEM_PICKUPS)) {
			Player p = (Player) s;
			p.sendMessage(convertString(getMsg("ToggledPickingUpItems"
					+ (toggleState(p) ? "On" : "Off")), p));
		}
	}

	private boolean toggleState(Player p) {
		boolean hasEnabled = pd.getBoolean("PlayerData."
				+ p.getUniqueId().toString() + ".itemPickUps");
		pd.set("PlayerData." + p.getUniqueId().toString() + ".itemPickUps",
				!hasEnabled);
		plugin.spd();
		return !hasEnabled;
	}
}
