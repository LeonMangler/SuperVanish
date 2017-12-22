/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.features;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

public class NoPush extends Feature {

    public NoPush(SuperVanish plugin) {
        super(plugin);
    }

    @Override
    public boolean isActive() {
        return plugin.getSettings().getBoolean("InvisibilityFeatures.DisablePush");
    }

    public void setCantPush(Player p) {
        Team team = p.getScoreboard().getTeam("Vanished");
        if (team == null) {
            team = p.getScoreboard().registerNewTeam("Vanished");
        }
        try {
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            team.addEntry(p.getName());
        } catch (NoSuchMethodError | NoClassDefFoundError ignored) {
        }
    }

    public void setCanPush(Player p) {
        Team team = p.getScoreboard().getTeam("Vanished");
        if (team != null)
            team.removeEntry(p.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVanish(PlayerHideEvent e) {
        setCantPush(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReappear(PlayerShowEvent e) {
        setCanPush(e.getPlayer());
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        if (plugin.getVanishStateMgr().isVanished(e.getPlayer().getUniqueId()))
            new BukkitRunnable() {
                @Override
                public void run() {
                    setCantPush(e.getPlayer());
                }
            }.runTaskLater(plugin, 5);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        setCanPush(e.getPlayer());
    }

}
