/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.hooks;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.dynmap.bukkit.DynmapPlugin;

public abstract class DynmapHook {

    public static SuperVanish plugin = (SuperVanish) Bukkit.getPluginManager()
            .getPlugin("SuperVanish");

    public static void adjustVisibility(Player p, boolean hide) {
        try {
            DynmapPlugin plugin = (DynmapPlugin) Bukkit.getPluginManager()
                    .getPlugin("dynmap");
            if (hide) {
                plugin.setPlayerVisiblity(p.getName(), false);
                plugin.sendBroadcastToWeb("", p.getName() + " quit");
            } else {
                plugin.setPlayerVisiblity(p.getName(), true);
                plugin.sendBroadcastToWeb("", p.getName() + " joined");
            }
        } catch (Exception e) {
            plugin.printException(e);
        }
    }
}
