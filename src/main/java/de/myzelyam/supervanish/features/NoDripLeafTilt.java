package de.myzelyam.supervanish.features;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

public class NoDripLeafTilt extends Feature{
  public NoDripLeafTilt(SuperVanish plugin) {
    super(plugin);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onInteract(PlayerInteractEvent e) {
    Player p = e.getPlayer();
    if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId())) return;
    if (!e.getClickedBlock().getType().toString().equals("BIG_DRIPLEAF")) return;
    e.setCancelled(true);
    }


  @Override
  public boolean isActive() {
    return plugin.getSettings().getBoolean("InvisibilityFeatures.DisableDripLeaf",true);
  }
}
