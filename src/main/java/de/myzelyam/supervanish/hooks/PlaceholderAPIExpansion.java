package de.myzelyam.supervanish.hooks;

import de.myzelyam.supervanish.SuperVanish;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final SuperVanish superVanish;

    public PlaceholderAPIExpansion(SuperVanish superVanish) {
        this.superVanish = superVanish;
    }

    @Override
    public String getIdentifier() {
        return "supervanish";
    }

    @Override
    public String getAuthor() {
        return "Myzelyam";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player p, String id) {
        try {
            if (id.equalsIgnoreCase("isvanished")
                    || id.equalsIgnoreCase("isinvisible")
                    || id.equalsIgnoreCase("vanished")
                    || id.equalsIgnoreCase("invisible"))
                return superVanish.getVanishStateMgr().isVanished(p.getUniqueId()) ? "Yes"
                        : "No";
            if (id.equalsIgnoreCase("onlinevanishedplayers")
                    || id.equalsIgnoreCase("onlinevanished")
                    || id.equalsIgnoreCase("invisibleplayers")
                    || id.equalsIgnoreCase("vanishedplayers")
                    || id.equalsIgnoreCase("hiddenplayers")) {
                Collection<UUID> onlineVanishedPlayers = superVanish.getVanishStateMgr()
                        .getOnlineVanishedPlayers();
                String playerListMessage = "";
                for (UUID uuid : onlineVanishedPlayers) {
                    Player onlineVanished = Bukkit.getPlayer(uuid);
                    if (onlineVanished == null) continue;
                    if (superVanish.getSettings().getBoolean(
                            "IndicationFeatures.LayeredPermissions.HideInvisibleInCommands", false)
                            && !superVanish.hasPermissionToSee(p, onlineVanished)) {
                        continue;
                    }
                    playerListMessage = playerListMessage + onlineVanished.getName() + ", ";
                }
                return playerListMessage.length() > 3
                        ? playerListMessage.substring(0, playerListMessage.length() - 2)
                        : playerListMessage;
            }
            if (id.equalsIgnoreCase("playercount")
                    || id.equalsIgnoreCase("onlineplayers")) {
                int playercount = Bukkit.getOnlinePlayers().size();
                for (UUID uuid : superVanish.getVanishStateMgr()
                        .getOnlineVanishedPlayers()) {
                    Player onlineVanished = Bukkit.getPlayer(uuid);
                    if (onlineVanished == null) continue;
                    if (p == null || !superVanish.canSee(p, onlineVanished)) playercount--;
                }
                return playercount + "";
            }
        } catch (Exception e) {
            superVanish.logException(e);
        }
        return "";
    }
}
