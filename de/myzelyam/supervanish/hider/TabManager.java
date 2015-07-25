package de.myzelyam.supervanish.hider;

import org.bukkit.entity.Player;

import de.myzelyam.supervanish.SVUtils;

public class TabManager extends SVUtils {

	private boolean enabled;

	private static TabManager instance;

	public static TabManager getInstance() {
		if (instance == null)
			instance = new TabManager();
		return instance;
	}

	private TabManager() {
		enabled = cfg.getBoolean("Configuration.Tablist.ChangeTabNames");
	}

	public void adjustTabname(Player p, SVTabAction action) {
		if (action == SVTabAction.RESTORE_NORMAL_TABNAME)
			restoreNormalTabname(p);
		else
			setCustomTabname(p);
	}

	private void restoreNormalTabname(Player p) {
		String ntn = TabData.loadData(p);
		if (ntn == null)
			return;
		p.setPlayerListName(convertString(ntn, p));
	}

	private void setCustomTabname(Player p) {
		if (!enabled)
			return;
		String tn = getMsg("TabName");
		if (tn != null) {
			TabData.storeData(p);
			StringBuffer sb = new StringBuffer(convertString(tn, p));
			if (convertString(tn, p).length() > 16)
				sb.setLength(16);
			else
				sb.setLength(convertString(tn, p).length());
			p.setPlayerListName(sb.toString());
		}
	}

	private static class TabData extends SVUtils {

		private static void storeData(Player p) {
			pd.set("PlayerData." + p.getUniqueId().toString()
					+ ".normalTabName", p.getPlayerListName());
			plugin.spd();
		}

		private static String loadData(Player p) {
			return pd.getString("PlayerData." + p.getUniqueId().toString()
					+ ".normalTabName");
		}
	}

	public enum SVTabAction {
		RESTORE_NORMAL_TABNAME, SET_CUSTOM_TABNAME;
	}
}
