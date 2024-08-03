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

public class FileVanishStateMgr extends VanishStateMgr {

    private final SuperVanish plugin;

    public FileVanishStateMgr(SuperVanish plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public boolean isVanished(final UUID uuid) {
        return getVanishedPlayersOnFile().contains(uuid);
    }

    @Override
    public long getLastSeen(UUID uuid) {
        return plugin.getPlayerData().getLong("PlayerData." + uuid + ".information.last_seen", System.currentTimeMillis() / 1000L);
    }

    @Override
    public void setVanishedState(final UUID uuid, String name, boolean hide, String causeName) {
        PlayerVanishStateChangeEvent event = new PlayerVanishStateChangeEvent(uuid, name, hide, causeName);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        List<String> vanishedPlayerUUIDStrings = plugin.getPlayerData().getStringList("InvisiblePlayers");
        if (hide)
            vanishedPlayerUUIDStrings.add(uuid.toString());
        else
            vanishedPlayerUUIDStrings.remove(uuid.toString());
        plugin.getPlayerData().set("InvisiblePlayers", vanishedPlayerUUIDStrings);
        plugin.getPlayerData().set("PlayerData." + uuid + ".information.last_seen", null);
        if (hide) {
            plugin.getPlayerData().set("PlayerData." + uuid + ".information.name", name);
            plugin.getPlayerData().set("PlayerData." + uuid + ".information.last_seen", System.currentTimeMillis() / 1000L);
        }
        plugin.getConfigMgr().getPlayerDataFile().save();
    }

    @Override
    public Set<UUID> getVanishedPlayers() {
        return getVanishedPlayersOnFile();
    }

    @Override
    public Collection<UUID> getOnlineVanishedPlayers() {
        Set<UUID> onlineVanishedPlayers = new HashSet<>();
        for (UUID vanishedUUID : getVanishedPlayers()) {
            if (Bukkit.getPlayer(vanishedUUID) != null)
                onlineVanishedPlayers.add(vanishedUUID);
        }
        return onlineVanishedPlayers;
    }

    public UUID getVanishedUUIDFromNameOnFile(String name) {
        for (UUID uuid : getVanishedPlayersOnFile()) {
            if (plugin.getPlayerData().getString("PlayerData." + uuid + ".information.name")
                    .equalsIgnoreCase(name)) {
                return uuid;
            }
        }
        return null;
    }

    private Set<UUID> getVanishedPlayersOnFile() {
        List<String> vanishedPlayerUUIDStrings = plugin.getPlayerData().getStringList("InvisiblePlayers");
        Set<UUID> vanishedPlayerUUIDs = new HashSet<>();
        for (String uuidString : new ArrayList<>(vanishedPlayerUUIDStrings)) {
            try {
                vanishedPlayerUUIDs.add(UUID.fromString(uuidString));
            } catch (IllegalArgumentException e) {
                vanishedPlayerUUIDStrings.remove(uuidString);
                plugin.log(Level.WARNING,
                        "The data.yml file contains an invalid player uuid," +
                                " deleting it.");
                plugin.getPlayerData().set("InvisiblePlayers", vanishedPlayerUUIDStrings);
                plugin.getConfigMgr().getPlayerDataFile().save();
            }
        }
        return vanishedPlayerUUIDs;
    }

    private void setVanishedPlayersOnFile(Set<UUID> vanishedPlayers) {
        List<String> vanishedPlayerUUIDStrings = new ArrayList<>();
        for (UUID uuid : vanishedPlayers)
            vanishedPlayerUUIDStrings.add(uuid.toString());
        plugin.getPlayerData().set("InvisiblePlayers",
                vanishedPlayerUUIDStrings);
        plugin.getConfigMgr().getPlayerDataFile().save();
    }
}
