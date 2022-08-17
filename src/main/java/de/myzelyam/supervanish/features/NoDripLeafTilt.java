package de.myzelyam.supervanish.features;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

public class NoDripLeafTilt extends Feature{
  public NoDripLeafTilt(SuperVanish plugin) {
    super(plugin);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onInteract(PlayerInteractEvent e) {
    if(plugin.getSettings().getBoolean("InvisibilityFeatures.DisableDripLeaf")) {
      String material = e.getClickedBlock().getType().toString();
      if (material.equals("BIG_DRIPLEAF")) {
        e.setCancelled(true);
      }
    }
  }


  @Override
  public boolean isActive() {
    return plugin.getSettings().getBoolean("InvisibilityFeatures.DisableDripLeaf");
  }
}
