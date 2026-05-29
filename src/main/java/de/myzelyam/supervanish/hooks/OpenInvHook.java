package de.myzelyam.supervanish.hooks;

import com.lishid.openinv.IOpenInv;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.api.vanish.PostPlayerHideEvent;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.utils.OneTimeWarning;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OpenInvHook extends PluginHook {

    private final OneTimeWarning warning;

    private final Set<UUID> alreadyHiddenBeforeVanishing = ConcurrentHashMap.newKeySet();

    public OpenInvHook(SuperVanish superVanish) {
        super(superVanish);
        warning = new OneTimeWarning(superVanish, null);
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
        } catch (Exception | LinkageError er) {
            warning.log(er);
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
        } catch (Exception | LinkageError er) {
            warning.log(er);
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
        } catch (Exception | LinkageError er) {
            warning.log(er);
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
        } catch (Exception | LinkageError er) {
            warning.log(er);
        }
    }

    public boolean openPlayerInventory(Player vanished, Player target) {
        IOpenInv openInv = (IOpenInv) plugin;
        try {
            openInv.openInventory(vanished, openInv.getSpecialInventory(target, true));
        } catch (Exception | LinkageError e) {
            warning.log(e);
            return false;
        }
        return true;
    }
}
