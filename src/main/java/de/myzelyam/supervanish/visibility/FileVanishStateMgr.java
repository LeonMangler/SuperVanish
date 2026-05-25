/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.visibility;

import de.myzelyam.api.vanish.PlayerVanishStateChangeEvent;
import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.concurrent.ConcurrentHashMap;

public class FileVanishStateMgr extends VanishStateMgr {

    private final SuperVanish plugin;
    private final Set<UUID> vanishedPlayersCache = ConcurrentHashMap.newKeySet();

    public FileVanishStateMgr(SuperVanish plugin) {
        super(plugin);
        this.plugin = plugin;
        loadVanishedPlayersFromFile();
    }

    @Override
    public boolean isVanished(final UUID uuid) {
        return vanishedPlayersCache.contains(uuid);
    }

    @Override
    public void setVanishedState(final UUID uuid, String name, boolean hide, String causeName) {
        PlayerVanishStateChangeEvent event = new PlayerVanishStateChangeEvent(uuid, name, hide, causeName);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        boolean changed;
        if (hide) {
            changed = vanishedPlayersCache.add(uuid);
        } else {
            changed = vanishedPlayersCache.remove(uuid);
        }
        if (!changed) {
            return;
        }
        List<String> vanishedPlayerUUIDStrings = new ArrayList<>();
        for (UUID vanishedUuid : vanishedPlayersCache) {
            vanishedPlayerUUIDStrings.add(vanishedUuid.toString());
        }
        plugin.getPlayerData().set("InvisiblePlayers", vanishedPlayerUUIDStrings);
        if (hide)
            plugin.getPlayerData().set("PlayerData." + uuid + ".information.name", name);
        plugin.getConfigMgr().getPlayerDataFile().save();
    }

    @Override
    public Set<UUID> getVanishedPlayers() {
        return new HashSet<>(vanishedPlayersCache);
    }

    @Override
    public Collection<UUID> getOnlineVanishedPlayers() {
        Set<UUID> onlineVanishedPlayers = new HashSet<>();
        for (UUID vanishedUUID : vanishedPlayersCache) {
            if (Bukkit.getPlayer(vanishedUUID) != null)
                onlineVanishedPlayers.add(vanishedUUID);
        }
        return onlineVanishedPlayers;
    }

    public UUID getVanishedUUIDFromNameOnFile(String name) {
        for (UUID uuid : vanishedPlayersCache) {
            if (plugin.getPlayerData().getString("PlayerData." + uuid + ".information.name")
                    .equalsIgnoreCase(name)) {
                return uuid;
            }
        }
        return null;
    }

    private void loadVanishedPlayersFromFile() {
        vanishedPlayersCache.clear();
        List<String> vanishedPlayerUUIDStrings = plugin.getPlayerData().getStringList("InvisiblePlayers");
        for (String uuidString : new ArrayList<>(vanishedPlayerUUIDStrings)) {
            try {
                vanishedPlayersCache.add(UUID.fromString(uuidString));
            } catch (IllegalArgumentException e) {
                vanishedPlayerUUIDStrings.remove(uuidString);
                plugin.log(Level.WARNING,
                        "The data.yml file contains an invalid player uuid," +
                                " deleting it.");
                plugin.getPlayerData().set("InvisiblePlayers", vanishedPlayerUUIDStrings);
                plugin.getConfigMgr().getPlayerDataFile().save();
            }
        }
    }
}
