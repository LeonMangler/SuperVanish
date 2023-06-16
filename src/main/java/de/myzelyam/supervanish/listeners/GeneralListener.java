/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.listeners;

import com.destroystokyo.paper.event.entity.PlayerNaturallySpawnCreaturesEvent;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import com.destroystokyo.paper.event.entity.SkeletonHorseTrapEvent;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.VanishPlayer;
import de.myzelyam.supervanish.features.Broadcast;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.raid.RaidTriggerEvent;

import java.util.List;


public class GeneralListener implements Listener {

    private final SuperVanish plugin;

    private final FileConfiguration config;

    public GeneralListener(SuperVanish plugin) {
        this.plugin = plugin;
        config = plugin.getSettings();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent e) {
        try {
            if (!(e.getDamager() instanceof Player)) return;
            if (e.getEntity() == null) return;
            Player p = (Player) e.getDamager();
            if (plugin.getVanishStateMgr().isVanished(p.getUniqueId())) {
                if (config.getBoolean("RestrictiveOptions.PreventHittingEntities")
                        && !p.hasPermission("sv.damageentities") && !p.hasPermission("sv.damage")) {
                    plugin.sendMessage(p, "EntityHitDenied", p);
                    e.setCancelled(true);
                }
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent e) {
        try {
            Player p = e.getEntity();
            if (plugin.getVanishStateMgr().isVanished(p.getUniqueId())) {
                String deathMessage = e.getDeathMessage();
                e.setDeathMessage(null);
                if (deathMessage != null)
                    Broadcast.announceSilentDeath(p, plugin, deathMessage);
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTarget(EntityTargetEvent e) {
        try {
            if (!(e.getTarget() instanceof Player)) return;
            if (!config.getBoolean("InvisibilityFeatures.DisabledGameEvents.MobTarget")) return;
            Player p = (Player) e.getTarget();
            if (plugin.getVanishStateMgr().isVanished(p.getUniqueId())) {
                e.setCancelled(true);
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSculkSensorTrigger(BlockReceiveGameEvent e) {
        try {
            if (!config.getBoolean("InvisibilityFeatures.DisabledGameEvents.SculkSensor")) return;
            if (!(e.getEntity() instanceof Player)) return;
            Player p = (Player) e.getEntity();
            if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId())) return;
            e.setCancelled(true);
        } catch (Exception er) {
            plugin.logException(er);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRaidTrigger(RaidTriggerEvent e) {
        try {
            if (!config.getBoolean("InvisibilityFeatures.DisabledGameEvents.Raid")) return;
            Player p = e.getPlayer();
            if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId())) return;
            e.setCancelled(true);
        } catch (Exception er) {
            plugin.logException(er);
        }
    }

    @EventHandler
    public void onSkeletonHorseTrap(SkeletonHorseTrapEvent e) {
        try {
            if (!config.getBoolean("InvisibilityFeatures.DisabledGameEvents.SkeletonHorseTrap")) return;
            List<HumanEntity> humans = e.getEligibleHumans();
            int humansCount = humans.size();
            for (HumanEntity human : humans) {
                if (human instanceof Player) {
                    Player p = (Player) human;
                    if (plugin.getVanishStateMgr().isVanished(p.getUniqueId())) {
                        humansCount--;
                    }
                }
            }
            if (humansCount == 0)
                e.setCancelled(true);
        } catch (Exception er) {
            plugin.logException(er);
        }
    }

    @EventHandler
    public void onEntitySpawn(PlayerNaturallySpawnCreaturesEvent e) {
        try {
            if (!config.getBoolean("InvisibilityFeatures.DisabledGameEvents.NaturalMobSpawn")) return;
            if (plugin.getVanishStateMgr().isVanished(e.getPlayer().getUniqueId()))
                e.setCancelled(true);
        } catch (Exception er) {
            plugin.logException(er);
        }
    }
    @EventHandler
    public void onEntitySpawnerSpawn(PreSpawnerSpawnEvent e) {
        try {
            if (!config.getBoolean("InvisibilityFeatures.DisabledGameEvents.SpawnerMobSpawn")) return;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld().equals(e.getSpawnerLocation().getWorld()) &&
                        p.getLocation().distanceSquared(e.getSpawnerLocation()) <= 256 &&
                        p.getGameMode() != GameMode.SPECTATOR &&
                        !plugin.getVanishStateMgr().isVanished(p.getUniqueId()))
                    return;
            }
            e.setCancelled(true);
        } catch (Exception er) {
            plugin.logException(er);
        }
    }
}
