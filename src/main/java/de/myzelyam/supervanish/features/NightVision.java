/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.features;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.utils.FoliaUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NightVision extends Feature implements Runnable {

    public static final int INFINITE_POTION_EFFECT_LENGTH = 32767;

    private final Map<UUID, PotionEffect> playerPreviousPotionEffectMap = new ConcurrentHashMap<>();

    public NightVision(SuperVanish plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        FoliaUtil.runGlobalTimer(plugin, this, 20 * 60 * 2, 20 * 60 * 2);
    }

    @Override
    public boolean isActive() {
        return plugin.getSettings().getBoolean("InvisibilityFeatures.NightVisionEffect");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVanish(PlayerHideEvent e) {
        Player p = e.getPlayer();
        PotionEffect currentEffect = p.getPotionEffect(PotionEffectType.NIGHT_VISION);
        if (currentEffect != null) {
            playerPreviousPotionEffectMap.put(p.getUniqueId(), currentEffect);
        }
        sendAddPotionEffect(p);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReappear(PlayerShowEvent e) {
        Player p = e.getPlayer();
        sendRemovePotionEffect(p);
        if (playerPreviousPotionEffectMap.containsKey(p.getUniqueId())) {
            restorePreviousEffect(p);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId())) {
            if (playerPreviousPotionEffectMap.containsKey(p.getUniqueId())) {
                restorePreviousEffect(p);
            }
        } else {
            sendAddPotionEffect(p);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId()))
            return;
        sendRemovePotionEffect(p);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId()))
            return;
        if (e.getFrom().getWorld() == null || e.getTo() == null || e.getTo().getWorld() == null
                || e.getFrom().getWorld().getName().equals(e.getTo().getWorld().getName())) return;
        sendRemovePotionEffect(p);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId()))
            return;
        sendAddPotionEffect(p);
    }

    @Override
    public void run() {
        // renew every now and then to prevent blinking bug
        for (Player p : FoliaUtil.onlinePlayersSnapshot()) {
            if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId())) continue;
            sendRemovePotionEffect(p);
            sendAddPotionEffect(p);
        }
    }

    private void sendAddPotionEffect(Player p) {
        if (p == null) return;
        
        Runnable r = () -> {
            if (p.isOnline()) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,
                    INFINITE_POTION_EFFECT_LENGTH, 0, true, false));
            }
        };
        
        FoliaUtil.runAtEntity(plugin, p, r);
    }

    private void sendRemovePotionEffect(Player p) {
        if (p == null) return;
        
        Runnable r = () -> {
            if (p.isOnline()) {
                p.removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
        };
        
        FoliaUtil.runAtEntity(plugin, p, r);
    }

    private void restorePreviousEffect(Player p) {
        if (p == null) return;
        
        PotionEffect prev = playerPreviousPotionEffectMap.remove(p.getUniqueId());
        if (prev == null) return;
        
        Runnable r = () -> {
            if (p.isOnline()) {
                p.addPotionEffect(prev);
            }
        };
        
        FoliaUtil.runAtEntity(plugin, p, r);
    }
}
