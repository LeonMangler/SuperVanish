package de.myzelyam.supervanish.listeners;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.VanishPlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class PickupListener implements Listener {

    private final SuperVanish plugin;

    private final FileConfiguration config;

    public PickupListener(SuperVanish plugin) {
        this.plugin = plugin;
        config = plugin.getSettings();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemPickUp(PlayerPickupItemEvent e) {
        handlePickupEvent(e);
    }
    @EventHandler
    public void onExperienceOrbPickup(PlayerPickupExperienceEvent e) {
        handlePickupEvent(e);
    }

    private void handlePickupEvent(PlayerEvent e) {
        try {
            Cancellable cancellable = (Cancellable) e;
            VanishPlayer vanishPlayer = plugin.getVanishPlayer(e.getPlayer());
            if (vanishPlayer == null || !vanishPlayer.isOnlineVanished()) return;
            if (!vanishPlayer.hasItemPickUpsEnabled())
                cancellable.setCancelled(true);
            if (plugin.getSettings().getBoolean("RestrictiveOptions.PreventModifyingOwnInventory")
                    && !vanishPlayer.getPlayer().hasPermission("sv.modifyowninv")) {
                cancellable.setCancelled(true);
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }

}
