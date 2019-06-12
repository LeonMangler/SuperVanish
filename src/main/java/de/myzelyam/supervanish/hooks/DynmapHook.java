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

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.dynmap.bukkit.DynmapPlugin;

public class DynmapHook extends PluginHook {

    private final boolean sendJoinLeave;
    private final SuperVanish superVanish;

    public DynmapHook(SuperVanish superVanish) {
        super(superVanish);
        this.superVanish = superVanish;
        sendJoinLeave
                = superVanish.getSettings().getBoolean("HookOptions.DynmapSendJoinLeaveMessages")
                && !superVanish.getMessage("DynmapFakeJoin").equals("");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVanish(PlayerHideEvent e) {
        Player p = e.getPlayer();
        DynmapPlugin dynmap = (DynmapPlugin) plugin;

        dynmap.setPlayerVisiblity(p, false);
        if (sendJoinLeave)
            dynmap.sendBroadcastToWeb("",
                    superVanish.replacePlaceholders(superVanish.getMessage("DynmapFakeQuit"), p));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReappear(PlayerShowEvent e) {
        Player p = e.getPlayer();
        DynmapPlugin dynmap = (DynmapPlugin) plugin;

        dynmap.setPlayerVisiblity(p, true);
        if (sendJoinLeave)
            dynmap.sendBroadcastToWeb("",
                    superVanish.replacePlaceholders(superVanish.getMessage("DynmapFakeJoin"), p));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        DynmapPlugin dynmap = (DynmapPlugin) plugin;

        if (superVanish.getVanishStateMgr().isVanished(p.getUniqueId())) {
            dynmap.setPlayerVisiblity(p, false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        DynmapPlugin dynmap = (DynmapPlugin) plugin;

        if (superVanish.getVanishStateMgr().isVanished(p.getUniqueId())) {
            dynmap.setPlayerVisiblity(p, true);
        }
    }
}
