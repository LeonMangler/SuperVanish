package de.myzelyam.supervanish.cmd;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class CmdLogin extends SubCommand {

    public CmdLogin(SuperVanish plugin, CommandSender p, String[] args, String label) {
        super(plugin);
        if (canDo(p, CommandAction.LOGIN)) {
            Bukkit.broadcastMessage(convertString(getMsg("ReappearMessage"), p));
        }
    }
}
