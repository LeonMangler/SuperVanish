package de.myzelyam.supervanish.listeners;

import de.myzelyam.supervanish.SuperVanish;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.util.List;

public class AdvancementListener implements Listener {

    private final SuperVanish plugin;

    private final FileConfiguration config;

    public AdvancementListener(SuperVanish plugin) {
        this.plugin = plugin;
        config = plugin.getSettings();
    }

    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent e) {
        try {
            Player p = e.getPlayer();
            Component message = e.message();
            if (message == null) return;
            if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId())) return;
            if (e.message() != null && plugin.getSettings().getBoolean("MessageOptions.HideAdvancementMessages", true)) {
                e.message(null);
                p.sendMessage(message);
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }
}
