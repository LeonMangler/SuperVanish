package de.myzelyam.supervanish.features;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class NoAdvancementEarn extends Feature {
    public NoAdvancementEarn(SuperVanish plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAdvancementCriterionGrant(PlayerAdvancementCriterionGrantEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId())) return;
        e.setCancelled(true);
    }

    @Override
    public boolean isActive() {
        return plugin.getSettings().getBoolean("InvisibilityFeatures.PreventAdvancementEarning", true);
    }
}


