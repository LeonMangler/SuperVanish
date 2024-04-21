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

import java.util.Objects;
import java.util.UUID;


/**
 * Holds additional information about players in the context of vanishing
 */
public class VanishPlayer {

    private final SuperVanish plugin;
    @Getter
    private final UUID playerUUID;
    @Setter
    private boolean itemPickUps;
    @Getter
    private int seePermissionLevel, usePermissionLevel;

    VanishPlayer(Player player, SuperVanish plugin, boolean itemPickUps) {
        this.plugin = plugin;
        this.playerUUID = player.getUniqueId();
        this.itemPickUps = itemPickUps;
        if (plugin.getSettings().getBoolean("IndicationFeatures.LayeredPermissions.LayeredSeeAndUsePermissions", false)) {
            seePermissionLevel = plugin.getLayeredPermissionLevel(player, "see");
            usePermissionLevel = plugin.getLayeredPermissionLevel(player, "use");
        }
    }

    public boolean isOnlineVanished() {
        return plugin.getVanishStateMgr().isVanished(playerUUID);
    }

    public boolean hasItemPickUpsEnabled() {
        return itemPickUps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VanishPlayer that = (VanishPlayer) o;
        return Objects.equals(playerUUID, that.playerUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerUUID);
    }
}
