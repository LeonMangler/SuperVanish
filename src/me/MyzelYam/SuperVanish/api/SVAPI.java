/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package me.MyzelYam.SuperVanish.api;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.config.MessagesFile;
import de.myzelyam.supervanish.config.SettingsFile;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

@Deprecated
public class SVAPI {

    private static SuperVanish plugin;

    /**
     * @return A Stringlist of the UUID's of all hidden players
     */
    public static List<String> getInvisiblePlayers() {
        return plugin.playerData.getStringList("InvisiblePlayers");
    }

    /**
     * @param p - the player.
     * @return TRUE if the player is invisible, FALSE otherwise.
     */
    public static boolean isInvisible(Player p) {
        return p != null && plugin.playerData.getStringList("InvisiblePlayers").contains(p.getUniqueId().toString());
    }

    /**
     * Hides a player using SuperVanish
     *
     * @param p - the player.
     */
    public static void hidePlayer(Player p) {
        plugin.getVisibilityAdjuster().hidePlayer(p);
    }

    /**
     * * Shows a player using SuperVanish
     *
     * @param p - the player.
     */
    public static void showPlayer(Player p) {
        plugin.getVisibilityAdjuster().showPlayer(p);
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

    @SuppressWarnings("deprecation")
    public static void setPlugin(SuperVanish plugin) {
        SVAPI.plugin = plugin;
    }

}