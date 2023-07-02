/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.commands.subcommands;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.VanishPlayer;
import de.myzelyam.supervanish.commands.CommandAction;
import de.myzelyam.supervanish.commands.SubCommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleItemPickups extends SubCommand {

    public ToggleItemPickups(SuperVanish plugin) {
        super(plugin);
    }

    @Override
    public void execute(Command cmd, CommandSender sender, String[] args, String label) {
        if (canDo(sender, CommandAction.TOGGLE_ITEM_PICKUPS, true)) {
            Player p = (Player) sender;
            plugin.sendMessage(p, plugin.getMessage("ToggledPickingUpItems"
                    + (toggleState(plugin.getVanishPlayer(p)) ? "On" : "Off")), p);
        }
    }

    private boolean toggleState(VanishPlayer p) {
        boolean hasEnabled = plugin.getPlayerData().getBoolean("PlayerData."
                + p.getPlayer().getUniqueId() + ".itemPickUps");
        plugin.getPlayerData().set("PlayerData." + p.getPlayer().getUniqueId() + ".itemPickUps", !hasEnabled);
        p.setItemPickUps(!hasEnabled);
        plugin.getConfigMgr().getPlayerDataFile().save();
        return !hasEnabled;
    }
}
