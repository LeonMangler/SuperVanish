/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.events;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.commands.CommandAction;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;


public class LoginEvent implements Listener {

    private final SuperVanish plugin;

    public LoginEvent(SuperVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(PlayerLoginEvent e) {
        try {
            if (e.getResult() != PlayerLoginEvent.Result.ALLOWED) return;
            final Player p = e.getPlayer();
            boolean vanished = plugin.getVanishStateMgr().isVanished(p.getUniqueId());
            boolean itemPickUps = plugin.getPlayerData().getBoolean(
                    "PlayerData." + p.getUniqueId() + ".itemPickUps",
                    plugin.getSettings().getBoolean("InvisibilityFeatures.DefaultPickUpItemsOption"));
            plugin.createVanishPlayer(p, itemPickUps);
            if (vanished && plugin.getSettings().getBoolean("VanishStateFeatures.CheckPermissionOnLogin", false)
                    && !CommandAction.VANISH_SELF.checkPermission(p, plugin)) {
                vanished = false;
            }
            if (!vanished && p.hasPermission("sv.joinvanished")
                    && plugin.getSettings().getBoolean("VanishStateFeatures.AutoVanishOnJoin", false)) {
                plugin.getVanishStateMgr().setVanishedState(p.getUniqueId(), p.getName(), true, null);
                vanished = true;
            }
            if (vanished) {
                // hide self
                for (Player onlinePlayer : Bukkit.getOnlinePlayers())
                    if (!plugin.hasPermissionToSee(onlinePlayer, p))
                        plugin.getVisibilityChanger().getHider().setHidden(p, onlinePlayer, true);
            }

            // hide others
            for (Player onlinePlayer : Bukkit.getOnlinePlayers())
                if (plugin.getVanishStateMgr().isVanished(onlinePlayer.getUniqueId())
                        && !plugin.hasPermissionToSee(p, onlinePlayer))
                    plugin.getVisibilityChanger().getHider().setHidden(onlinePlayer, p, true);
        } catch (Exception er) {
            plugin.logException(er);
        }
    }
}
