/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.hooks;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.dynmap.bukkit.DynmapPlugin;

public abstract class DynmapHook {

    public static SuperVanish plugin = (SuperVanish) Bukkit.getPluginManager()
            .getPlugin("SuperVanish");

    public static void adjustVisibility(Player p, boolean hide, FileConfiguration settings) {
        try {
            DynmapPlugin plugin = (DynmapPlugin) Bukkit.getPluginManager()
                    .getPlugin("dynmap");
            plugin.setPlayerVisiblity(p.getName(), !hide);
            if (settings.getBoolean("Configuration.Hooks.DynmapHook.showJoinQuitMessage")) {
                plugin.postPlayerJoinQuitToWeb(p.getName(), p.getName(), !hide);
            }
        } catch (Exception e) {
            plugin.printException(e);
        }
    }
}
