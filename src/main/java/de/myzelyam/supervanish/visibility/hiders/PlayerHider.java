/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.visibility.hiders;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class PlayerHider implements Listener {

    protected final SuperVanish plugin;
    protected final Map<UUID, Set<UUID>> playerHiddenFromPlayersMap = new ConcurrentHashMap<>();

    public PlayerHider(SuperVanish plugin) {
        this.plugin = plugin;
        registerQuitListener();
    }

    public abstract String getName();

    public boolean isHidden(Player player, Player viewer) {
        return isHidden(player.getUniqueId(), viewer.getUniqueId());
    }

    public boolean isHidden(UUID playerUUID, Player viewer) {
        return isHidden(playerUUID, viewer.getUniqueId());
    }

    public boolean isHidden(UUID playerUUID, UUID viewerUUID) {
        if (playerUUID.equals(viewerUUID)) return false;
        Set<UUID> hiddenFromPlayers = playerHiddenFromPlayersMap.get(playerUUID);
        return hiddenFromPlayers != null && hiddenFromPlayers.contains(viewerUUID);
    }

    public boolean isHidden(String playerName, Player viewer) {
        if (playerName.equalsIgnoreCase(viewer.getName())) return false;
        Player player = Bukkit.getPlayerExact(playerName);
        return player != null && isHidden(player.getUniqueId(), viewer.getUniqueId());
    }

    /**
     * @return TRUE if the operation changed the state, FALSE if it did not
     */
    public boolean setHidden(Player player, Player viewer, boolean hidden) {
        if (viewer == player) return false;
        Set<UUID> hiddenFromPlayers = playerHiddenFromPlayersMap.computeIfAbsent(player.getUniqueId(),
                ignored -> ConcurrentHashMap.newKeySet());
        UUID viewerUuid = viewer.getUniqueId();
        if (hidden && !hiddenFromPlayers.contains(viewerUuid)) {
            hiddenFromPlayers.add(viewerUuid);
            return true;
        } else if (!hidden && hiddenFromPlayers.contains(viewerUuid)) {
            hiddenFromPlayers.remove(viewerUuid);
            return true;
        }
        return false;
    }

    public Set<Player> getHiddenPlayerKeys() {
        return playerHiddenFromPlayersMap.keySet().stream()
                .map(Bukkit::getPlayer)
                .filter(player -> player != null)
                .collect(Collectors.toSet());
    }

    private void registerQuitListener() {
        plugin.getServer().getPluginManager().registerEvents(new Listener() {

            @EventHandler(priority = EventPriority.MONITOR)
            public void onQuit(final PlayerQuitEvent e) {
                UUID quittingPlayerUuid = e.getPlayer().getUniqueId();
                playerHiddenFromPlayersMap.remove(quittingPlayerUuid);
                for (Set<UUID> hiddenFromPlayers : playerHiddenFromPlayersMap.values()) {
                    hiddenFromPlayers.remove(quittingPlayerUuid);
                }
            }
        }, plugin);
    }
}
