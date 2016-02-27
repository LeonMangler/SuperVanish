package de.myzelyam.supervanish.cmd;

import de.myzelyam.supervanish.SVUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdToggleItemPickups extends SVUtils {

    public CmdToggleItemPickups(CommandSender sender, String[] args, String label) {
        if (canDo(sender, CommandAction.TOGGLE_ITEM_PICKUPS)) {
            Player p = (Player) sender;
            p.sendMessage(convertString(getMsg("ToggledPickingUpItems"
                    + (toggleState(p) ? "On" : "Off")), p));
        }
    }

    private boolean toggleState(Player player) {
        boolean hasEnabled = playerData.getBoolean("PlayerData."
                + player.getUniqueId().toString() + ".itemPickUps");
        playerData.set("PlayerData." + player.getUniqueId().toString() + ".itemPickUps",
                !hasEnabled);
        plugin.savePlayerData();
        return !hasEnabled;
    }
}
