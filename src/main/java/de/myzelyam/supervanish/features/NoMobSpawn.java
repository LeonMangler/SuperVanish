package de.myzelyam.supervanish.features;

import com.destroystokyo.paper.event.entity.PlayerNaturallySpawnCreaturesEvent;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import com.destroystokyo.paper.event.entity.SkeletonHorseTrapEvent;
import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.List;
import java.util.UUID;

public class NoMobSpawn extends Feature {

    private boolean suppressErrors = false;

    public NoMobSpawn(SuperVanish plugin) {
        super(plugin);
    }

    @EventHandler
    public void onSkeletonHorseTrap(SkeletonHorseTrapEvent e) {
        try {
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
            if (!suppressErrors) {
                plugin.logException(er);
                suppressErrors = true;
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(PlayerNaturallySpawnCreaturesEvent e) {
        try {
            if (plugin.getVanishStateMgr().isVanished(e.getPlayer().getUniqueId()))
                e.setCancelled(true);
        } catch (Exception er) {
            if (!suppressErrors) {
                plugin.logException(er);
                suppressErrors = true;
            }
        }
    }

    @EventHandler
    public void onEntitySpawnerSpawn(PreSpawnerSpawnEvent e) {
        try {
            // First check if a non-spectator vanished player is in range
            boolean vanishedPlayerInRange = false;
            for (UUID vanishedUUID : plugin.getVanishStateMgr().getOnlineVanishedPlayers()) {
                Player p = Bukkit.getPlayer(vanishedUUID);
                if (p == null) continue;
                if (p.getWorld().equals(e.getSpawnerLocation().getWorld()) &&
                        p.getLocation().distanceSquared(e.getSpawnerLocation()) <= 256 &&
                        p.getGameMode() != GameMode.SPECTATOR)
                    vanishedPlayerInRange = true;
            }
            if (!vanishedPlayerInRange) return;

            // If so, only cancel if no non-vanished player is in range
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld().equals(e.getSpawnerLocation().getWorld()) &&
                        p.getLocation().distanceSquared(e.getSpawnerLocation()) <= 256 &&
                        p.getGameMode() != GameMode.SPECTATOR &&
                        !plugin.getVanishStateMgr().isVanished(p.getUniqueId()))
                    return;
            }
            e.setCancelled(true);
        } catch (Exception er) {
            if (!suppressErrors) {
                plugin.logException(er);
                suppressErrors = true;
            }
        }
    }

    @Override
    public boolean isActive() {
        return plugin.getSettings().getBoolean("InvisibilityFeatures.PreventMobSpawning", true);
    }
}
