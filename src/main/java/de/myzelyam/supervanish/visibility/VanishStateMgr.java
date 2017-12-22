/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.visibility;

import de.myzelyam.supervanish.SuperVanishPlugin;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public abstract class VanishStateMgr {

    protected final SuperVanishPlugin plugin;

    public VanishStateMgr(SuperVanishPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract boolean isVanished(final UUID uuid);

    public abstract void setVanishedState(final UUID uuid, String name, boolean hide, String causeName);

    public final void setVanishedState(final UUID uuid, String name, boolean hide) {
        setVanishedState(uuid, name, hide, null);
    }

    public abstract Set<UUID> getVanishedPlayers();

    public abstract Collection<UUID> getOnlineVanishedPlayers();
}
