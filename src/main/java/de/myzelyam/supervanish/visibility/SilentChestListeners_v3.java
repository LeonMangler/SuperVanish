/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.visibility;

import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.api.vanish.VanishAPI;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.utils.OneDotEightUtils;
import de.myzelyam.supervanish.utils.ProtocolLibPacketUtils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class SilentChestListeners_v3 implements Listener {

    private final Map<Player, StateInfo> playerStateInfoMap = new HashMap<>();
    private final SuperVanish plugin;

    public SilentChestListeners_v3(SuperVanish plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSpectatorItemMove(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;
        Player p = (Player) e.getWhoClicked();
        if (OneDotEightUtils.isSpectator(p)) {
            if (VanishAPI.isInvisible(p)) {
                e.setCancelled(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        StateInfo stateInfo = playerStateInfoMap.remove(p);
        if (stateInfo == null) return;
        restoreState(stateInfo, p);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (playerStateInfoMap.containsKey(p)
                && e.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReappear(PlayerShowEvent e) {
        Player p = e.getPlayer();
        StateInfo stateInfo = playerStateInfoMap.remove(p);
        if (stateInfo == null) return;
        restoreState(stateInfo, p);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (playerStateInfoMap.containsKey(p)) {
            if (p.getOpenInventory().getType() != InventoryType.CHEST) {
                restoreState(playerStateInfoMap.get(p), p);
                playerStateInfoMap.remove(p);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onGameModeChange(PlayerGameModeChangeEvent e) {
        Player p = e.getPlayer();
        if (playerStateInfoMap.containsKey(p) && e.getNewGameMode() != GameMode.SPECTATOR) {
            // Don't let low-priority event listeners cancel the gamemode change
            if (e.isCancelled()) e.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!VanishAPI.isInvisible(p)) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (p.getGameMode() == GameMode.SPECTATOR) return;
        //noinspection deprecation
        if (p.isSneaking() && p.getItemInHand() != null
                && p.getItemInHand().getType().isBlock()
                && p.getItemInHand().getType() != Material.AIR)
            return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (!(block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST))
            return;
        StateInfo stateInfo = StateInfo.extract(p);
        playerStateInfoMap.put(p, stateInfo);
        p.setGameMode(GameMode.SPECTATOR);
        if (!stateInfo.hasSlowness && !stateInfo.isFlying && plugin.getProtocolLibPacketUtils() != null)
            plugin.getProtocolLibPacketUtils().sendAddPotionEffect(p,
                    new PotionEffect(PotionEffectType.SLOW,
                            ProtocolLibPacketUtils.INFINITE_POTION_DURATION, 0));

        // don't let the gamemode change move the player down
        if (!stateInfo.isFlying)
            p.teleport(p.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player))
            return;
        final Player p = (Player) e.getPlayer();
        if (!playerStateInfoMap.containsKey(p)) return;
        if (e.getInventory().getType() != InventoryType.CHEST) {
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                StateInfo stateInfo = playerStateInfoMap.get(p);
                if (stateInfo == null) return;
                restoreState(stateInfo, p);
                playerStateInfoMap.remove(p);
                // don't let the player glitch into the block below by sneaking
                if (p.isSneaking())
                    p.teleport(p.getLocation().add(0, 0.3, 0),
                            PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        }.runTaskLater(plugin, 1);
    }

    private void restoreState(StateInfo stateInfo, Player p) {
        p.setGameMode(stateInfo.gameMode);
        p.setAllowFlight(stateInfo.canFly);
        p.setFlying(stateInfo.isFlying);
        if (!stateInfo.hasSlowness && p.isOnline() && !stateInfo.isFlying
                && plugin.getProtocolLibPacketUtils() != null)
            plugin.getProtocolLibPacketUtils().sendRemovePotionEffect(p, PotionEffectType.SLOW);
    }

    private static class StateInfo {

        private final boolean canFly, isFlying, hasSlowness;
        private final GameMode gameMode;

        StateInfo(boolean canFly, boolean isFlying, boolean hasSlowness, GameMode gameMode) {
            this.canFly = canFly;
            this.isFlying = isFlying;
            this.hasSlowness = hasSlowness;
            this.gameMode = gameMode;
        }

        static StateInfo extract(Player p) {
            return new StateInfo(
                    p.getAllowFlight(),
                    p.isFlying(),
                    p.hasPotionEffect(PotionEffectType.SLOW),
                    p.getGameMode()
            );
        }
    }
}