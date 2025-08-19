package de.myzelyam.supervanish.hooks;

import com.lishid.openinv.IOpenInv;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.api.vanish.PostPlayerHideEvent;
import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class OpenInvHook extends PluginHook {

    private boolean errorLogged = false;

    private final Set<UUID> alreadyHiddenBeforeVanishing = new HashSet<>();

    public OpenInvHook(SuperVanish superVanish) {
        super(superVanish);
    }

    @EventHandler
    public void onVanish(PostPlayerHideEvent e) {
        try {
            Player p = e.getPlayer();
            IOpenInv openInv = (IOpenInv) plugin;

            if (openInv.getSilentContainerStatus(p)) {
                alreadyHiddenBeforeVanishing.add(p.getUniqueId());
            } else {
                if (!p.hasPermission("sv.silentchest")) return;
                openInv.setSilentContainerStatus(p, true);
            }
        } catch (Exception | NoSuchMethodError | NoClassDefFoundError er) {
            if (!errorLogged) {
                superVanish.logException(er);
                errorLogged = true;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReappear(PlayerShowEvent e) {
        try {
            Player p = e.getPlayer();
            IOpenInv openInv = (IOpenInv) plugin;

            if (!alreadyHiddenBeforeVanishing.remove(p.getUniqueId())) {
                openInv.setSilentContainerStatus(p, false);
            }
        } catch (Exception | NoSuchMethodError | NoClassDefFoundError er) {
            if (!errorLogged) {
                superVanish.logException(er);
                errorLogged = true;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent e) {
        try {
            Player p = e.getPlayer();
            IOpenInv openInv = (IOpenInv) plugin;

            if (superVanish.getVanishStateMgr().isVanished(p.getUniqueId())) {
                if (openInv.getSilentContainerStatus(p)) {
                    alreadyHiddenBeforeVanishing.add(p.getUniqueId());
                } else {
                    if (!p.hasPermission("sv.silentchest")) return;
                    openInv.setSilentContainerStatus(p, true);
                }
            }
        } catch (Exception | NoSuchMethodError | NoClassDefFoundError er) {
            if (!errorLogged) {
                superVanish.logException(er);
                errorLogged = true;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        try {
            Player p = e.getPlayer();
            IOpenInv openInv = (IOpenInv) plugin;

            if (superVanish.getVanishStateMgr().isVanished(p.getUniqueId())) {
                if (!alreadyHiddenBeforeVanishing.remove(p.getUniqueId())) {
                    openInv.setSilentContainerStatus(p, false);
                }
            }
        } catch (Exception | NoSuchMethodError | NoClassDefFoundError er) {
            if (!errorLogged) {
                superVanish.logException(er);
                errorLogged = true;
            }
        }
    }

    public boolean openPlayerInventory(Player vanished, Player target) {
        IOpenInv openInv = (IOpenInv) plugin;
        try {
            openInv.openInventory(vanished, openInv.getSpecialInventory(target, true));
        } catch (Exception | NoSuchMethodError | NoClassDefFoundError e) {
            if (!errorLogged) {
                superVanish.logException(e);
                errorLogged = true;
            }
            return false;
        }
        return true;
    }
}
