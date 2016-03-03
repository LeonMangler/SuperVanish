package de.myzelyam.supervanish.cmd;

import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdToggleItemPickups extends SubCommand {

    public CmdToggleItemPickups(SuperVanish plugin, CommandSender sender, String[] args, String label) {
        super(plugin);
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
