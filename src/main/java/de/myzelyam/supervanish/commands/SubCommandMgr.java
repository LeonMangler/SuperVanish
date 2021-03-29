/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.commands;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.commands.subcommands.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SubCommandMgr {

    private SuperVanish plugin;

    public SubCommandMgr(SuperVanish plugin) {
        this.plugin = plugin;
    }

    public void execute(Command cmd, CommandSender sender, String[] args, String label) {
        try {
            // invalid usage by default
            Executable executable = new InvalidUsage(plugin);
            // vanish self #1
            if (args.length == 0) {
                if (sender instanceof Player)
                    executable = new VanishSelf(plugin);
                executable.execute(cmd, sender, args, label);
                return;
            }
            // vanish other
            if ((args[0].equalsIgnoreCase("on")
                    || args[0].equalsIgnoreCase("off")
                    || args[0].equalsIgnoreCase("vanish")
                    || args[0].equalsIgnoreCase("reappear")
                    || args[0].equalsIgnoreCase("enable")
                    || args[0].equalsIgnoreCase("disable"))
                    && args.length > 1
                    || Bukkit.getPlayer(args[0]) != null) {
                executable = Bukkit.getPlayer(args[0]) != null
                        && !(args[0].equalsIgnoreCase("on")
                        || args[0].equalsIgnoreCase("off")
                        || args[0].equalsIgnoreCase("vanish")
                        || args[0].equalsIgnoreCase("reappear")
                        || args[0].equalsIgnoreCase("enable")
                        || args[0].equalsIgnoreCase("disable"))
                        ? new VanishOther(Bukkit.getPlayer(args[0]), plugin)
                        : new VanishOther(plugin);
            }
            // vanish self #2
            if ((args[0].equalsIgnoreCase("on")
                    || args[0].equalsIgnoreCase("off")
                    || args[0].equalsIgnoreCase("vanish")
                    || args[0].equalsIgnoreCase("reappear")
                    || args[0].equalsIgnoreCase("enable")
                    || args[0].equalsIgnoreCase("disable")
                    || args[0].equalsIgnoreCase("-s"))
                    && args.length == 1) {
                executable = new VanishSelf(plugin);
            }
            //
            if (args[0].equalsIgnoreCase("help")
                    || args[0].equalsIgnoreCase("commands")
                    || args[0].equalsIgnoreCase("?")) {
                executable = new ShowHelp(plugin);
            }
            if (args[0].equalsIgnoreCase("printstacktrace")
                    || args[0].equalsIgnoreCase("logstacktrace")
                    || args[0].equalsIgnoreCase("dumpstacktrace")
                    || args[0].equalsIgnoreCase("stacktrace")) {
                executable = new PrintStacktrace(plugin);
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
                executable = new ToggleItemPickups(plugin);
            }
            if (args[0].equalsIgnoreCase("recreatecfg")
                    || args[0].equalsIgnoreCase("rccfg")
                    || args[0].equalsIgnoreCase("recreateconfig")
                    || args[0].equalsIgnoreCase("rcconfig")
                    || args[0].equalsIgnoreCase("updatecfg")
                    || args[0].equalsIgnoreCase("recreate")
                    || args[0].equalsIgnoreCase("recreatefiles")
                    || args[0].equalsIgnoreCase("rf")
                    || args[0].equalsIgnoreCase("rcf")
                    || args[0].equalsIgnoreCase("refi")) {
                executable = new RecreateFiles(plugin);
            }
            if (args[0].equalsIgnoreCase("reload")
                    || args[0].equalsIgnoreCase("rl")
                    || args[0].equalsIgnoreCase("re")
                    || args[0].equalsIgnoreCase("rlc")
                    || args[0].equalsIgnoreCase("rlcfg")
                    || args[0].equalsIgnoreCase("reloadconfig")
                    || args[0].equalsIgnoreCase("rlp")
                    || args[0].equalsIgnoreCase("rlpl")
                    || args[0].equalsIgnoreCase("reloadplugin")) {
                executable = new Reload(plugin);
            }
            if (args[0].equalsIgnoreCase("list")) {
                executable = new VanishedList(plugin);
            }
            if (args[0].equalsIgnoreCase("login")) {
                executable = new BroadcastLogin(plugin);
            }
            if (args[0].equalsIgnoreCase("logout")) {
                executable = new BroadcastLogout(plugin);
            }
            executable.execute(cmd, sender, args, label);
        } catch (Exception e) {
            plugin.logException(e);
        }
    }

    public List<String> onTabComplete(Command command, CommandSender sender,
                                      String alias, String[] args) {
        // don't provide any help at all if user doesn't have permission to
        // execute '/sv help'
        if (!CommandAction.hasAnyCmdPermission(sender, plugin)) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            String toComplete = args[0];
            // didn't start to type first argument
            if (toComplete.equalsIgnoreCase(""))
                return CommandAction.getAvailableFirstArguments(sender, plugin);
            // started to type it, only show args that start with given argument
            List<String> matchingFirstArguments = new ArrayList<>();
            for (String arg : CommandAction.getAvailableFirstArguments(sender, plugin)) {
                if (arg.toLowerCase(Locale.ENGLISH).startsWith(toComplete.toLowerCase(Locale.ENGLISH)))
                    matchingFirstArguments.add(arg);
            }
            if (matchingFirstArguments.isEmpty())
                return getOnlinePlayerNameTabCompletions(toComplete);
            return matchingFirstArguments;
            // extra commands
        } else if (args.length == 2) {
            // vanish other player tab completion
            if ((args[0].equalsIgnoreCase("on")
                    || args[0].equalsIgnoreCase("off")
                    || args[0].equalsIgnoreCase("vanish")
                    || args[0].equalsIgnoreCase("reappear")
                    || args[0].equalsIgnoreCase("enable")
                    || args[0].equalsIgnoreCase("disable"))
                    && CommandAction.VANISH_OTHER.checkPermission(sender, plugin)) {
                return getOnlinePlayerNameTabCompletions(args[1]);
            }
        }
        return Collections.emptyList();
    }

    private List<String> getOnlinePlayerNameTabCompletions(String arg) {
        List<String> playerNames = new ArrayList<>();
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.getName().toLowerCase(Locale.ENGLISH).startsWith(arg.toLowerCase(Locale.ENGLISH)))
                playerNames.add(p.getName());
        }
        return playerNames;
    }
}
