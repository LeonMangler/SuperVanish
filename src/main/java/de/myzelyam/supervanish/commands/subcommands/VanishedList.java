/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.commands.subcommands;

import com.google.common.collect.ImmutableList;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.commands.CommandAction;
import de.myzelyam.supervanish.commands.SubCommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class VanishedList extends SubCommand {

    public VanishedList(SuperVanish plugin) {
        super(plugin);
    }

    @Override
    public void execute(Command cmd, final CommandSender sender, final String[] args, String label) {
        if (canDo(sender, CommandAction.VANISHED_LIST, true)) {
            String listMessage = plugin.getMessage("ListMessagePrefix");
            StringBuilder stringBuilder = new StringBuilder();
            List<UUID> allInvisiblePlayerUUIDs = ImmutableList.copyOf(getAllVanishedPlayers());
            if (allInvisiblePlayerUUIDs.isEmpty()) {
                stringBuilder = stringBuilder.append("none");
            }
            for (int i = 0; i < allInvisiblePlayerUUIDs.size(); i++) {
                UUID playerUUID = allInvisiblePlayerUUIDs.get(i);
                String name = Bukkit.getOfflinePlayer(playerUUID).getName();
                if (Bukkit.getPlayer(playerUUID) == null) {
                    name = name + ChatColor.RED + "[offline]" + ChatColor.WHITE;
                }
                stringBuilder = stringBuilder.append(name);
                if (i != allInvisiblePlayerUUIDs.size() - 1)
                    stringBuilder = stringBuilder.append(ChatColor.GREEN).append(", ").append(ChatColor.WHITE);
            }
            listMessage = listMessage.replace("%l", stringBuilder.toString());
            plugin.sendMessage(sender, listMessage, sender);
        }
    }
}
