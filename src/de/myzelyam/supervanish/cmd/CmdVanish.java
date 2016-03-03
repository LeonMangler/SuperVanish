package de.myzelyam.supervanish.cmd;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CmdVanish extends SubCommand {

    public CmdVanish(SuperVanish plugin, CommandSender sender, String[] args, String label) {
        super(plugin);
        if (canDo(sender, CommandAction.VANISH_SELF)) {
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
}
