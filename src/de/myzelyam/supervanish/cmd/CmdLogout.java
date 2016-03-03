package de.myzelyam.supervanish.cmd;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class CmdLogout extends SubCommand {

    public CmdLogout(SuperVanish plugin, CommandSender p, String[] args, String label) {
        super(plugin);
        if (canDo(p, CommandAction.LOGOUT)) {
            Bukkit.broadcastMessage(convertString(getMsg("VanishMessage"), p));
        }
    }
}
