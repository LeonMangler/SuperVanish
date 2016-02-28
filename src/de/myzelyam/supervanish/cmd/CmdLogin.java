package de.myzelyam.supervanish.cmd;

import de.myzelyam.supervanish.SVUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class CmdLogin extends SVUtils {

    public CmdLogin(CommandSender p, String[] args, String label) {
        if (canDo(p, CommandAction.LOGIN)) {
            Bukkit.broadcastMessage(convertString(getMsg("ReappearMessage"), p));
        }
    }
}
