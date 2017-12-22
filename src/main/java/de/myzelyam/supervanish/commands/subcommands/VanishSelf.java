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
import org.bukkit.entity.Player;

public class VanishSelf extends SubCommand {

    public VanishSelf(SuperVanish plugin) {
        super(plugin);
    }

    @Override
    public void execute(Command cmd, CommandSender s, String[] args, String label) {
        if (canDo(s, CommandAction.VANISH_SELF, true)) {
            Player p = (Player) s;
            if (args.length == 0) {
                if (isVanished(p.getUniqueId()))
                    showPlayer(p);
                else
                    hidePlayer(p);
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("off")
                        || args[0].equalsIgnoreCase("reappear")
                        || args[0].equalsIgnoreCase("disable")) {
                    if (!isVanished(p.getUniqueId())) {
                        plugin.sendMessage(p, "NotVanishedError", p);
                        return;
                    }
                    showPlayer(p);
                } else if (args[0].equalsIgnoreCase("-s")) {
                    if (isVanished(p.getUniqueId()))
                        plugin.getVisibilityChanger().showPlayer(p, null, true);
                    else
                        plugin.getVisibilityChanger().hidePlayer(p, null, true);
                } else {
                    if (isVanished(p.getUniqueId())) {
                        plugin.sendMessage(p, "AlreadyVanishedError", p);
                        return;
                    }
                    hidePlayer(p);
                }
            }
        }
    }
}
