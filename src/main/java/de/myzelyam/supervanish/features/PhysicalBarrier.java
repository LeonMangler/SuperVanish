package de.myzelyam.supervanish.features;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.scheduler.ScheduledTask;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PhysicalBarrier extends Feature {
    private final Map<UUID, ArmorStand> barriers = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask> barrierTasks = new ConcurrentHashMap<>();

    public PhysicalBarrier(SuperVanish plugin) {
        super(plugin);
    }

    @Override
    public boolean isActive() {
        return plugin.getSettings().getBoolean("InvisibilityFeatures.PhysicalBarrier", true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVanish(PlayerHideEvent event) {
        createBarrier(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onReappear(PlayerShowEvent event) {
        removeBarrier(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (plugin.getVanishStateMgr().isVanished(playerId)) {
            SuperVanish.getScheduler().runTaskLater(() -> createBarrier(playerId), 10L);
        }
    }

    private void createBarrier(UUID playerId) {
        try {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline()) return;
            removeBarrier(playerId);

            Location loc = player.getLocation();
            Level level = ((CraftWorld) loc.getWorld()).getHandle();
            
            ArmorStand barrier = new ArmorStand(EntityType.ARMOR_STAND, level);
            barrier.setPos(loc.getX(), loc.getY(), loc.getZ());
            barrier.setInvisible(true);
            barrier.setInvulnerable(true);
            barrier.setNoGravity(true);
            barrier.setSilent(true);
            barrier.setMarker(true);
            barrier.setSmall(true);

            barriers.put(playerId, barrier);

            ScheduledTask task = SuperVanish.getScheduler().runTaskTimer(() -> {
                Player livePlayer = Bukkit.getPlayer(playerId);
                if (livePlayer == null || !livePlayer.isOnline() || !plugin.getVanishStateMgr().isVanished(playerId)) {
                    removeBarrier(playerId);
                    return;
                }

                Location newLoc = livePlayer.getLocation();
                ArmorStand stand = barriers.get(playerId);
                if (stand != null && !stand.isRemoved()) {
                    stand.setPos(newLoc.getX(), newLoc.getY(), newLoc.getZ());
                }
            }, 1L, 1L);
            barrierTasks.put(playerId, task);

        } catch (Exception e) {
            plugin.logException(e);
        }
    }

    private void removeBarrier(UUID playerId) {
        ScheduledTask task = barrierTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }

        ArmorStand barrier = barriers.remove(playerId);
        if (barrier != null) {
            try {
                barrier.discard();
            } catch (Exception e) {
                plugin.logException(e);
            }
        }
    }

    @Override
    public void onDisable() {
        for (UUID uuid : new java.util.ArrayList<>(barriers.keySet())) {
            removeBarrier(uuid);
        }
        barrierTasks.clear();
        barriers.clear();
    }

    @Override
    public void onEnable() {
        for (UUID vanishedUUID : plugin.getVanishStateMgr().getOnlineVanishedPlayers()) {
            Player vanished = Bukkit.getPlayer(vanishedUUID);
            if (vanished != null && vanished.isOnline()) {
                createBarrier(vanishedUUID);
            }
        }
    }
}
