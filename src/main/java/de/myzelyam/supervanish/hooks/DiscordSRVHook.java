package de.myzelyam.supervanish.hooks;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.supervanish.SuperVanish;
import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class DiscordSRVHook extends PluginHook {
    public DiscordSRVHook(SuperVanish superVanish) {
        super(superVanish);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVanish(PlayerHideEvent e) {
        DiscordSRV.getPlugin().sendJoinMessage(e.getPlayer(), e.getPlayer().getName() + "left the game");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReappear(PlayerShowEvent e) {
        DiscordSRV.getPlugin().sendJoinMessage(e.getPlayer(), e.getPlayer().getName() + " joined the game");
    }
}
