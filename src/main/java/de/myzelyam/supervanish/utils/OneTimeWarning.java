/*
 * Copyright (c) 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.utils;

import de.myzelyam.supervanish.SuperVanish;

import java.util.concurrent.atomic.AtomicBoolean;

public final class OneTimeWarning {

    private final SuperVanish plugin;
    private final String message;
    private final AtomicBoolean logged = new AtomicBoolean(false);

    public OneTimeWarning(SuperVanish plugin, String message) {
        this.plugin = plugin;
        this.message = message;
    }

    public void log(Throwable throwable) {
        if (!logged.compareAndSet(false, true)) return;
        plugin.logException(throwable);
        if (message != null && !message.isEmpty()) {
            plugin.getLogger().warning(message);
        }
    }
}
