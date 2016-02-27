package de.myzelyam.supervanish.hider;

import de.myzelyam.supervanish.SVUtils;
import org.bukkit.entity.Player;

public class TabManager extends SVUtils {

    private static TabManager instance;
    private boolean enabled;

    private TabManager() {
        enabled = settings.getBoolean("Configuration.Tablist.ChangeTabNames");
    }

    public static TabManager getInstance() {
        if (instance == null)
            instance = new TabManager();
        return instance;
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
            StringBuilder sb = new StringBuilder(convertString(tn, p));
            if (convertString(tn, p).length() > 16)
                sb.setLength(16);
            else
                sb.setLength(convertString(tn, p).length());
            p.setPlayerListName(sb.toString());
        }
    }

    public enum SVTabAction {
        RESTORE_NORMAL_TABNAME, SET_CUSTOM_TABNAME;
    }

    private static class TabData extends SVUtils {

        private static void storeData(Player p) {
            playerData.set("PlayerData." + p.getUniqueId().toString()
                    + ".normalTabName", p.getPlayerListName());
            plugin.savePlayerData();
        }

        private static String loadData(Player p) {
            return playerData.getString("PlayerData." + p.getUniqueId().toString()
                    + ".normalTabName");
        }
    }
}
