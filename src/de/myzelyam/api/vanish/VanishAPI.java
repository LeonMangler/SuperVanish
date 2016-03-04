/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.api.vanish;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.config.MessagesFile;
import de.myzelyam.supervanish.config.SettingsFile;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class VanishAPI {

    private static SuperVanish plugin;

    /**
     * @return A String list of the UUID's of all hidden players
     */
    public static List<String> getInvisiblePlayers() {
        return plugin.playerData.getStringList("InvisiblePlayers");
    }

    /**
     * @param p - the player.
     * @return TRUE if the player is invisible, FALSE otherwise.
     */
    public static boolean isInvisible(Player p) {
        if (p == null) throw new IllegalArgumentException("player cannot be null");
        return plugin.playerData.getStringList("InvisiblePlayers").contains(p.getUniqueId().toString());
    }

    /**
     * Hides a player using SuperVanish
     *
     * @param p - the player.
     */
    public static void hidePlayer(Player p) {
        if (p == null) throw new IllegalArgumentException("player cannot be null");
        plugin.getVisibilityAdjuster().hidePlayer(p);
    }

    /**
     * * Shows a player using SuperVanish
     *
     * @param p - the player.
     */
    public static void showPlayer(Player p) {
        if (p == null) throw new IllegalArgumentException("player cannot be null");
        plugin.getVisibilityAdjuster().showPlayer(p);
    }

    /**
     * * Checks if a player is allowed to see another player
     *
     * @param viewer - the viewer
     * @param viewed - the viewed player
     * @return TRUE if viewed is not vanished or viewer has the permission to see vanished players
     */
    public static boolean canSee(Player viewer, Player viewed) {
        if (viewer == null) throw new IllegalArgumentException("viewer cannot be null");
        if (!isInvisible(viewed)) return true;
        boolean enableSeePermission = getConfiguration().getBoolean("Configuration.Players.EnableSeePermission");
        return enableSeePermission && viewer.hasPermission("sv.see");
    }

    public static FileConfiguration getConfiguration() {
        return plugin.settings;
    }

    public static FileConfiguration getMessages() {
        return plugin.messages;
    }

    public static FileConfiguration getPlayerData() {
        return plugin.playerData;
    }

    public static void reloadConfig() {
        // messages
        plugin.messagesFile = new MessagesFile();
        plugin.messagesFile.saveDefaultConfig();
        plugin.messages = plugin.messagesFile.getConfig();
        // config
        plugin.settingsFile = new SettingsFile();
        plugin.settingsFile.saveDefaultConfig();
        plugin.settings = plugin.settingsFile.getConfig();
    }

    public static SuperVanish getPlugin() {
        return plugin;
    }

    public static void setPlugin(SuperVanish plugin) {
        VanishAPI.plugin = plugin;
    }
}