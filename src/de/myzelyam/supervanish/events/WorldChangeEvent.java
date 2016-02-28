package de.myzelyam.supervanish.events;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.hider.PlayerHider;
import de.myzelyam.supervanish.hider.TabManager;
import de.myzelyam.supervanish.hider.TabManager.SVTabAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WorldChangeEvent extends PlayerHider implements Listener {

    private static WorldChangeEvent instance;
    private SuperVanish plugin = (SuperVanish) Bukkit.getServer()
            .getPluginManager().getPlugin("SuperVanish");
    private int hideDelay;
    private int invisibilityDelay;
    private int tabDelay;

    private WorldChangeEvent() {
        // init compatibility delays
        hideDelay = settings
                .getInt("Configuration.CompatibilityOptions.ActionDelay.HideDelayOnWorldChangeInTicks");
        if (!settings.getBoolean("Configuration.CompatibilityOptions.ActionDelay.Enable"))
            hideDelay = 0;
        invisibilityDelay = settings
                .getInt("Configuration.CompatibilityOptions.ActionDelay.InvisibilityPotionDelayOnWorldChangeInTicks");
        if (!settings.getBoolean("Configuration.CompatibilityOptions.ActionDelay.Enable"))
            invisibilityDelay = 0;
        tabDelay = settings
                .getInt("Configuration.CompatibilityOptions.ActionDelay.TabNameChangeDelayOnWorldChangeInTicks");
        if (!settings.getBoolean("Configuration.CompatibilityOptions.ActionDelay.Enable"))
            tabDelay = 0;
    }

    public static WorldChangeEvent getInstance() {
        if (instance == null)
            instance = new WorldChangeEvent();
        return instance;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        try {
            final Player p = e.getPlayer();
            if (!isHidden(p))
                return;
            // check auto-reappear-option
            if (settings.getBoolean("Configuration.Players.ReappearOnWorldChange")) {
                showPlayer(p);
                return;
            }
            // re-hide
            if (hideDelay > 0) {
                Bukkit.getServer().getScheduler()
                        .scheduleSyncDelayedTask(plugin, new Runnable() {

                            @Override
                            public void run() {
                                hideToAll(p);
                            }
                        }, hideDelay);
            } else {
                hideToAll(p);
            }
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
                                    TabManager.getInstance().adjustTabname(p,
                                            SVTabAction.SET_CUSTOM_TABNAME);
                                }
                            }, tabDelay);
                } else {
                    TabManager.getInstance().adjustTabname(p,
                            SVTabAction.SET_CUSTOM_TABNAME);
                }
            }
        } catch (Exception er) {
            plugin.printException(er);
        }
    }
}