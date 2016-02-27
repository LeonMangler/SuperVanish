package de.myzelyam.supervanish.hider;

import de.myzelyam.api.vanish.VanishAPI;
import de.myzelyam.supervanish.SVUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerHider extends SVUtils {

    protected void showToAll(Player player) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            viewer.showPlayer(player);
        }
    }

    protected void hideToAll(Player player) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.hasPermission("sv.see")
                    || !settings.getBoolean("Configuration.Players.EnableSeePermission")) {
                viewer.hidePlayer(player);
            }
        }
    }

    protected void hideAllInvisibleTo(Player viewer) {
        if (viewer.hasPermission("sv.see")
                && settings.getBoolean("Configuration.Players.EnableSeePermission"))
            return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (VanishAPI.getInvisiblePlayers().contains(
                    player.getUniqueId().toString()))
                player.hidePlayer(player);
        }
    }
}
