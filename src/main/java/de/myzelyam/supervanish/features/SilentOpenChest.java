/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.features;

import com.comphenix.protocol.ProtocolLibrary;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.supervanish.SuperVanish;
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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static org.bukkit.Material.*;

public class SilentOpenChest extends Feature {

    private final Map<Player, StateInfo> playerStateInfoMap = new HashMap<>();

    private final Collection<Material> additionalChestMaterials;

    public SilentOpenChest(SuperVanish plugin) {
        super(plugin);
        additionalChestMaterials = new ArrayList<>();
        if (plugin.getVersionUtil().isOneDotXOrHigher(11)) {
            try {
                //noinspection unused
                InventoryType testInvType = InventoryType.SHULKER_BOX;
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
        for (Player p : playerStateInfoMap.keySet()) {
            StateInfo stateInfo = playerStateInfoMap.remove(p);
            if (stateInfo == null) continue;
            restoreState(stateInfo, p);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSpectatorClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;
        Player p = (Player) e.getWhoClicked();
        if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId())) return;
        if (!playerStateInfoMap.containsKey(p)) return;
        if (p.getGameMode() == GameMode.SPECTATOR) {
            e.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        StateInfo stateInfo = playerStateInfoMap.remove(p);
        if (stateInfo == null) return;
        restoreState(stateInfo, p);
        playerStateInfoMap.remove(p);
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
        p.closeInventory();
        restoreState(stateInfo, p);
        playerStateInfoMap.remove(p);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (playerStateInfoMap.containsKey(p)) {
            Location loc = e.getTo() != null ? e.getTo() : e.getFrom();
            if (playerStateInfoMap.get(p).openLoc.distance(loc) > .5) {
                p.closeInventory();
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
        if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId())
                || !p.hasPermission("sv.silentchest")) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (p.getGameMode() == GameMode.SPECTATOR) return;
        // Remember to keep "p.getItemInHand() != null" as we can't ensure that older Spigot versions will always return a non-null value
        //noinspection deprecation,ConstantConditions
        if (p.isSneaking() && p.getItemInHand() != null && p.getItemInHand().getType() != Material.AIR)
            return;
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
        playerStateInfoMap.put(p, stateInfo);
        p.setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player))
            return;
        final Player p = (Player) e.getPlayer();
        if (!playerStateInfoMap.containsKey(p)) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                StateInfo stateInfo = playerStateInfoMap.get(p);
                if (stateInfo == null) return;
                restoreState(stateInfo, p);
                playerStateInfoMap.remove(p);
            }
        }.runTaskLater(plugin, 1);
    }

    private void restoreState(StateInfo stateInfo, Player p) {
        p.setGameMode(stateInfo.gameMode);
        new BukkitRunnable() {
            @Override
            public void run() {
                p.setAllowFlight(stateInfo.canFly);
                p.setFlying(stateInfo.isFlying);
            }
        }.runTaskLater(plugin, 1);
    }

    @Override
    public boolean isActive() {
        return plugin.getSettings().getBoolean("InvisibilityFeatures.OpenChestsSilently");
    }

    private boolean isShulkerBox(Inventory inv) {
        try {
            return inv.getType() == InventoryType.SHULKER_BOX;
        } catch (NoSuchFieldError e) {
            return false;
        }
    }

    private boolean isShulkerBox(InventoryView inv) {
        try {
            return inv.getType() == InventoryType.SHULKER_BOX;
        } catch (NoSuchFieldError e) {
            return false;
        }
    }

    public boolean hasSilentlyOpenedChest(Player p) {
        return playerStateInfoMap.containsKey(p);
    }

    @Override
    public void onEnable() {
        SilentOpenChestPacketAdapter packetAdapter = new SilentOpenChestPacketAdapter(this);
        ProtocolLibrary.getProtocolManager().addPacketListener(packetAdapter);
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
