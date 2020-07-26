/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.hooks;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.clip.placeholderapi.PlaceholderAPI;

public class PlaceholderAPIHook extends PluginHook {

    private final PlaceholderAPIExpansion expansion;

    public PlaceholderAPIHook(SuperVanish superVanish) {
        super(superVanish);
        this.expansion = new PlaceholderAPIExpansion(superVanish);
    }

    public static String translatePlaceholders(String msg, Player p) {
        return PlaceholderAPI.setPlaceholders(p, msg);
    }

    @Override
    public void onPluginEnable(Plugin plugin) {
        onPluginDisable(plugin);
        expansion.register();
    }

    @Override
    public void onPluginDisable(Plugin plugin) {
        if (expansion.isRegistered()) {
            PlaceholderAPI.unregisterExpansion(expansion);
        }
    }
}
