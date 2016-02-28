package de.myzelyam.supervanish.hooks;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.api.vanish.VanishAPI;
import me.kvq.plugin.trails.API.SuperTrailsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class SuperTrailsHook implements Listener {

    public SuperTrailsHook(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (VanishAPI.isInvisible(p)) {
                SuperTrailsAPI.HideTrailFor(p, true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVanish(PlayerHideEvent e) {
        SuperTrailsAPI.HideTrailFor(e.getPlayer(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReappear(PlayerShowEvent e) {
        SuperTrailsAPI.HideTrailFor(e.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        SuperTrailsAPI.HideTrailFor(e.getPlayer(),
                VanishAPI.isInvisible(e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        SuperTrailsAPI.HideTrailFor(e.getPlayer(),
                VanishAPI.isInvisible(e.getPlayer()));
    }
}
