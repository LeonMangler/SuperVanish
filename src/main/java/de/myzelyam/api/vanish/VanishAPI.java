/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.api.vanish;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.utils.Validation;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Static API for SuperVanish on Bukkit
 */
@SuppressWarnings("unused")
public class VanishAPI {

    private static SuperVanish PLUGIN;

    /**
     * @return A collection of the UUIDs of all online vanished players
     */
    public static List<UUID> getInvisiblePlayers() {
        return new ArrayList<>(PLUGIN.getVanishStateMgr().getOnlineVanishedPlayers());
    }

    /**
     * @return A collection of the UUIDs of all vanished players, both online and offline players
     */
    public static List<UUID> getAllInvisiblePlayers() {
        return new ArrayList<>(PLUGIN.getVanishStateMgr().getVanishedPlayers());
    }

    /**
     * Player must be online for this to return true if MySQL is enabled
     *
     * @param p - the player.
     * @return TRUE if the player is invisible, FALSE otherwise.
     */
    public static boolean isInvisible(Player p) {
        Validation.checkNotNull("Player cannot be null!", p);
        return PLUGIN.getVanishStateMgr().isVanished(p.getUniqueId());
    }

    /**
     * Checks if a player is invisible, online or not
     * <p/>
     * Deprecated: Will cause minor lag if mysql is enabled, use asynchronously or sparingly
     *
     * @param uuid - the player's UUID.
     * @return TRUE if the player is invisible, FALSE otherwise.
     */
    public static boolean isInvisibleOffline(UUID uuid) {
        Validation.checkNotNull("UUID cannot be null!", uuid);
        return PLUGIN.getVanishStateMgr().isVanished(uuid);
    }

    /**
     * Hides a player using SuperVanish
     *
     * @param p - the player.
     */
    public static void hidePlayer(Player p) {
        Validation.checkNotNull("Player cannot be null!", p);
        PLUGIN.getVisibilityChanger().hidePlayer(p);
    }

    /**
     * Shows a player using SuperVanish
     *
     * @param p - the player.
     */
    public static void showPlayer(Player p) {
        Validation.checkNotNull("Player cannot be null!", p);
        PLUGIN.getVisibilityChanger().showPlayer(p);
    }

    /**
     * Checks if a player is allowed to see another player
     *
     * @param viewer - the viewer
     * @param viewed - the viewed player
     * @return TRUE if viewed is not vanished or viewer has the permission to see viewed
     */
    public static boolean canSee(Player viewer, Player viewed) {
        return PLUGIN.canSee(viewer, viewed);
    }

    public static FileConfiguration getConfiguration() {
        return PLUGIN.getSettings();
    }

    public static FileConfiguration getMessages() {
        return PLUGIN.getMessages();
    }

    public static FileConfiguration getPlayerData() {
        return PLUGIN.getPlayerData();
    }

    public static void reloadConfig() {
        PLUGIN.reload();
    }

    public static SuperVanish getPlugin() {
        return PLUGIN;
    }

    public static void setPlugin(SuperVanish plugin) {
        VanishAPI.PLUGIN = plugin;
    }
}