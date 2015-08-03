package de.myzelyam.supervanish.events;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

import de.myzelyam.supervanish.SVUtils;
import de.myzelyam.supervanish.hooks.EssentialsHook;

public class PlayerControl extends SVUtils implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTp(PlayerTeleportEvent e) {
		// remove potion effects
		Player p = e.getPlayer();
		if (!isHidden(p))
			return;
		if (e.getFrom().getWorld().getName()
				.equals(e.getTo().getWorld().getName()))
			return;
		// remove night vision (readded in WC-Event)
		if (cfg.getBoolean("Configuration.Players.AddNightVision"))
			p.removePotionEffect(PotionEffectType.NIGHT_VISION);
		// remove invisibility (s.a.)
		if (cfg.getBoolean("Configuration.Players.EnableGhostPlayers"))
			p.removePotionEffect(PotionEffectType.INVISIBILITY);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void food(FoodLevelChangeEvent e) {
		try {
			List<String> vpl = pd.getStringList("InvisiblePlayers");
			if (e.getEntity() instanceof Player
					&& !cfg.getBoolean("Configuration.Players.DisableHungerForInvisiblePlayers")) {
				Player p = (Player) e.getEntity();
				if (vpl.contains(p.getUniqueId().toString()))
					e.setCancelled(true);
			}
		} catch (Exception er) {
			plugin.printException(er);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void dmg(EntityDamageEvent e) {
		try {
			if (!(e.getEntity() instanceof Player)) {
				return;
			}
			Player p = (Player) e.getEntity();
			List<String> vpl = pd.getStringList("InvisiblePlayers");
			if (vpl.contains(p.getUniqueId().toString())) {
				e.setCancelled(true);
			}
		} catch (Exception er) {
			plugin.printException(er);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void target(EntityTargetEvent e) {
		try {
			if (!(e.getTarget() instanceof Player)) {
				return;
			}
			Player p = (Player) e.getTarget();
			List<String> vpl = pd.getStringList("InvisiblePlayers");
			if (vpl.contains(p.getUniqueId().toString())) {
				e.setCancelled(true);
			}
		} catch (Exception er) {
			plugin.printException(er);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void pressurePlate(PlayerInteractEvent e) {
		try {
			List<String> vpl = pd.getStringList("InvisiblePlayers");
			if (cfg.getBoolean("Configuration.Players.DisablePressurePlatesForInvisiblePlayers") != true) {
				return;
			}
			if (e.getAction().equals(Action.PHYSICAL)) {
				if (e.getClickedBlock().getType() == Material.STONE_PLATE
						|| e.getClickedBlock().getType() == Material.WOOD_PLATE
						|| e.getClickedBlock().getType() == Material.TRIPWIRE) {
					if (vpl.contains(e.getPlayer().getUniqueId().toString()))
						e.setCancelled(true);
				}
			}
		} catch (Exception er) {
			plugin.printException(er);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPickUp(PlayerPickupItemEvent e) {
		try {
			Player p = e.getPlayer();
			List<String> vpl = pd.getStringList("InvisiblePlayers");
			if (vpl.contains(p.getUniqueId().toString())) {
				if (pd.get("PlayerData." + p.getUniqueId().toString()
						+ ".itemPickUps") == null)
					if (cfg.getBoolean("Configuration.Players.DisableItemPickUpsByDefault"))
						e.setCancelled(true);
					else
						return;
				if (!pd.getBoolean("PlayerData." + p.getUniqueId().toString()
						+ ".itemPickUps"))
					e.setCancelled(true);
			}
		} catch (Exception er) {
			plugin.printException(er);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlace(BlockPlaceEvent e) {
		try {
			Player p = e.getPlayer();
			List<String> vpl = getInvisiblePlayers();
			if (vpl.contains(p.getUniqueId().toString())) {
				if (cfg.getBoolean("Configuration.Players.PreventBlockPlacing")) {
					e.setCancelled(true);
				}
			}
		} catch (Exception er) {
			plugin.printException(er);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBreak(BlockBreakEvent e) {
		try {
			Player p = e.getPlayer();
			List<String> vpl = getInvisiblePlayers();
			if (vpl.contains(p.getUniqueId().toString())) {
				if (cfg.getBoolean("Configuration.Players.PreventBlockBreaking")) {
					e.setCancelled(true);
				}
			}
		} catch (Exception er) {
			plugin.printException(er);
		}
	}

	@EventHandler
	public void onBlockBlock(BlockCanBuildEvent e) {
		try {
			List<String> vpl = pd.getStringList("InvisiblePlayers");
			Block b = e.getBlock();
			Location blockloc = b.getLocation();
			for (Player p : b.getWorld().getPlayers()) {
				if (!vpl.contains(p.getUniqueId().toString()))
					continue;
				if (p.getLocation().distance(blockloc) < 2)
					e.setBuildable(true);
			}
		} catch (Exception er) {
			plugin.printException(er);
		}
	}

	@EventHandler
	public void onPlayerArrowBlock(EntityDamageByEntityEvent e) {
		try {
			List<String> vpl = pd.getStringList("InvisiblePlayers");
			Entity entityDamager = e.getDamager();
			Entity entityDamaged = e.getEntity();
			if (entityDamager instanceof Arrow) {
				Arrow arrow = (Arrow) entityDamager;
				if (entityDamaged instanceof Player
						&& arrow.getShooter() instanceof Player) {
					Player damaged = (Player) entityDamaged;
					if (vpl.contains(damaged.getUniqueId().toString())) {
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

	@EventHandler(priority = EventPriority.HIGH)
	public void onEssentialsEnable(PluginEnableEvent e) {
		try {
			final List<String> vpl = pd.getStringList("InvisiblePlayers");
			if (e.getPlugin().getName().equalsIgnoreCase("Essentials")) {
				Bukkit.getServer().getScheduler()
						.scheduleSyncDelayedTask(plugin, new Runnable() {

							@Override
							public void run() {
								for (Player p : Bukkit.getOnlinePlayers()) {
									if (vpl.contains(p.getUniqueId().toString())) {
										EssentialsHook.hidePlayer(p);
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
