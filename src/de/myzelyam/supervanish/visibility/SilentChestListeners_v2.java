/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
import de.myzelyam.supervanish.utils.ChestInventoryWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;

import javax.annotation.CheckForNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * How it works: Bukkit events are fired before sound-effects/animations -> use
 * events to get who's caused the effect
 * <p>
 * Warning: doesn't work when a vanished player and a normal player look into
 * the same chest at the same time because Minecraft doesn't send the packets
 * correctly then
 */
public class SilentChestListeners_v2 implements Listener {

    private final SuperVanish plugin;

    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    private final Map<Location, UUIDWrapper> locationToUUIDMap = new ConcurrentHashMap<>();

    private final Map<ChestInventoryWrapper, LocationWrapper> inventoryToLocationMap = new HashMap<>();

    public SilentChestListeners_v2(SuperVanish plugin) {
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
                        //if (e.getPacket().getIntegers().read(1) != 1)
                        //    return;
                        BlockPosition position = e.getPacket()
                                .getBlockPositionModifier().read(0);
                        if (position == null)
                            return;
                        Location location = position.toVector()
                                .toLocation(listener.getWorld());
                        UUIDWrapper info = locationToUUIDMap.get(location);
                        if (info == null)
                            return;
                        info.getUUID();
                        // System.out.println("Location->UUID successful (animation) -> increased uses");
                        e.setCancelled(true);
                        // System.out.println("Cancelled animation");
                    }
                } catch (Exception er) {
                    SilentChestListeners_v2.this.plugin.printException(er);
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
                                        e.getPacket().getIntegers().read(2) / 8
                                );
                                check:
                                if (!locationToUUIDMap.containsKey(location)) {
                                    // search for adjacent blocks, too, since
                                    // the position of the sound is not exact
                                    List<Location> adjacentBlockLocations = getAdjacentBlockLocations(location, true);
                                    for (Location otherLocation : adjacentBlockLocations) {
                                        for (Location location1 : locationToUUIDMap.keySet()) {
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
                                UUIDWrapper info = locationToUUIDMap.get(location);
                                if (info == null)
                                    return;
                                UUID uuid = info.getUUID();
                                // System.out.println("Location->UUID successful (sound) -> increased uses");
                                if (checkPlayerIsNearby(location, uuid)) {
                                    e.setCancelled(true);
                                    // System.out.println("Cancelled sound");
                                }
                            }
                        } catch (Exception er) {
                            SilentChestListeners_v2.this.plugin.printException(er);
                        }
                    }
                });
    }

    public void setupBukkitEventListener() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!VanishAPI.isInvisible(p)) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (!(block.getState() instanceof Chest)) return;
        ChestInventoryWrapper inventory;
        Chest chest1 = (Chest) block.getState();
        Chest chest2 = getAdjacentChest(chest1);
        if (chest2 != null) {
            //noinspection ConstantConditions,ConstantIfStatement
            if (true) return; // TODO: Fix double chests
            inventory = new ChestInventoryWrapper(chest1, chest2);
        } else {
            inventory = new ChestInventoryWrapper(chest1);
        }
        LocationWrapper info = new LocationWrapper(block.getLocation());
        //System.out.println("Stored Inventory->Location");
        inventoryToLocationMap.put(inventory, info);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player))
            return;
        Player p = (Player) e.getPlayer();
        if (!VanishAPI.isInvisible(p)) return;
        if (e.getInventory().getType() != InventoryType.CHEST) {
            return;
        }
        // System.out.println("KeyMap: " + inventoryToLocationMap.keySet());
        // System.out.println("Key: " + new ChestInventoryWrapper(e.getInventory()));
        LocationWrapper locationWrapper = inventoryToLocationMap.get(new ChestInventoryWrapper(e.getInventory()));
        if (locationWrapper == null) return;
        Location location = locationWrapper.getLocation();
        // System.out.println("Inventory->Location successful (open) -> increased uses");
        // System.out.println("Stored Location->UUID");
        locationToUUIDMap.put(location,
                new UUIDWrapper(p.getUniqueId()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player))
            return;
        Player p = (Player) e.getPlayer();
        if (!VanishAPI.isInvisible(p)) return;
        if (e.getInventory().getType() != InventoryType.CHEST) {
            return;
        }
        LocationWrapper locationInfo = inventoryToLocationMap.get(new ChestInventoryWrapper(e.getInventory()));
        if (locationInfo == null) return;
        // System.out.println("Inventory->Location successful (close) -> increased uses");
        Location location = locationInfo.getLocation();
        // System.out.println("Stored Location->UUID");
        locationToUUIDMap.put(location, new UUIDWrapper(p.getUniqueId()));
    }

    private Location addToLocation(Location l, int x, int z) {
        return new Location(l.getWorld(), l.getX() + x, l.getY(), l.getZ() + z);
    }

    @CheckForNull
    private Chest getAdjacentChest(Chest chest) {
        Material chestType = chest.getType();
        for (Location location : getAdjacentBlockLocations(chest.getLocation(), false)) {
            Block block = location.getBlock();
            if (block.getType() == chestType && block.getState() instanceof Chest)
                return (Chest) block.getState();
        }
        return null;
    }

    private boolean isDoubleChestInventory(Inventory inventory) {
        return inventory instanceof DoubleChestInventory;
    }

    private List<Location> getAdjacentBlockLocations(Location loc, boolean corners) {
        List<Location> adjacentBlockLocations = new ArrayList<>();
        adjacentBlockLocations.add(addToLocation(loc, 1, 0));
        adjacentBlockLocations.add(addToLocation(loc, -1, 0));
        adjacentBlockLocations.add(addToLocation(loc, 0, -1));
        adjacentBlockLocations.add(addToLocation(loc, 0, 1));
        if (corners) {
            adjacentBlockLocations.add(addToLocation(loc, 1, 1));
            adjacentBlockLocations.add(addToLocation(loc, -1, -1));
            adjacentBlockLocations.add(addToLocation(loc, 1, -1));
            adjacentBlockLocations.add(addToLocation(loc, -1, 1));
        }
        return adjacentBlockLocations;
    }


    private boolean roughLocationEquals(Location l1, Location l2) {
        return l1.getBlockX() == l2.getBlockX()
                && l1.getBlockY() == l2.getBlockY()
                && l1.getBlockZ() == l2.getBlockZ();
    }

    private boolean checkPlayerIsNearby(Location location, UUID playerUUID) {
        for (Player p : location.getWorld().getPlayers()) {
            if (!p.getUniqueId().equals(playerUUID))
                continue;
            if (p.getLocation().distanceSquared(location) < 40.0) {
                return true;
            }
        }
        return false;
    }

    private class UUIDWrapper {

        UUIDWrapper(UUID uuid) {
            this.uuid = uuid;
            this.uses = 0;
        }

        private UUID uuid;

        public UUID getUUID() {
            if (++uses >= 2) {
                // remove this value from the map after two uses
                for (Location key : new HashSet<>(locationToUUIDMap.keySet())) {
                    if (locationToUUIDMap.get(key) == this) {
                        locationToUUIDMap.remove(key);
                        // System.out.println("Removed location->uuid keys because uses >= 2");
                    }
                }
            }
            return uuid;
        }

        private int uses;

        public int getUses() {
            return uses;
        }
    }

    private class LocationWrapper {

        LocationWrapper(Location location) {
            this.location = location;
            this.uses = 0;
        }

        private Location location;

        public Location getLocation() {
            if (++uses >= 2) {
                // remove this value from the maps after two uses
                for (ChestInventoryWrapper key : new HashSet<>(inventoryToLocationMap.keySet())) {
                    if (inventoryToLocationMap.get(key) == this) {
                        inventoryToLocationMap.remove(key);
                        // System.out.println("Removed inventory->location keys because uses >= 2");
                    }
                }
            }
            return location;
        }

        private int uses;

        public int getUses() {
            return uses;
        }
    }
}
