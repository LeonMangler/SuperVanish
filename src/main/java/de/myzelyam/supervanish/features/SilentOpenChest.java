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

import ca.spottedleaf.concurrentutil.map.SWMRHashTable;
import java.util.*;

import static org.bukkit.Material.*;

public class SilentOpenChest extends Feature {

    private final Map<UUID, StateInfo> playerStateInfoMap = new SWMRHashTable<>();

    private final Set<Material> additionalChestMaterials;

    public SilentOpenChest(SuperVanish plugin) {
        super(plugin);
        additionalChestMaterials = EnumSet.noneOf(Material.class);
        if (plugin.getVersionUtil().isOneDotXOrHigher(11)) {
            try {
                additionalChestMaterials.addAll(Arrays.asList(BLACK_SHULKER_BOX, BLUE_SHULKER_BOX, BROWN_SHULKER_BOX,
                        CYAN_SHULKER_BOX, GRAY_SHULKER_BOX, GREEN_SHULKER_BOX, LIGHT_BLUE_SHULKER_BOX,
                        LIME_SHULKER_BOX, MAGENTA_SHULKER_BOX, ORANGE_SHULKER_BOX, PINK_SHULKER_BOX,
                        PURPLE_SHULKER_BOX, RED_SHULKER_BOX, WHITE_SHULKER_BOX,
                        YELLOW_SHULKER_BOX));
                try {
                    additionalChestMaterials.add(LIGHT_GRAY_SHULKER_BOX);
                } catch (NoSuchFieldError e) {
                    // old name
                    additionalChestMaterials.add(Material.valueOf("SILVER_SHULKER_BOX"));
                }
                try {
                    additionalChestMaterials.add(SHULKER_BOX);
                } catch (NoSuchFieldError ignored) {
                    // no standard shulker box in old versions
                }
                if (plugin.getVersionUtil().isOneDotXOrHigher(14)) {
                    additionalChestMaterials.add(Material.valueOf("BARREL"));
                }
            } catch (NoSuchFieldError | IllegalArgumentException ignored) {
                // no shulker box support in very old versions
            }
        }
    }

    @Override
    public void onDisable() {
        for (UUID id : new ArrayList<>(playerStateInfoMap.keySet())) {
            StateInfo stateInfo = playerStateInfoMap.remove(id);
            if (stateInfo == null) continue;
            Player p = plugin.getServer().getPlayer(id);
            if (p != null) restoreState(stateInfo, p);
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
        UUID id = p.getUniqueId();
        StateInfo stateInfo = playerStateInfoMap.get(id);
        if (stateInfo == null) return;
        Location loc = e.getTo() != null ? e.getTo() : e.getFrom();
        if (stateInfo.openLoc.distanceSquared(loc) > 0.25D) {
            p.closeInventory();
            restoreState(stateInfo, p);
            playerStateInfoMap.remove(id);
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
        // Use modern main-hand API; ItemStack is never null on modern versions
        if (p.isSneaking()) {
            final Material handType = p.getInventory().getItemInMainHand().getType();
            if (handType != Material.AIR && (handType.isBlock() || handType == ITEM_FRAME)) {
                return;
            }
        }
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (block.getType() == ENDER_CHEST) {
            e.setCancelled(true);
            p.openInventory(p.getEnderChest());
            return;
        }
        if (!(block.getType() == CHEST || block.getType() == TRAPPED_CHEST
                || plugin.getVersionUtil().isOneDotXOrHigher(11) && additionalChestMaterials.contains(block.getType())))
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
        final UUID id = p.getUniqueId();
        if (!playerStateInfoMap.containsKey(id)) return;
        SuperVanish.getScheduler().runTaskLater(() -> {
            StateInfo stateInfo = playerStateInfoMap.get(id);
            if (stateInfo == null) return;
            restoreState(stateInfo, p);
            playerStateInfoMap.remove(id);
        }, 1);
    }

    private void restoreState(StateInfo stateInfo, Player p) {
        p.setGameMode(stateInfo.gameMode);
        p.teleport(p.getLocation().add(0, 0.2, 0));
        SuperVanish.getScheduler().runTaskLater(() -> {
            p.setAllowFlight(stateInfo.canFly);
            p.setFlying(stateInfo.isFlying);
        }, 1);
    }

    @Override
    public boolean isActive() {
        return plugin.getSettings().getBoolean("InvisibilityFeatures.OpenChestsSilently")
                && !(plugin.getPluginHookMgr() != null && plugin.getPluginHookMgr().isHookActive(OpenInvHook.class));
    }

    public boolean hasSilentlyOpenedChest(Player p) {
        return playerStateInfoMap.containsKey(p.getUniqueId());
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
