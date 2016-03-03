package de.myzelyam.supervanish.hider;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class TabMgr {

    private final SuperVanish plugin;

    private FileConfiguration playerData;

    public TabMgr(SuperVanish plugin) {
        this.plugin = plugin;
        playerData = plugin.playerData;
        enabled = plugin.settings.getBoolean("Configuration.Tablist.ChangeTabNames");
    }

    private boolean enabled;

    public void adjustTabname(Player p, TabAction action) {
        if (action == TabAction.RESTORE_NORMAL_TABNAME)
            restoreNormalTabname(p);
        else
            setCustomTabname(p);
    }

    private void restoreNormalTabname(Player p) {
        String ntn = loadData(p);
        if (ntn == null)
            return;
        p.setPlayerListName(plugin.convertString(ntn, p));
    }

    private void setCustomTabname(Player p) {
        if (!enabled)
            return;
        String tn = plugin.getMsg("TabName");
        if (tn != null) {
            storeData(p);
            StringBuilder sb = new StringBuilder(plugin.convertString(tn, p));
            if (plugin.convertString(tn, p).length() > 16)
                sb.setLength(16);
            else
                sb.setLength(plugin.convertString(tn, p).length());
            p.setPlayerListName(sb.toString());
        }
    }

    public enum TabAction {
        RESTORE_NORMAL_TABNAME, SET_CUSTOM_TABNAME
    }

    public void storeData(Player p) {
        playerData.set("PlayerData." + p.getUniqueId().toString()
                + ".normalTabName", p.getPlayerListName());
        plugin.savePlayerData();
    }

    public String loadData(Player p) {
        return playerData.getString("PlayerData." + p.getUniqueId().toString()
                + ".normalTabName");
    }
}
