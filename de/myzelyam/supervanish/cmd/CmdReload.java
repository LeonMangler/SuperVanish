package de.myzelyam.supervanish.cmd;

import de.myzelyam.supervanish.SVUtils;
import de.myzelyam.supervanish.config.SettingsFile;
import de.myzelyam.supervanish.config.MessagesFile;
import org.bukkit.command.CommandSender;

public class CmdReload extends SVUtils {

    public CmdReload(CommandSender p, String[] args, String label) {
        if (canDo(p, CommandAction.RELOAD)) {
            // messages
            plugin.messagesFile = new MessagesFile();
            plugin.messagesFile.saveDefaultConfig();
            plugin.messages = plugin.messagesFile.getConfig();
            // config
            plugin.settingsFile = new SettingsFile();
            plugin.settingsFile.saveDefaultConfig();
            plugin.settings = plugin.settingsFile.getConfig();
            settings = plugin.settings;
            p.sendMessage(convertString(getMsg("ConfigReloadedMessage"), p));
        }
    }
}
