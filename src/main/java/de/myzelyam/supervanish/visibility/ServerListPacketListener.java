/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.visibility;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Level;

public final class ServerListPacketListener {

    private ServerListPacketListener() {
    }

    public static void register(SuperVanish plugin) {
        plugin.getLogger().log(Level.INFO, "Hooked into Paper for server list vanish support");
        plugin.getServer().getPluginManager().registerEvents(new PaperServerPingListener(plugin), plugin);
    }

    public static boolean isEnabled(SuperVanish plugin) {
        final FileConfiguration config = plugin.getSettings();
        return config.getBoolean(
                "ExternalInvisibility.ServerList.AdjustAmountOfOnlinePlayers")
                || config.getBoolean(
                "ExternalInvisibility.ServerList.AdjustListOfLoggedInPlayers");
    }
}
