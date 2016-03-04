/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.events;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.hooks.EssentialsHook;
import de.myzelyam.supervanish.utils.OneDotEightUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;

public class GeneralEventListener implements Listener {

    private final SuperVanish plugin;
    private final FileConfiguration settings;
    private final FileConfiguration playerData;

    public GeneralEventListener(SuperVanish plugin) {
        this.plugin = plugin;
        this.playerData = plugin.playerData;
        this.settings = plugin.settings;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        // remove potion effects
        Player p = e.getPlayer();
        Collection<Player> invisiblePlayers = plugin.getOnlineInvisiblePlayers();
        if (!invisiblePlayers.contains(p)) return;
        if (e.getFrom().getWorld().getName()
                .equals(e.getTo().getWorld().getName()))
            return;
        // remove night vision (re-added in WorldChange event)
        if (settings.getBoolean("Configuration.Players.AddNightVision"))
            p.removePotionEffect(PotionEffectType.NIGHT_VISION);
        // remove invisibility
        if (settings.getBoolean("Configuration.Players.EnableGhostPlayers"))
            p.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent e) {
        try {
            if (e.getEntity() instanceof Player
                    && !settings.getBoolean("Configuration.Players.DisableHungerForInvisiblePlayers")) {
                Player p = (Player) e.getEntity();
                Collection<Player> invisiblePlayers = plugin.getOnlineInvisiblePlayers();
                if (invisiblePlayers.contains(p))
                    e.setCancelled(true);
            }
        } catch (Exception er) {
            plugin.printException(er);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent e) {
        try {
            if (!(e.getEntity() instanceof Player)) {
                return;
            }
            Player p = (Player) e.getEntity();
            Collection<Player> invisiblePlayers = plugin.getOnlineInvisiblePlayers();
            if (invisiblePlayers.contains(p)) {
                e.setCancelled(true);
            }
        } catch (Exception er) {
            plugin.printException(er);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityTarget(EntityTargetEvent e) {
        try {
            if (!(e.getTarget() instanceof Player)) {
                return;
            }
            Player p = (Player) e.getTarget();
            List<String> vpl = playerData.getStringList("InvisiblePlayers");
            if (vpl.contains(p.getUniqueId().toString())) {
                e.setCancelled(true);
            }
        } catch (Exception er) {
            plugin.printException(er);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent e) {
        try {
            Collection<Player> invisiblePlayers = plugin.getOnlineInvisiblePlayers();
            if (!settings.getBoolean("Configuration.Players.DisablePressurePlatesForInvisiblePlayers")) {
                return;
            }
            if (e.getAction().equals(Action.PHYSICAL)) {
                if (e.getClickedBlock().getType() == Material.STONE_PLATE
                        || e.getClickedBlock().getType() == Material.WOOD_PLATE
                        || e.getClickedBlock().getType() == Material.TRIPWIRE || (!SuperVanish.SERVER_IS_ONE_DOT_SEVEN &&
                        OneDotEightUtils.isPressurePlate(e.getClickedBlock().getType()))) {
                    if (invisiblePlayers.contains(e.getPlayer()))
                        e.setCancelled(true);
                }
            }
        } catch (Exception er) {
            plugin.printException(er);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPickupItem(PlayerPickupItemEvent e) {
        try {
            Player p = e.getPlayer();
            Collection<Player> invisiblePlayers = plugin.getOnlineInvisiblePlayers();
            if (invisiblePlayers.contains(p)) {
                if (playerData.get("PlayerData." + p.getUniqueId().toString()
                        + ".itemPickUps") == null)
                    if (settings.getBoolean("Configuration.Players.DisableItemPickUpsByDefault"))
                        e.setCancelled(true);
                    else
                        return;
                if (!playerData.getBoolean("PlayerData." + p.getUniqueId().toString()
                        + ".itemPickUps"))
                    e.setCancelled(true);
            }
        } catch (Exception er) {
            plugin.printException(er);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent e) {
        try {
            Player p = e.getPlayer();
            Collection<Player> invisiblePlayers = plugin.getOnlineInvisiblePlayers();
            if (invisiblePlayers.contains(p)) {
                if (settings.getBoolean("Configuration.Players.PreventBlockPlacing")) {
                    e.setCancelled(true);
                }
            }
        } catch (Exception er) {
            plugin.printException(er);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        try {
            Player p = e.getPlayer();
            Collection<Player> invisiblePlayers = plugin.getOnlineInvisiblePlayers();
            if (invisiblePlayers.contains(p)) {
                if (settings.getBoolean("Configuration.Players.PreventBlockBreaking")) {
                    e.setCancelled(true);
                }
            }
        } catch (Exception er) {
            plugin.printException(er);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerHit(EntityDamageByEntityEvent e) {
        try {
            if (!(e.getDamager() instanceof Player))
                return;
            if (e.getEntity() == null)
                return;
            Player p = (Player) e.getDamager();
            Collection<Player> invisiblePlayers = plugin.getOnlineInvisiblePlayers();
            if (invisiblePlayers == null)
                return;
            if (invisiblePlayers.contains(p)) {
                if (settings.getBoolean("Configuration.Players.PreventHittingEntities")) {
                    e.setCancelled(true);
                }
            }
        } catch (Exception er) {
            plugin.printException(er);
        }
    }

    @EventHandler
    public void onBlockCanBuild(BlockCanBuildEvent e) {
        try {
            Collection<Player> invisiblePlayers = plugin.getOnlineInvisiblePlayers();
            Block block = e.getBlock();
            Location bLocation = block.getLocation();
            for (Player p : block.getWorld().getPlayers()) {
                if (!invisiblePlayers.contains(p))
                    continue;
                if (p.getLocation().distanceSquared(bLocation) <= 2.0)
                    e.setBuildable(true);
            }
        } catch (Exception er) {
            plugin.printException(er);
        }
    }

    @EventHandler
    public void onPlayerArrowBlock(EntityDamageByEntityEvent e) {
        try {
            Entity entityDamager = e.getDamager();
            Entity entityDamaged = e.getEntity();
            if (entityDamager instanceof Arrow) {
                Arrow arrow = (Arrow) entityDamager;
                if (entityDamaged instanceof Player
                        && arrow.getShooter() instanceof Player) {
                    Player damaged = (Player) entityDamaged;
                    Collection<Player> invisiblePlayers = plugin.getOnlineInvisiblePlayers();
                    if (invisiblePlayers.contains(damaged)) {
                        Vector velocity = arrow.getVelocity();
                        damaged.teleport(damaged.getLocation().add(0, 2, 0));
                        Arrow nextArrow = arrow.getShooter().launchProjectile(
                                Arrow.class);
                        nextArrow.setVelocity(velocity);
                        nextArrow.setBounce(false);
                        nextArrow.setShooter(arrow.getShooter());
                        nextArrow.setFireTicks(arrow.getFireTicks());
                        nextArrow.setCritical(arrow.isCritical());
                        nextArrow.setKnockbackStrength(arrow
                                .getKnockbackStrength());
                        e.setCancelled(true);
                        arrow.remove();
                    }
                }
            }
        } catch (Exception er) {
            plugin.printException(er);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEssentialsEnable(PluginEnableEvent e) {
        try {
            final Collection<Player> invisiblePlayers = plugin.getOnlineInvisiblePlayers();
            if (e.getPlugin().getName().equalsIgnoreCase("Essentials")
                    && settings.getBoolean("Configuration.Hooks.EnableEssentialsHook")) {
                Bukkit.getServer().getScheduler()
                        .scheduleSyncDelayedTask(plugin, new Runnable() {

                            @Override
                            public void run() {
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    if (invisiblePlayers.contains(player)) {
                                        EssentialsHook.hidePlayer(player);
                                    }
                                }
                            }
                        }, 5);
            }
        } catch (Exception er) {
            plugin.printException(er);
        }
    }
}
