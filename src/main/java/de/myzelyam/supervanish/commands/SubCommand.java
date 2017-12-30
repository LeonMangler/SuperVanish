/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.commands;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public abstract class SubCommand implements Executable {

    protected final SuperVanish plugin;

    public SubCommand(SuperVanish plugin) {
        this.plugin = plugin;
    }

    protected Collection<UUID> getAllVanishedPlayers() {
        return plugin.getVanishStateMgr().getVanishedPlayers();
    }

    protected Collection<UUID> getOnlineVanishedPlayers() {
        return plugin.getVanishStateMgr().getOnlineVanishedPlayers();
    }

    public void hidePlayer(Player player) {
        plugin.getVisibilityChanger().hidePlayer(player);
    }

    public void showPlayer(Player player) {
        plugin.getVisibilityChanger().showPlayer(player);
    }


    public boolean isVanished(UUID uuid) {
        return plugin.getVanishStateMgr().isVanished(uuid);
    }

    public boolean canDo(CommandSender sender, CommandAction action, boolean sendErrors) {
        if (!(sender instanceof Player))
            if (!action.usableByConsole()) {
                if (sendErrors)
                    plugin.sendMessage(sender, "InvalidSender", sender);
                return false;
            }
        if (!action.checkPermission(sender, plugin)) {
            if (sendErrors)
                plugin.sendMessage(sender, "NoPermission", sender);
            return false;
        }
        return true;
    }
}