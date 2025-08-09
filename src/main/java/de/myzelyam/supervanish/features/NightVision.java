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
import io.github.projectunified.minelib.scheduler.common.util.Platform;
import io.github.projectunified.minelib.scheduler.global.GlobalScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NightVision extends Feature implements Runnable {

    public static final int INFINITE_POTION_EFFECT_LENGTH = 32767;

    private final Map<UUID, PotionEffect> playerPreviousPotionEffectMap = new HashMap<>();

    public NightVision(SuperVanish plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        GlobalScheduler.get(plugin).runTimer(this, 20 * 60 * 2, 20 * 60 * 2);
    }

    @Override
    public boolean isActive() {
        return plugin.getSettings().getBoolean("InvisibilityFeatures.NightVisionEffect");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVanish(PlayerHideEvent e) {
        Player p = e.getPlayer();
        // getPotionEffect(..) is 1.10+ API
        if (plugin.getVersionUtil().isOneDotXOrHigher(10)) {
            if (p.getPotionEffect(PotionEffectType.NIGHT_VISION) != null) {
                playerPreviousPotionEffectMap.put(p.getUniqueId(), p.getPotionEffect(PotionEffectType.NIGHT_VISION));
            }
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
        for (UUID uuid : plugin.getVanishStateMgr().getOnlineVanishedPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            sendRemovePotionEffect(p);
            sendAddPotionEffect(p);
        }
    }

    private void sendAddPotionEffect(Player p) {
        Runnable r = () -> p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,
                INFINITE_POTION_EFFECT_LENGTH, 0, true, false));
        if (Platform.FOLIA.isPlatform()) {
            p.getScheduler().run(plugin, task -> r.run(), () -> {});
        } else {
            r.run();
        }
    }

    private void sendRemovePotionEffect(Player p) {
        Runnable r = () -> p.removePotionEffect(PotionEffectType.NIGHT_VISION);
        if (Platform.FOLIA.isPlatform()) {
            p.getScheduler().run(plugin, task -> r.run(), () -> {});
        } else {
            r.run();
        }
    }

    private void restorePreviousEffect(Player p) {
        PotionEffect prev = playerPreviousPotionEffectMap.remove(p.getUniqueId());
        if (prev == null) return;
        Runnable r = () -> p.addPotionEffect(prev);
        if (Platform.FOLIA.isPlatform()) {
            p.getScheduler().run(plugin, task -> r.run(), () -> {});
        } else {
            r.run();
        }
    }
}
