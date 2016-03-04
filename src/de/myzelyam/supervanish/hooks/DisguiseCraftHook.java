/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.hooks;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pgDev.bukkit.DisguiseCraft.api.PlayerDisguiseEvent;

public class DisguiseCraftHook implements Listener {

    public SuperVanish plugin = (SuperVanish) Bukkit.getPluginManager()
            .getPlugin("SuperVanish");

    @EventHandler
    public void onDisguise(PlayerDisguiseEvent e) {
        try {
            Player p = e.getPlayer();
            if (plugin.playerData.getStringList("InvisiblePlayers")
                    .contains(p.getUniqueId().toString())) {
                p.sendMessage(ChatColor.RED
                        + "[SV] You can't disguise yourself at the moment!");
                e.setCancelled(true);
            }
        } catch (Exception er) {
            plugin.printException(er);
        }
    }
}
