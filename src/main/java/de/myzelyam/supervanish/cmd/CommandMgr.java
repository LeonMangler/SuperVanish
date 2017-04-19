/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.cmd;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandMgr {

    public CommandMgr(SuperVanish plugin, Command cmd, CommandSender sender, String[] args,
                      String label) {
        try {
            if (label.equalsIgnoreCase("vlist")) {
                new CmdList(plugin, sender, args, label);
                return;
            }
            if (cmd.getName().equalsIgnoreCase("sv")) {
                if (args.length == 0) {
                    if (sender instanceof Player)
                        new CmdVanish(plugin, sender, args, label);
                    else
                        sender.sendMessage(plugin.convertString(
                                plugin.getMsg("InvalidUsageMessage"), sender));
                    return;
                }
                if (args[0].equalsIgnoreCase("updateCfg")) {
                    new CmdUpdateCfg(plugin, sender, args, label);
                    return;
                }
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("reload")
                            || args[0].equalsIgnoreCase("rl")) {
                        new CmdReload(plugin, sender, args, label);
                        return;
                    }
                    if (args[0].equalsIgnoreCase("list")) {
                        new CmdList(plugin, sender, args, label);
                        return;
                    }
                    if (args[0].equalsIgnoreCase("on")
                            || args[0].equalsIgnoreCase("off")) {
                        new CmdVanish(plugin, sender, args, label);
                        return;
                    }
                    if (args[0].equalsIgnoreCase("toggleitempickups")
                            || args[0].equalsIgnoreCase("toggleitempickup")
                            || args[0].equalsIgnoreCase("toggleitem")
                            || args[0].equalsIgnoreCase("toggleitems")
                            || args[0].equalsIgnoreCase("togglepickup")
                            || args[0].equalsIgnoreCase("togglepickups")
                            || args[0].equalsIgnoreCase("pickup")
                            || args[0].equalsIgnoreCase("pickups")
                            || args[0].equalsIgnoreCase("tipu")
                            || args[0].equalsIgnoreCase("tip")) {
                        new CmdToggleItemPickups(plugin, sender, args, label);
                        return;
                    }
                    sender.sendMessage(plugin.convertString(
                            plugin.getMsg("InvalidUsageMessage"), sender));
                    return;
                }
                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("on")
                            || args[0].equalsIgnoreCase("off")) {
                        new CmdVanishOther(plugin, sender, args, label);
                        return;
                    }
                    sender.sendMessage(plugin.convertString(
                            plugin.getMsg("InvalidUsageMessage"), sender));
                    return;
                }
                sender.sendMessage(
                        plugin.convertString(plugin.getMsg("InvalidUsageMessage"), sender));
                return;
            }
            if (cmd.getName().equalsIgnoreCase("vlogin")) {
                new CmdLogin(plugin, sender, args, label);
                return;
            }
            if (cmd.getName().equalsIgnoreCase("vlogout")) {
                new CmdLogout(plugin, sender, args, label);
            }
        } catch (Exception e) {
            plugin.printException(e);
        }
    }
}
