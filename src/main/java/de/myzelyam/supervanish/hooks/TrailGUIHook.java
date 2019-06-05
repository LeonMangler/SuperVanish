/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.hooks;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

import ca.jamiesinn.trailgui.Listeners;
import ca.jamiesinn.trailgui.TrailGUI;

public class TrailGUIHook extends PluginHook {

    public TrailGUIHook(SuperVanish superVanish) {
        super(superVanish);
    }

    @Override
    public void onPluginEnable(Plugin plugin) {
        if (!(plugin instanceof TrailGUI)) {
            superVanish.log(Level.WARNING,
                    "Failed to hook into TrailGUI. (PluginNotFound)");
            return;
        }
        PlayerMoveEvent.getHandlerList().unregister(plugin);
        final Listeners trailGUIListeners = new Listeners((TrailGUI) plugin);
        plugin.getServer().getPluginManager().registerEvents(new Listener() {

            @EventHandler
            public void onPlayerMove(PlayerMoveEvent event) {
                if (superVanish.getVanishStateMgr()
                        .getOnlineVanishedPlayers().contains(
                                event.getPlayer().getUniqueId()))
                    return;
                trailGUIListeners.onPlayerMove(event);
            }
        }, plugin);
    }
}
