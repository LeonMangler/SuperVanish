/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class LayeredPermissionChecker {

    private final SuperVanish plugin;
    private final FileConfiguration settings;

    public LayeredPermissionChecker(SuperVanish plugin) {
        this.plugin = plugin;
        settings = plugin.getSettings();
    }

    /**
     * @return TRUE if sender has *permission* to use /sv on, else FALSE; TRUE doesn't mean that sender can
     * actually use /sv on
     */
    public boolean hasPermissionToVanish(CommandSender sender) {
        if (settings.getBoolean(
                "IndicationFeatures.LayeredPermissions.LayeredSeeAndUsePermissions", false)) {
            if (sender.hasPermission("sv.use")) return true;
            int permissionLevel;
            if (sender instanceof Player)
                permissionLevel = plugin.getVanishPlayer((Player) sender).getUsePermissionLevel();
            else permissionLevel = getLayeredPermissionLevel(sender, "use");
            return permissionLevel > 0 && sender.hasPermission("sv.use.level" + permissionLevel);
        } else return sender.hasPermission("sv.use");
    }

    public boolean hasPermissionToSee(Player viewer, Player viewed) {
        if (viewer == null)
            throw new IllegalArgumentException("viewer cannot be null");
        if (viewer == viewed) return true;
        if (settings.getBoolean(
                "IndicationFeatures.LayeredPermissions.LayeredSeeAndUsePermissions", false)) {
            VanishPlayer vanishViewer = plugin.getVanishPlayer(viewer);
            VanishPlayer vanishViewed = plugin.getVanishPlayer(viewed);
            int viewerLevel = vanishViewer.getSeePermissionLevel();
            if (viewerLevel == 0) return false;
            int viewedLevel = Math.max(1, vanishViewed.getUsePermissionLevel());
            return viewerLevel >= viewedLevel;
        } else {
            boolean enableSeePermission = settings
                    .getBoolean("IndicationFeatures.LayeredPermissions.EnableSeePermission", true);
            return enableSeePermission && viewer.hasPermission("sv.see");
        }
    }

    public int getLayeredPermissionLevel(CommandSender sender, String permission) {
        boolean enableSeePermission = settings
                .getBoolean("IndicationFeatures.LayeredPermissions.EnableSeePermission", true);
        if (!enableSeePermission && permission.equalsIgnoreCase("see"))
            return 0;
        int maxLevel = settings.getInt("IndicationFeatures.LayeredPermissions.MaxLevel", 100);
        int level = sender.hasPermission("sv." + permission) ? 1 : 0;
        for (int i = 1; i <= maxLevel; i++)
            if (sender.hasPermission("sv." + permission + ".level" + i))
                level = i;
        return level;
    }
}
