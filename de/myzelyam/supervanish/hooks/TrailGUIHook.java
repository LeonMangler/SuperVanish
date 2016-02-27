package de.myzelyam.supervanish.hooks;

import ca.jamiesinn.trailgui.Listeners;
import ca.jamiesinn.trailgui.Main;
import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public abstract class TrailGUIHook {

    public static void replaceMoveListener() {
        final Plugin trailGUI = Bukkit.getPluginManager().getPlugin("TrailGUI");
        if (trailGUI == null || !(trailGUI instanceof Main)) {
            Bukkit.getLogger().log(Level.WARNING,
                    "[SuperVanish] Failed to hook into TrailGUI. (PluginNotFound)");
            return;
        }
        PlayerMoveEvent.getHandlerList().unregister(trailGUI);
        final Listeners trailGUIListeners = new Listeners((Main) trailGUI);
        trailGUI.getServer().getPluginManager().registerEvents(new Listener() {

            @EventHandler
            public void onPlayerMove(PlayerMoveEvent event) {
                if (Bukkit.getPluginManager().isPluginEnabled("SuperVanish")
                        && VanishAPI.getInvisiblePlayers().contains(
                        event.getPlayer().getUniqueId().toString()))
                    return;
                trailGUIListeners.onPlayerMove(event);
            }
        }, trailGUI);
    }
}
