/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish;

import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.Setter;


/**
 * Wrapped org.bukkit.entity.Player
 */
public class VanishPlayer {

    private final SuperVanish plugin;
    @Getter
    private Player player;
    @Setter
    private boolean itemPickUps;
    @Getter
    private int seePermissionLevel, usePermissionLevel;

    VanishPlayer(Player player, SuperVanish plugin, boolean itemPickUps) {
        this.plugin = plugin;
        this.player = player;
        this.itemPickUps = itemPickUps;
        if (plugin.getSettings().getBoolean("IndicationFeatures.LayeredPermissions.LayeredSeeAndUsePermissions", false)) {
            seePermissionLevel = plugin.getLayeredPermissionLevel(player, "see");
            usePermissionLevel = plugin.getLayeredPermissionLevel(player, "use");
        }
    }

    public boolean isOnlineVanished() {
        return plugin.getVanishStateMgr().isVanished(player.getUniqueId());
    }

    public boolean hasItemPickUpsEnabled() {
        return itemPickUps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VanishPlayer that = (VanishPlayer) o;
        return player != null ? player.getUniqueId().equals(that.player.getUniqueId()) : that.player == null;
    }

    @Override
    public int hashCode() {
        return player != null ? player.getUniqueId().hashCode() : 0;
    }
}
