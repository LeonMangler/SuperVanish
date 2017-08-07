/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.cmd;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public abstract class SubCommand {

    protected final SuperVanish plugin;

    public SubCommand(SuperVanish plugin) {
        this.plugin = plugin;
    }

    protected List<String> getAllInvisiblePlayers() {
        return plugin.getAllInvisiblePlayers();
    }

    protected Collection<Player> getOnlineInvisiblePlayers() {
        return plugin.getOnlineInvisiblePlayers();
    }

    public void hidePlayer(Player p) {
        plugin.getVisibilityAdjuster().hidePlayer(p);
    }

    public void showPlayer(Player p) {
        plugin.getVisibilityAdjuster().showPlayer(p);
    }

    public void showPlayer(Player p, boolean hideJoinMsg) {
        plugin.getVisibilityAdjuster().showPlayer(p, hideJoinMsg);
    }

    protected boolean isVanished(Player p) {
        return getOnlineInvisiblePlayers().contains(p);
    }

    protected boolean canDo(CommandSender p, CommandAction cmd) {
        if (!(p instanceof Player))
            if (!cmd.canConsole()) {
                p.sendMessage(convertString(getMsg("InvalidSenderMessage"), p));
                return false;
            }
        if (!p.hasPermission(cmd.getPerm())) {
            p.sendMessage(convertString(getMsg("NoPermissionMessage"), p));
            return false;
        }
        return true;
    }

    protected String getMsg(String msg) {
        return plugin.getMsg(msg);
    }

    protected String convertString(String message, CommandSender p) {
        return plugin.convertString(message, p);
    }

    protected enum CommandAction {
        VANISH_SELF("sv.use", false), VANISH_OTHER("sv.others", true), LIST(
                "sv.list", true), LOGIN("sv.login", false), LOGOUT("sv.logout",
                false), TOGGLE_ITEM_PICKUPS("sv.toggleitempickups", false), UPDATE_CFG(
                "sv.updatecfg", true), RELOAD("sv.reload", true);

        private String perm;

        private boolean console;

        CommandAction(String perm, boolean console) {
            this.perm = perm;
            this.console = console;
        }

        String getPerm() {
            return perm;
        }

        boolean canConsole() {
            return console;
        }
    }
}
