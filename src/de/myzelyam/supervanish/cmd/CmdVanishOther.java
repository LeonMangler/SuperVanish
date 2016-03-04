/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.cmd;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CmdVanishOther extends SubCommand {

    public CmdVanishOther(SuperVanish plugin, CommandSender sender, String[] args, String label) {
        super(plugin);
        if (canDo(sender, CommandAction.VANISH_OTHER)) {
            if (args.length < 2) {
                sender.sendMessage(convertString(getMsg("InvalidUsageMessage"), sender));
                return;
            }
            Collection<Player> onlineInvisiblePlayers = getOnlineInvisiblePlayers();
            boolean hide = false;
            switch (args[0]) {
                case "on":
                    hide = true;
            }
            Player p = Bukkit.getPlayer(args[1]);
            if (p == null) {
                sender.sendMessage(
                        convertString(getMsg("PlayerNotOnlineMessage"), sender));
                return;
            }
            // check
            if (hide && onlineInvisiblePlayers.contains(p)) {
                sender.sendMessage(
                        convertString(getMsg("AlreadyInvisibleMessage"), p));
                return;
            } else if (!hide && !onlineInvisiblePlayers.contains(p)) {
                sender.sendMessage(
                        convertString(getMsg("AlreadyVisibleMessage"), p));
                return;
            }
            // hide
            if (hide) {
                hidePlayer(p);
                sender.sendMessage(convertString(getMsg("HideOtherMessage"), p));
                // show
            } else {
                showPlayer(p);
                sender.sendMessage(convertString(getMsg("ShowOtherMessage"), p));
            }
        }
    }
}
