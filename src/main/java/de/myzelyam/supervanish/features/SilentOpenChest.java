/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.features;

import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.hooks.OpenInvHook;
import de.myzelyam.supervanish.utils.FoliaUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.bukkit.Material.*;

public class SilentOpenChest extends Feature {

    private final Map<UUID, StateInfo> playerStateInfoMap = new ConcurrentHashMap<>();

    private final Collection<Material> additionalChestMaterials;

    public SilentOpenChest(SuperVanish plugin) {
        super(plugin);
        additionalChestMaterials = new ArrayList<>();
        additionalChestMaterials.addAll(Arrays.asList(BLACK_SHULKER_BOX, BLUE_SHULKER_BOX, BROWN_SHULKER_BOX,
                CYAN_SHULKER_BOX, GRAY_SHULKER_BOX, GREEN_SHULKER_BOX, LIGHT_BLUE_SHULKER_BOX,
                LIGHT_GRAY_SHULKER_BOX, LIME_SHULKER_BOX, MAGENTA_SHULKER_BOX, ORANGE_SHULKER_BOX,
                PINK_SHULKER_BOX, PURPLE_SHULKER_BOX, RED_SHULKER_BOX, SHULKER_BOX, WHITE_SHULKER_BOX,
                YELLOW_SHULKER_BOX, BARREL));
    }

    @Override
    public void onDisable() {
        for (UUID playerUuid : new ArrayList<>(playerStateInfoMap.keySet())) {
            Player p = plugin.getServer().getPlayer(playerUuid);
            StateInfo stateInfo = playerStateInfoMap.remove(playerUuid);
            if (stateInfo == null) continue;
            if (p == null) continue;
            FoliaUtil.runAtEntity(plugin, p, () -> restoreState(stateInfo, p));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSpectatorClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;
        Player p = (Player) e.getWhoClicked();
        if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId())) return;
        if (!playerStateInfoMap.containsKey(p.getUniqueId())) return;
        if (p.getGameMode() == GameMode.SPECTATOR) {
            e.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        StateInfo stateInfo = playerStateInfoMap.remove(p.getUniqueId());
        if (stateInfo == null) return;
        restoreState(stateInfo, p);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (playerStateInfoMap.containsKey(p.getUniqueId())
                && e.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReappear(PlayerShowEvent e) {
        Player p = e.getPlayer();
        StateInfo stateInfo = playerStateInfoMap.remove(p.getUniqueId());
        if (stateInfo == null) return;
        p.closeInventory();
        restoreState(stateInfo, p);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        StateInfo stateInfo = playerStateInfoMap.get(p.getUniqueId());
        if (stateInfo != null) {
            Location loc = e.getTo() != null ? e.getTo() : e.getFrom();
            if (stateInfo.openLoc.distance(loc) > .5) {
                p.closeInventory();
                restoreState(stateInfo, p);
                playerStateInfoMap.remove(p.getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onGameModeChange(PlayerGameModeChangeEvent e) {
        Player p = e.getPlayer();
        if (playerStateInfoMap.containsKey(p.getUniqueId()) && e.getNewGameMode() != GameMode.SPECTATOR) {
            // Don't let low-priority event listeners cancel the gamemode change
            if (e.isCancelled()) e.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId())
                || !p.hasPermission("sv.silentchest")) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (p.getGameMode() == GameMode.SPECTATOR) return;
        Material mainHandType = p.getInventory().getItemInMainHand().getType();
        if (p.isSneaking()
                && (mainHandType.isBlock() || mainHandType == ITEM_FRAME)
                && mainHandType != Material.AIR)
            return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (block.getType() == ENDER_CHEST) {
            e.setCancelled(true);
            p.openInventory(p.getEnderChest());
            return;
        }
        if (!(block.getType() == CHEST || block.getType() == TRAPPED_CHEST
                || additionalChestMaterials.contains(block.getType())))
            return;
        StateInfo stateInfo = StateInfo.extract(p);
        p.setVelocity(new Vector(0, 0, 0));
        playerStateInfoMap.put(p.getUniqueId(), stateInfo);
        p.setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player))
            return;
        final Player p = (Player) e.getPlayer();
        if (!playerStateInfoMap.containsKey(p.getUniqueId())) return;
        FoliaUtil.runAtEntity(plugin, p, () -> {
            StateInfo stateInfo = playerStateInfoMap.get(p.getUniqueId());
            if (stateInfo == null) return;
            restoreState(stateInfo, p);
            playerStateInfoMap.remove(p.getUniqueId());
        });
    }

    private void restoreState(StateInfo stateInfo, Player p) {
        if (!FoliaUtil.isOwnedByCurrentRegion(p)) {
            FoliaUtil.runAtEntity(plugin, p, () -> restoreState(stateInfo, p));
            return;
        }
        p.setGameMode(stateInfo.gameMode);
        p.teleportAsync(p.getLocation().add(0, 0.2, 0));
        FoliaUtil.runAtEntity(plugin, p, () -> {
            p.setAllowFlight(stateInfo.canFly);
            p.setFlying(stateInfo.isFlying);
        });
    }

    @Override
    public boolean isActive() {
        return plugin.getSettings().getBoolean("InvisibilityFeatures.OpenChestsSilently")
                && !(plugin.getPluginHookMgr() != null && plugin.getPluginHookMgr().isHookActive(OpenInvHook.class));
    }

    public boolean hasSilentlyOpenedChest(Player p) {
        return playerStateInfoMap.containsKey(p.getUniqueId());
    }

    public boolean hasSilentlyOpenedChest(UUID uuid) {
        return playerStateInfoMap.containsKey(uuid);
    }

    @Override
    public void onEnable() {
    }

    private static class StateInfo {

        private final boolean canFly, isFlying;
        private final GameMode gameMode;
        private final Location openLoc;

        StateInfo(boolean canFly, boolean isFlying, GameMode gameMode, Location openLoc) {
            this.canFly = canFly;
            this.isFlying = isFlying;
            this.gameMode = gameMode;
            this.openLoc = openLoc;
        }

        static StateInfo extract(Player p) {
            return new StateInfo(
                    p.getAllowFlight(),
                    p.isFlying(),
                    p.getGameMode(),
                    p.getLocation()
            );
        }
    }
}
