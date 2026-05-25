/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.listeners;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.commands.CommandAction;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.HashMap;
import java.util.Map;


public class LoginListener implements Listener {

    private final SuperVanish plugin;

    public LoginListener(SuperVanish plugin) {
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
            plugin.refreshCachedPermissionLevels(p);
            if (vanished && plugin.getSettings().getBoolean("VanishStateFeatures.CheckPermissionOnLogin", false)
                    && !CommandAction.VANISH_SELF.checkPermission(p, plugin)) {
                vanished = false;
            }
            if (!vanished && p.hasPermission("sv.joinvanished")
                    && plugin.getSettings().getBoolean("VanishStateFeatures.AutoVanishOnJoin", false)) {
                plugin.getVanishStateMgr().setVanishedState(p.getUniqueId(), p.getName(), true, null);
                vanished = true;
            }
            final java.util.List<Player> snapshot = new java.util.ArrayList<>(Bukkit.getOnlinePlayers());

            final Map<java.util.UUID, Boolean> joinerCanSeeMap = new HashMap<>(snapshot.size());
            final Map<java.util.UUID, Boolean> onlineCanSeeJoinerMap = new HashMap<>(snapshot.size());

            for (Player onlinePlayer : snapshot) {
                if (onlinePlayer.getUniqueId().equals(p.getUniqueId())) continue;
                joinerCanSeeMap.put(onlinePlayer.getUniqueId(), plugin.hasPermissionToSee(p, onlinePlayer));
                if (vanished) {
                    onlineCanSeeJoinerMap.put(onlinePlayer.getUniqueId(), plugin.hasPermissionToSee(onlinePlayer, p));
                }
            }

            for (Player onlinePlayer : snapshot) {
                if (onlinePlayer.getUniqueId().equals(p.getUniqueId())) continue;

                if (vanished && !onlineCanSeeJoinerMap.get(onlinePlayer.getUniqueId())) {
                    plugin.getVisibilityChanger().getHider().setHidden(p, onlinePlayer, true);
                }
                if (plugin.getVanishStateMgr().isVanished(onlinePlayer.getUniqueId())
                        && !joinerCanSeeMap.get(onlinePlayer.getUniqueId())) {
                    plugin.getVisibilityChanger().getHider().setHidden(onlinePlayer, p, true);
                }
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }
}
