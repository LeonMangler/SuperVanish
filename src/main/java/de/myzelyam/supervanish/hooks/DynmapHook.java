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
            DynmapPlugin dynmap = (DynmapPlugin) Bukkit.getPluginManager()
                    .getPlugin("dynmap");
            if (hide) {
                dynmap.setPlayerVisiblity(p.getName(), false);
                dynmap.sendBroadcastToWeb("",
                        plugin.convertString(plugin.getMsg("DynmapFakeQuit"), p));
            } else {
                dynmap.setPlayerVisiblity(p.getName(), true);
                dynmap.sendBroadcastToWeb("",
                        plugin.convertString(plugin.getMsg("DynmapFakeJoin"), p));
            }
        } catch (Exception e) {
            plugin.printException(e);
        }
    }
}
