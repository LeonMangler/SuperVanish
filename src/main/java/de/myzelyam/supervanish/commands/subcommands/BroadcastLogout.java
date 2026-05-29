/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.commands.subcommands;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.commands.CommandAction;
import de.myzelyam.supervanish.commands.SubCommand;
import de.myzelyam.supervanish.utils.FoliaUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class BroadcastLogout extends SubCommand {

    public BroadcastLogout(SuperVanish plugin) {
        super(plugin);
    }

    @Override
    public void execute(Command cmd, CommandSender p, String[] args, String label) {
        if (canDo(p, CommandAction.BROADCAST_LOGOUT, true)) {
            FoliaUtil.forEachOnlinePlayer(plugin, onlinePlayer ->
                    plugin.sendMessage(onlinePlayer, "VanishMessage", p, onlinePlayer));
        }
    }
}
