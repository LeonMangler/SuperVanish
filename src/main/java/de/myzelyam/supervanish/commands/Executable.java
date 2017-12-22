/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface Executable {

    void execute(Command cmd, CommandSender sender, String[] args, String label);
}
