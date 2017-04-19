/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.cmd;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.config.MessagesFile;
import de.myzelyam.supervanish.config.SettingsFile;
import org.bukkit.command.CommandSender;

public class CmdReload extends SubCommand {

    public CmdReload(SuperVanish plugin, CommandSender p, String[] args, String label) {
        super(plugin);
        if (canDo(p, CommandAction.RELOAD)) {
            // messages
            plugin.messagesFile = new MessagesFile();
            plugin.messagesFile.saveDefaultConfig();
            plugin.messages = plugin.messagesFile.getConfig();
            // config
            plugin.settingsFile = new SettingsFile();
            plugin.settingsFile.saveDefaultConfig();
            plugin.settings = plugin.settingsFile.getConfig();
            p.sendMessage(convertString(getMsg("ConfigReloadedMessage"), p).split("\\n"));
        }
    }
}
