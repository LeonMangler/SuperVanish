/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.hooks;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import me.kvq.plugin.trails.API.SuperTrailsAPI;

public class SuperTrailsHook extends PluginHook {

    public SuperTrailsHook(SuperVanish superVanish) {
        super(superVanish);
    }

    @Override
    public void onPluginEnable(Plugin plugin) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (superVanish.getVanishStateMgr().isVanished(p.getUniqueId())) {
                SuperTrailsAPI.HideTrailFor(p, true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVanish(PlayerHideEvent e) {
        SuperTrailsAPI.HideTrailFor(e.getPlayer(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReappear(PlayerShowEvent e) {
        SuperTrailsAPI.HideTrailFor(e.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        if (!superVanish.getVanishStateMgr().isVanished(e.getPlayer().getUniqueId())) return;
        SuperTrailsAPI.HideTrailFor(e.getPlayer(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        if (!superVanish.getVanishStateMgr().isVanished(e.getPlayer().getUniqueId())) return;
        SuperTrailsAPI.HideTrailFor(e.getPlayer(), false);
    }
}
