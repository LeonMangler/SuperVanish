package de.myzelyam.supervanish.events;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.hider.TabMgr.TabAction;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

public class WorldChangeEvent implements Listener {

    private final SuperVanish plugin;
    private final FileConfiguration settings;

    public WorldChangeEvent(SuperVanish plugin) {
        this.plugin = plugin;
        this.settings = plugin.settings;
        // init compatibility delays
        invisibilityDelay = settings
                .getInt("Configuration.CompatibilityOptions.ActionDelay.InvisibilityPotionDelayOnWorldChangeInTicks");
        if (!settings.getBoolean("Configuration.CompatibilityOptions.ActionDelay.Enable"))
            invisibilityDelay = 0;
        tabDelay = settings
                .getInt("Configuration.CompatibilityOptions.ActionDelay.TabNameChangeDelayOnWorldChangeInTicks");
        if (!settings.getBoolean("Configuration.CompatibilityOptions.ActionDelay.Enable"))
            tabDelay = 0;
    }

    private int invisibilityDelay;
    private int tabDelay;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        try {
            final Player p = e.getPlayer();
            Collection<Player> onlineInvisiblePlayers = plugin.getOnlineInvisiblePlayers();
            if (!onlineInvisiblePlayers.contains(p))
                return;
            // check auto-reappear-option
            if (settings.getBoolean("Configuration.Players.ReappearOnWorldChange")) {
                plugin.getVisibilityAdjuster().showPlayer(p);
                return;
            }
            // re-hide
            plugin.getVisibilityAdjuster().getHider().hideToAll(p);
            // re-add invisibility
            if (settings.getBoolean("Configuration.Players.EnableGhostPlayers")) {
                boolean isInvisible = false;
                for (PotionEffect potionEffect : p.getActivePotionEffects())
                    if (potionEffect.getType() == PotionEffectType.INVISIBILITY) isInvisible = true;
                if (!isInvisible) {
                    if (invisibilityDelay > 0) {
                        Bukkit.getServer().getScheduler()
                                .scheduleSyncDelayedTask(plugin, new Runnable() {

                                    @Override
                                    public void run() {
                                        p.addPotionEffect(new PotionEffect(
                                                PotionEffectType.INVISIBILITY,
                                                Integer.MAX_VALUE, 1));
                                    }
                                }, invisibilityDelay);
                    } else {
                        p.addPotionEffect(new PotionEffect(
                                PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
                    }
                }
            }
            // re-add night vision (removed in teleport event)
            if (settings.getBoolean("Configuration.Players.AddNightVision"))
                p.addPotionEffect(new PotionEffect(
                        PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
            // re-adjust tablist name
            if (settings.getBoolean("Configuration.Tablist.ChangeTabNames")) {
                if (tabDelay > 0) {
                    Bukkit.getServer().getScheduler()
                            .scheduleSyncDelayedTask(plugin, new Runnable() {

                                @Override
                                public void run() {
                                    plugin.getTabMgr().adjustTabname(p,
                                            TabAction.SET_CUSTOM_TABNAME);
                                }
                            }, tabDelay);
                } else {
                    plugin.getTabMgr().adjustTabname(p,
                            TabAction.SET_CUSTOM_TABNAME);
                }
            }
        } catch (Exception er) {
            plugin.printException(er);
        }
    }
}