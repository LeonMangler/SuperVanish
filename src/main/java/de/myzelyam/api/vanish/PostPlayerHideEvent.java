/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.api.vanish;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PostPlayerHideEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private final boolean silent;

    public PostPlayerHideEvent(Player p, boolean silent) {
        super(p);
        this.silent = silent;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isSilent() {
        return silent;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
