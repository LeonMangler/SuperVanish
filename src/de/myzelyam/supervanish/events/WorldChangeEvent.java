/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.events;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.utils.ProtocolLibPacketUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class WorldChangeEvent implements Listener {

    private final SuperVanish plugin;

    public WorldChangeEvent(SuperVanish plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration getSettings() {
        return plugin.settings;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        try {
            final Player p = e.getPlayer();
            if (!plugin.getOnlineInvisiblePlayers().contains(p))
                return;
            // check auto-reappear-option
            if (getSettings().getBoolean("Configuration.Players.ReappearOnWorldChange")) {
                plugin.getVisibilityAdjuster().showPlayer(p);
                return;
            }
            // re-hide
            plugin.getVisibilityAdjuster().getHider().hideToAll(p);
            // re-add night vision (removed in teleport event)
            if (getSettings().getBoolean("Configuration.Players.AddNightVision"))
                if (plugin.packetNightVision) {
                    plugin.getProtocolLibPacketUtils().sendAddPotionEffect(p, new PotionEffect(
                            PotionEffectType.NIGHT_VISION, ProtocolLibPacketUtils.INFINITE_POTION_LENGTH, 0));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            plugin.getProtocolLibPacketUtils().sendAddPotionEffect(p, new PotionEffect(
                                    PotionEffectType.NIGHT_VISION, ProtocolLibPacketUtils.INFINITE_POTION_LENGTH, 0));
                        }
                    }.runTaskLater(plugin, 1);
                } else
                    p.addPotionEffect(new PotionEffect(
                            PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
        } catch (Exception er) {
            plugin.printException(er);
        }
    }
}