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
import net.pl3x.map.Pl3xMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Pl3xMapHook extends PluginHook {

    private final SuperVanish superVanish;

    public Pl3xMapHook(SuperVanish superVanish) {
        super(superVanish);
        this.superVanish = superVanish;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVanish(PlayerHideEvent e) {
        Player p = e.getPlayer();
        Pl3xMap pl3xMap = (Pl3xMap) plugin;

        pl3xMap.getPlayerRegistry().get(p.getName()).setHidden(true,true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReappear(PlayerShowEvent e) {
        Player p = e.getPlayer();
        Pl3xMap pl3xMap = (Pl3xMap) plugin;

        pl3xMap.getPlayerRegistry().get(p.getName()).setHidden(false,true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Pl3xMap pl3xMap = (Pl3xMap) plugin;

        if (superVanish.getVanishStateMgr().isVanished(p.getUniqueId())) {
            pl3xMap.getPlayerRegistry().get(p.getName()).setHidden(true,true);
        }
    }
}
