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

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class InvalidUsage extends SubCommand {

    public InvalidUsage(SuperVanish plugin) {
        super(plugin);
    }

    @Override
    public void execute(Command cmd, CommandSender sender, String[] args, String label) {
        if (!CommandAction.hasAnyCmdPermission(sender, plugin)) {
            plugin.sendMessage(sender, "NoPermission", sender);
            return;
        }
        plugin.sendMessage(sender, "InvalidUsage", sender);
    }
}
