/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.visibility.hiders;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.utils.BukkitPlayerHidingUtil;
import de.myzelyam.supervanish.utils.FoliaUtil;
import de.myzelyam.supervanish.visibility.hiders.modules.TabCompleteModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;

public class PreventionHider extends PlayerHider implements BooleanSupplier {
    public PreventionHider(SuperVanish plugin) {
        super(plugin);
        if (!BukkitPlayerHidingUtil.isNewPlayerHidingAPISupported(plugin)) {
            FoliaUtil.runGlobalTimer(plugin, this, 2, 2);
        }
        if (plugin.isUseProtocolLib()
                && plugin.getSettings().getBoolean("InvisibilityFeatures.ModifyTabCompletePackets", true)
                && !plugin.getVersionUtil().isMinecraftAtLeast("1.21")) {
            // Not supported anymore on 1.21 and above (ProtocolLib broken)
            TabCompleteModule.register(plugin, this);
        }
    }

    @Override
    public boolean setHidden(Player player, Player viewer, boolean hidden) {
        if (super.setHidden(player, viewer, hidden) || BukkitPlayerHidingUtil.isNewPlayerHidingAPISupported(plugin)) {
            FoliaUtil.runAtEntity(plugin, viewer, () -> {
                if (!viewer.isOnline()) return;
                if (hidden) BukkitPlayerHidingUtil.hidePlayer(player, viewer, plugin);
                else BukkitPlayerHidingUtil.showPlayer(player, viewer, plugin);
            });
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return "Prevention";
    }

    @Override
    public boolean getAsBoolean() {
        for (Map.Entry<UUID, Set<UUID>> entry : playerHiddenFromPlayersMap.entrySet()) {
            if (BukkitPlayerHidingUtil.isNewPlayerHidingAPISupported(plugin)) {
                return false;
            }
            Player hidden = Bukkit.getPlayer(entry.getKey());
            if (hidden == null) continue;
            for (UUID viewerUuid : entry.getValue()) {
                Player viewer = Bukkit.getPlayer(viewerUuid);
                if (viewer == null) continue;
                FoliaUtil.runAtEntity(plugin, viewer, () -> {
                    if (viewer.isOnline()) BukkitPlayerHidingUtil.hidePlayer(hidden, viewer, plugin);
                });
            }
        }
        return true;
    }
}
