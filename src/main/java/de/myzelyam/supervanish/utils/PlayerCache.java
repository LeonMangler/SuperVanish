/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.utils;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerCache {

    private static Map<Player, PlayerCache> playerCacheMap = new HashMap<>();

    private final Player player;

    private int seePermissionLevel, usePermissionLevel;

    private PlayerCache(Player player, SuperVanish plugin) {
        this.player = player;
        seePermissionLevel = player.hasPermission("sv.see") ? 1 : 0;
        usePermissionLevel = 1;
        if (plugin.settings.getBoolean("Configuration.Players.LayeredSeeAndUsePermissions", true)) {
            for (int i = 1; i < 101; i++)
                if (player.hasPermission("sv.see.level" + i))
                    seePermissionLevel = i;
            for (int i = 1; i < 101; i++)
                if (player.hasPermission("sv.use.level" + i))
                    usePermissionLevel = i;
        }
    }

    public static PlayerCache fromPlayer(Player p, SuperVanish plugin) {
        if (!playerCacheMap.containsKey(p)) {
            playerCacheMap.put(p, new PlayerCache(p, plugin));
        }
        return playerCacheMap.get(p);
    }

    public static Map<Player, PlayerCache> getPlayerCacheMap() {
        return playerCacheMap;
    }

    public Player getPlayer() {
        return player;
    }

    public int getSeePermissionLevel() {
        return seePermissionLevel;
    }

    public int getUsePermissionLevel() {
        return usePermissionLevel;
    }
}
