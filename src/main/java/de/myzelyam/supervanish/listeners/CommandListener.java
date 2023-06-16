package de.myzelyam.supervanish.listeners;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {

    private final SuperVanish plugin;
    private final FileConfiguration config;

    public CommandListener(SuperVanish plugin) {
        this.plugin = plugin;
        config = plugin.getSettings();
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onCommand(PlayerCommandPreprocessEvent e) {
        try {
            if (!plugin.getSettings().getBoolean("MessageOptions.DisableMsgCommand", true)) return;
            Player p = e.getPlayer();
            if (p.hasPermission("sv.bypass")) return;
            String[] args = e.getMessage().split(" ");
            if (args.length < 3) return;
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !plugin.getVanishStateMgr().isVanished(target.getUniqueId())) return;

            String message = plugin.getMessage("FakeNotOnline");

            String command = e.getMessage().replace("/", "");
            String cmd = command.split(" ")[0];
            String[] blockedCommands = {"msg", "tell", "w"};
            CommandMap commandMap = Bukkit.getServer().getCommandMap();

            for (String blockedCommand : blockedCommands) {
                if (cmd.equalsIgnoreCase("minecraft:" + blockedCommand)) {
                    e.getPlayer().sendMessage(message);
                    e.setCancelled(true);
                    return;
                } else if (cmd.equalsIgnoreCase(blockedCommand) && !(commandMap.getCommand(blockedCommand) instanceof PluginCommand)) {
                    e.getPlayer().sendMessage(message);
                    e.setCancelled(true);
                    return;
                }
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }
}
