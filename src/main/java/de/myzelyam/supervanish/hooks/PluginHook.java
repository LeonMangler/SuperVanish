/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.hooks;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * Represents a hook into a plugin to support its functionality or make it support SuperVanish's
 * functionality
 */
public abstract class PluginHook implements Listener {

    protected final SuperVanish superVanish;
    protected Plugin plugin;

    public PluginHook(SuperVanish superVanish) {
        this.superVanish = superVanish;
    }

    public void onPluginEnable(Plugin plugin) {
    }

    public void onPluginDisable(Plugin plugin) {
    }

    public Plugin getPlugin() {
        return plugin;
    }

    void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }
}
