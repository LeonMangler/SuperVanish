package de.myzelyam.supervanish.features;

import com.destroystokyo.paper.event.entity.PlayerNaturallySpawnCreaturesEvent;
import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import com.destroystokyo.paper.event.entity.SkeletonHorseTrapEvent;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.utils.FoliaUtil;
import de.myzelyam.supervanish.utils.OneTimeWarning;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.List;

public class NoMobSpawn extends Feature {

    private final OneTimeWarning warning;

    public NoMobSpawn(SuperVanish plugin) {
        super(plugin);
        warning = new OneTimeWarning(plugin, null);
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
            warning.log(er);
        }
    }

    @EventHandler
    public void onEntitySpawn(PlayerNaturallySpawnCreaturesEvent e) {
        try {
            if (plugin.getVanishStateMgr().isVanished(e.getPlayer().getUniqueId()))
                e.setCancelled(true);
        } catch (Exception er) {
            warning.log(er);
        }
    }

    @EventHandler
    public void onEntitySpawnerSpawn(PreSpawnerSpawnEvent e) {
        try {
            // First check if a non-spectator vanished player is in range
            boolean vanishedPlayerInRange = false;
            for (Player p : FoliaUtil.onlinePlayersSnapshot()) {
                if (!FoliaUtil.isOwnedByCurrentRegion(p)) continue;
                if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId())) continue;
                if (p.getWorld().equals(e.getSpawnerLocation().getWorld()) &&
                        p.getLocation().distanceSquared(e.getSpawnerLocation()) <= 256 &&
                        p.getGameMode() != GameMode.SPECTATOR)
                    vanishedPlayerInRange = true;
            }
            if (!vanishedPlayerInRange) return;

            // If so, only cancel if no non-vanished player is in range
            for (Player p : FoliaUtil.onlinePlayersSnapshot()) {
                if (!FoliaUtil.isOwnedByCurrentRegion(p)) continue;
                if (p.getWorld().equals(e.getSpawnerLocation().getWorld()) &&
                        p.getLocation().distanceSquared(e.getSpawnerLocation()) <= 256 &&
                        p.getGameMode() != GameMode.SPECTATOR &&
                        !plugin.getVanishStateMgr().isVanished(p.getUniqueId()))
                    return;
            }
            e.setCancelled(true);
        } catch (Exception er) {
            warning.log(er);
        }
    }

    @Override
    public boolean isActive() {
        return plugin.getSettings().getBoolean("InvisibilityFeatures.PreventMobSpawning", true);
    }
}
