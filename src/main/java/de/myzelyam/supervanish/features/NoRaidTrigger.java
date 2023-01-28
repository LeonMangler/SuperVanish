package de.myzelyam.supervanish.features;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.raid.RaidEvent;
import org.bukkit.event.raid.RaidTriggerEvent;

public class NoRaidTrigger extends Feature {

    public NoRaidTrigger(SuperVanish plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(RaidTriggerEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId())) return;
        e.setCancelled(true);
    }

    @Override
    public boolean isActive() {
        return plugin.getSettings().getBoolean("InvisibilityFeatures.PreventRaidTriggering", true);
    }
}
