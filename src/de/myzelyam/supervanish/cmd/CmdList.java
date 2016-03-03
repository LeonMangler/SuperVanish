package de.myzelyam.supervanish.cmd;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class CmdList extends SubCommand {

    public CmdList(SuperVanish plugin, CommandSender p, String[] args, String label) {
        super(plugin);
        if (canDo(p, CommandAction.LIST)) {
            String listMessage = getMsg("ListMessage");
            StringBuffer stringBuffer = new StringBuffer();
            List<String> allInvisiblePlayers = getAllInvisiblePlayers();
            if (allInvisiblePlayers.size() == 0) {
                stringBuffer = stringBuffer.append("none");
            }
            for (int i = 0; i < allInvisiblePlayers.size(); i++) {
                String uuidString = allInvisiblePlayers.get(i);
                UUID playerUUID = UUID.fromString(uuidString);
                String name = Bukkit.getOfflinePlayer(playerUUID).getName();
                if (Bukkit.getPlayer(playerUUID) == null) {
                    name = name + ChatColor.RED + "[offline]" + ChatColor.WHITE;
                }
                stringBuffer = stringBuffer.append(name);
                if (i != (allInvisiblePlayers.size() - 1))
                    stringBuffer = stringBuffer.append(ChatColor.GREEN).append(", ").append(ChatColor.WHITE);
            }
            listMessage = listMessage.replace("%l", stringBuffer.toString());
            p.sendMessage(convertString(listMessage, p));
        }
    }
}
