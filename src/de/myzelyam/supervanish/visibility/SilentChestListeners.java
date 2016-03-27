
package de.myzelyam.supervanish.visibility;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import de.myzelyam.api.vanish.VanishAPI;
import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * How it works: Bukkit events are fired before sound-effects/animations -> use
 * events to get who's caused the effect
 * <p>
 * Warning: doesn't work when a vanished player and a normal player look into
 * the same chest at the same time because Minecraft doesn't send the packets
 * correctly then
 */
public class SilentChestListeners implements Listener {

    private final SuperVanish plugin;

    private final ProtocolManager protocolManager = ProtocolLibrary
            .getProtocolManager();

    private volatile Map<Location, UUIDTimeInfo> inventoryInfoMap = new ConcurrentHashMap<>();

    public SilentChestListeners(SuperVanish plugin) {
        this.plugin = plugin;
    }

    public void setupAnimationListener() {
        protocolManager.addPacketListener(new PacketAdapter(plugin,
                ListenerPriority.HIGH, PacketType.Play.Server.BLOCK_ACTION) {

            @Override
            public void onPacketSending(PacketEvent e) {
                try {
                    if (e.getPacketType() == PacketType.Play.Server.BLOCK_ACTION) {
                        Player listener = e.getPlayer();
                        // is the animation from a chest?
                        if (e.getPacket().getIntegers().read(1) != 1)
                            return;
                        BlockPosition position = e.getPacket()
                                .getBlockPositionModifier().read(0);
                        if (position == null)
                            return;
                        Location location = position.toVector()
                                .toLocation(listener.getWorld());
                        UUIDTimeInfo info = inventoryInfoMap.get(location);
                        if (info == null)
                            return;
                        e.setCancelled(true);
                    }
                } catch (Exception er) {
                    SilentChestListeners.this.plugin.printException(er);
                }
            }
        });
    }

    public void setupSoundListener() {
        protocolManager.addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.HIGH,
                        PacketType.Play.Server.NAMED_SOUND_EFFECT) {

                    @Override
                    public void onPacketSending(PacketEvent e) {
                        try {
                            if (e.getPacketType() == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                                Player listener = e.getPlayer();
                                // divide the location by 8, since it's a bit
                                // obfuscated
                                Location location = new Location(
                                        listener.getWorld(),
                                        e.getPacket().getIntegers().read(0) / 8,
                                        e.getPacket().getIntegers().read(1) / 8,
                                        e.getPacket().getIntegers().read(2)
                                                / 8);
                                check:
                                if (!inventoryInfoMap
                                        .containsKey(location)) {
                                    // search for adjacent blocks, too, since
                                    // the position of the sound is not exact
                                    List<Location> adjacentBlockLocations = getAdjacentBlockLocations(
                                            location);
                                    for (Location otherLocation : adjacentBlockLocations) {
                                        for (Location location1 : inventoryInfoMap
                                                .keySet()) {
                                            if (roughLocationEquals(location1,
                                                    otherLocation)) {
                                                location = location1;
                                                break check;
                                            }
                                        }
                                    }
                                    return;
                                }
                                // location has been validated
                                UUIDTimeInfo info = inventoryInfoMap
                                        .get(location);
                                if (info == null)
                                    return;
                                UUID uuid = info.uuid;
                                if (checkPlayerNearby(location, uuid)) {
                                    e.setCancelled(true);
                                }
                            }
                        } catch (Exception er) {
                            SilentChestListeners.this.plugin.printException(er);
                        }
                    }
                });
    }

    public void setupBukkitEventListener() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        class CleanUpTask extends BukkitRunnable {

            @Override
            public void run() {
                Set<Location> scheduledForCleanUp = new HashSet<>();
                for (Location key : inventoryInfoMap.keySet()) {
                    UUIDTimeInfo uuidTimeInfo = inventoryInfoMap.get(key);
                    if (System.currentTimeMillis()
                            - uuidTimeInfo.time > TimeUnit.SECONDS
                            .toMillis(2)) {
                        scheduledForCleanUp.add(key);
                    }
                }
                for (Location key : scheduledForCleanUp) {
                    // remove already used location keys
                    inventoryInfoMap.remove(key);
                }
            }
        }
        new CleanUpTask().runTaskTimer(plugin, 20 * 2, 20 * 2);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player))
            return;
        Player p = (Player) e.getPlayer();
        Location location = e.getInventory().getLocation();
        if (e.getInventory().getType() != InventoryType.CHEST) {
            return;
        }
        if (location == null)
            return;
        if (!VanishAPI.isInvisible(p)) {
            // not vanished? same block? invalidate!
            if (inventoryInfoMap.containsKey(location)) {
                inventoryInfoMap.remove(location);
            }
            return;
        }
        inventoryInfoMap.put(location,
                new UUIDTimeInfo(p.getUniqueId(), System.currentTimeMillis()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player))
            return;
        Player p = (Player) e.getPlayer();
        if (e.getInventory().getType() != InventoryType.CHEST) {
            return;
        }
        Location location = e.getInventory().getLocation();
        if (location == null)
            return;
        if (!VanishAPI.isInvisible(p)) {
            // not invisible? same block? invalidate!
            if (inventoryInfoMap.containsKey(location)) {
                inventoryInfoMap.remove(location);
            }
            return;
        }
        inventoryInfoMap.put(location,
                new UUIDTimeInfo(p.getUniqueId(), System.currentTimeMillis()));
    }

    private Location addToLocation(Location l, int x, int z) {
        return new Location(l.getWorld(), l.getX() + x, l.getY(), l.getZ() + z);
    }

    private List<Location> getAdjacentBlockLocations(Location loc) {
        List<Location> adjacentBlockLocations = new ArrayList<>();
        adjacentBlockLocations.add(addToLocation(loc, 1, 0));
        adjacentBlockLocations.add(addToLocation(loc, -1, 0));
        adjacentBlockLocations.add(addToLocation(loc, 0, -1));
        adjacentBlockLocations.add(addToLocation(loc, 0, 1));
        adjacentBlockLocations.add(addToLocation(loc, 1, 1));
        adjacentBlockLocations.add(addToLocation(loc, -1, -1));
        adjacentBlockLocations.add(addToLocation(loc, 1, -1));
        adjacentBlockLocations.add(addToLocation(loc, -1, 1));
        return adjacentBlockLocations;
    }

    private boolean roughLocationEquals(Location l1, Location l2) {
        return l1.getBlockX() == l2.getBlockX()
                && l1.getBlockY() == l2.getBlockY()
                && l1.getBlockZ() == l2.getBlockZ();
    }

    private boolean checkPlayerNearby(Location location, UUID playerUUID) {
        for (Player p : location.getWorld().getPlayers()) {
            if (!p.getUniqueId().equals(playerUUID))
                continue;
            if (p.getLocation().distanceSquared(location) < 40.0) {
                return true;
            }
        }
        return false;
    }

    private class UUIDTimeInfo {

        UUIDTimeInfo(UUID uuid, long time) {
            this.uuid = uuid;
            this.time = time;
        }

        UUID uuid;

        long time;
    }
}
