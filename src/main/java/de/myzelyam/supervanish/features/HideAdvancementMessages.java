package de.myzelyam.supervanish.features;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.utils.OneTimeWarning;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

// This feature is paper-only because the PlayerAdvancementDoneEvent#message() method doesn't exist in Spigot
public class HideAdvancementMessages extends Feature {

    private final OneTimeWarning warning;

    public HideAdvancementMessages(SuperVanish plugin) {
        super(plugin);
        warning = new OneTimeWarning(plugin, null);
    }

    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent e) {
        try {
            Player p = e.getPlayer();
            Component message = e.message();
            if (message == null) return;
            if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId())) return;
            if (e.message() == null) return;
            e.message(null);
            p.sendMessage(message);
        } catch (Exception er) {
            warning.log(er);
        }
    }

    @Override
    public boolean isActive() {
        return plugin.getSettings().getBoolean("MessageOptions.HideAdvancementMessages", true);
    }
}
