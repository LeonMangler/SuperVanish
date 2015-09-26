package de.myzelyam.supervanish;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import de.myzelyam.supervanish.hider.VisibilityAdjuster;

public class SVUtils {

	protected static SuperVanish plugin = (SuperVanish) Bukkit.getServer()
			.getPluginManager().getPlugin("SuperVanish");

	protected static FileConfiguration cfg;

	protected static FileConfiguration pd;

	protected List<String> getInvisiblePlayers() {
		return pd.getStringList("InvisiblePlayers");
	}

	public void hidePlayer(Player p) {
		VisibilityAdjuster.getInstance().hidePlayer(p);
	}

	public void showPlayer(Player p) {
		VisibilityAdjuster.getInstance().showPlayer(p);
	}

	public void showPlayer(Player p, boolean hideJoinMsg) {
		VisibilityAdjuster.getInstance().showPlayer(p, hideJoinMsg);
	}

	protected boolean isHidden(Player p) {
		return getInvisiblePlayers().contains(p.getUniqueId().toString());
	}

	public SVUtils() {
		if (plugin == null)
			throw new RuntimeException("Plugin cannot be null!");
		cfg = plugin.cfg;
		pd = plugin.pd;
	}

	protected boolean canDo(CommandSender p, CommandAction cmd) {
		if (!(p instanceof Player))
			if (!cmd.canConsole()) {
				p.sendMessage(convertString(getMsg("InvalidSenderMessage"), p));
				return false;
			}
		if (!p.hasPermission(cmd.getPerm())) {
			p.sendMessage(convertString(getMsg("NoPermissionMessage"), p));
			return false;
		}
		return true;
	}

	protected enum CommandAction {
		VANISH_SELF("sv.use", false), VANISH_OTHER("sv.others", true), LIST(
				"sv.list", true), LOGIN("sv.login", false), LOGOUT("sv.logout",
				false), TOGGLE_ITEM_PICKUPS("sv.toggleitempickups", false), UPDATE_CFG(
				"sv.updatecfg", true), RELOAD("sv.reload", true);

		private String perm;

		private boolean console;

		CommandAction(String perm, boolean console) {
			this.perm = perm;
			this.console = console;
		}

		String getPerm() {
			return perm;
		}

		boolean canConsole() {
			return console;
		}
	}

	protected String getMsg(String msg) {
		return plugin.getMsg(msg);
	}

	public String convertString(String message, CommandSender p) {
		return plugin.convertString(message, p);
	}
}