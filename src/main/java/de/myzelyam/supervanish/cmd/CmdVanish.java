/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.cmd;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.utils.PlayerCache;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CmdVanish extends SubCommand {

    public CmdVanish(SuperVanish plugin, CommandSender sender, String[] args, String label) {
        super(plugin);
        if (hasLayeredAccess(sender) || canDo(sender, CommandAction.VANISH_SELF)) {
            Player p = (Player) sender;
            Collection<Player> onlineInvisiblePlayers = getOnlineInvisiblePlayers();
            if (args.length == 0) {
                if (onlineInvisiblePlayers.contains(p))
                    showPlayer(p);
                else
                    hidePlayer(p);
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("off")) {
                    if (!onlineInvisiblePlayers.contains(p)) {
                        p.sendMessage(convertString(
                                getMsg("OnReappearWhileVisible"), p));
                        return;
                    }
                    showPlayer(p);
                } else if (args[0].equalsIgnoreCase("on")) {
                    if (onlineInvisiblePlayers.contains(p)) {
                        p.sendMessage(convertString(
                                getMsg("OnVanishWhileInvisible"), p));
                        return;
                    }
                    hidePlayer(p);
                }
            }
        }
    }

    private boolean hasLayeredAccess(CommandSender sender) {
        if (!(sender instanceof Player)) return false;
        if (!plugin.settings.getBoolean("Configuration.Players.LayeredSeeAndUsePermissions")) return false;
        Player p = (Player) sender;
        int level = PlayerCache.fromPlayer(p, plugin).getUsePermissionLevel();
        return level > 0 && p.hasPermission("sv.use.level" + level);
    }
}
