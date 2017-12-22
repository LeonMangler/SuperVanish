/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.commands;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.utils.Validation;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CommandAction {
    VANISH_SELF(
            "sv.use",
            false,
            "/sv [on|off]",
            "Toggles your visibility") {
        @Override
        public boolean checkPermission(CommandSender sender, SuperVanish plugin) {
            Validation.checkNotNull(plugin, "plugin cannot be null");
            return plugin.hasPermissionToVanish(sender);
        }
    },
    VANISH_OTHER(
            "sv.others",
            true,
            "/sv [on|off] <player>",
            "Shows or hides a player"),
    VANISHED_LIST(
            "sv.list",
            true,
            "/sv list",
            "Shows a list of vanished players"),
    BROADCAST_LOGIN(
            "sv.login",
            false,
            "/sv login",
            "Broadcasts a fake login message"),
    BROADCAST_LOGOUT(
            "sv.logout",
            false,
            "/sv logout",
            "Broadcasts a fake logout message"),
    TOGGLE_ITEM_PICKUPS(
            "sv.toggleitems",
            false,
            "/sv tipu",
            "Toggles picking up items") {
        @Override
        public boolean checkPermission(CommandSender sender, SuperVanish plugin) {
            return sender.hasPermission(getMainPermission()) || sender.hasPermission("sv.toggleitempickups");
        }
    },
    RECREATE_FILES(
            "sv.recreatefiles",
            true,
            "/sv recreatefiles",
            "Recreates the config") {
        @Override
        public boolean checkPermission(CommandSender sender, SuperVanish plugin) {
            return sender.hasPermission(getMainPermission()) || sender.hasPermission("sv.updatecfg");
        }
    },
    RELOAD(
            "sv.reload",
            true,
            "/sv reload",
            "Reloads all settings and messages"),
    PRINT_STACKTRACE(
            "sv.stacktrace",
            true,
            "/sv stacktrace",
            "Logs info for a bug report"),
    SHOW_HELP(
            "sv.help",
            true,
            "/sv help",
            "Shows this help page");

    /**
     * Use {@link #checkPermission(CommandSender, SuperVanish)} to check whether a {@link CommandSender} has
     * permission to perform an action or not
     */
    @Getter
    private final String mainPermission;
    private final boolean console;
    @Getter
    private final String usage, description;

    static List<String> getAvailableFirstArguments(CommandSender sender, SuperVanish plugin) {
        Validation.checkNotNull(plugin, "plugin cannot be null");
        List<String> list = new ArrayList<>();
        if (SHOW_HELP.checkPermission(sender, plugin))
            list.add("help");
        if (RECREATE_FILES.checkPermission(sender, plugin))
            list.add("recreatefiles");
        if (RELOAD.checkPermission(sender, plugin))
            list.add("reload");
        if (VANISHED_LIST.checkPermission(sender, plugin))
            list.add("list");
        if (VANISH_SELF.checkPermission(sender, plugin))
            list.add("on");
        if (VANISH_SELF.checkPermission(sender, plugin))
            list.add("off");
        if (TOGGLE_ITEM_PICKUPS.checkPermission(sender, plugin))
            list.add("tipu");
        if (PRINT_STACKTRACE.checkPermission(sender, plugin))
            list.add("stacktrace");
        return list;
    }

    public static boolean hasAnyCmdPermission(CommandSender sender, SuperVanish plugin) {
        for (CommandAction action : CommandAction.values())
            if (action.checkPermission(sender, plugin))
                return true;
        return false;
    }

    public boolean checkPermission(CommandSender sender, SuperVanish plugin) {
        return sender.hasPermission(getMainPermission());
    }

    public boolean usableByConsole() {
        return console;
    }
}