/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.features;

import de.myzelyam.supervanish.SuperVanish;

import io.github.projectunified.minelib.scheduler.global.GlobalScheduler;
import org.bukkit.event.Listener;

/**
 * Represents a toggleable feature of SuperVanish
 */
public abstract class Feature implements Listener {

    protected final SuperVanish plugin;

    public Feature(SuperVanish plugin) {
        this.plugin = plugin;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public abstract boolean isActive();

    protected void delay(Runnable runnable) {
        GlobalScheduler.get(plugin).run(runnable);
    }
}
