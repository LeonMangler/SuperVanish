/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish;

import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;


/**
 * Holds additional information about players in the context of vanishing
 */
public class VanishPlayer {

    private final SuperVanish plugin;
    private final UUID playerUUID;
    private volatile boolean itemPickUps;
    private final int seePermissionLevel, usePermissionLevel;

    VanishPlayer(Player player, SuperVanish plugin, boolean itemPickUps) {
        this.plugin = plugin;
        this.playerUUID = player.getUniqueId();
        this.itemPickUps = itemPickUps;
        if (plugin.getSettings().getBoolean("IndicationFeatures.LayeredPermissions.LayeredSeeAndUsePermissions", false)) {
            seePermissionLevel = plugin.getLayeredPermissionLevel(player, "see");
            usePermissionLevel = plugin.getLayeredPermissionLevel(player, "use");
        } else {
            seePermissionLevel = 0;
            usePermissionLevel = 0;
        }
    }

    public boolean isOnlineVanished() {
        return plugin.getVanishStateMgr().isVanished(playerUUID);
    }

    public boolean hasItemPickUpsEnabled() {
        return itemPickUps;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getSeePermissionLevel() {
        return seePermissionLevel;
    }

    public int getUsePermissionLevel() {
        return usePermissionLevel;
    }

    public void setItemPickUps(boolean itemPickUps) {
        this.itemPickUps = itemPickUps;
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
