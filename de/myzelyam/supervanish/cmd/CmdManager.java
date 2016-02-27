package de.myzelyam.supervanish.cmd;

import de.myzelyam.supervanish.SVUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdManager extends SVUtils {

    public CmdManager(Command cmd, CommandSender sender, String[] args,
                      String label) {
        try {
            if (label.equalsIgnoreCase("vlist")) {
                new CmdList(sender, args, label);
                return;
            }
            if (cmd.getName().equalsIgnoreCase("sv")) {
                if (args.length == 0) {
                    if (sender instanceof Player)
                        new CmdVanish(sender, args, label);
                    else
                        sender.sendMessage(convertString(
                                getMsg("InvalidUsageMessage"), sender));
                    return;
                }
                if (args[0].equalsIgnoreCase("updateCfg")) {
                    new CmdUpdateCfg(sender, args, label);
                    return;
                }
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("reload")
                            || args[0].equalsIgnoreCase("rl")) {
                        new CmdReload(sender, args, label);
                        return;
                    }
                    if (args[0].equalsIgnoreCase("list")) {
                        new CmdList(sender, args, label);
                        return;
                    }
                    if (args[0].equalsIgnoreCase("on")
                            || args[0].equalsIgnoreCase("off")) {
                        new CmdVanish(sender, args, label);
                        return;
                    }
                    if (args[0].equalsIgnoreCase("toggleitempickups")
                            || args[0].equalsIgnoreCase("toggleitempickup")
                            || args[0].equalsIgnoreCase("toggleitem")
                            || args[0].equalsIgnoreCase("toggleitems")
                            || args[0].equalsIgnoreCase("togglepickup")
                            || args[0].equalsIgnoreCase("togglepickups")
                            || args[0].equalsIgnoreCase("pickup")
                            || args[0].equalsIgnoreCase("pickups")
                            || args[0].equalsIgnoreCase("tipu")
                            || args[0].equalsIgnoreCase("tip")) {
                        new CmdToggleItemPickups(sender, args, label);
                        return;
                    }
                    sender.sendMessage(convertString(
                            getMsg("InvalidUsageMessage"), sender));
                    return;
                }
                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("on")
                            || args[0].equalsIgnoreCase("off")) {
                        new CmdVanishOther(sender, args, label);
                        return;
                    }
                    sender.sendMessage(convertString(
                            getMsg("InvalidUsageMessage"), sender));
                    return;
                }
                sender.sendMessage(
                        convertString(getMsg("InvalidUsageMessage"), sender));
                return;
            }
            if (cmd.getName().equalsIgnoreCase("vlogin")) {
                new CmdLogin(sender, args, label);
                return;
            }
            if (cmd.getName().equalsIgnoreCase("vlogout")) {
                new CmdLogout(sender, args, label);
            }
        } catch (Exception e) {
            plugin.printException(e);
        }
    }
}
