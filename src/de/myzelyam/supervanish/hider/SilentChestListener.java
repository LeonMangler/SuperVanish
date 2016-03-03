package de.myzelyam.supervanish.hider;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import de.myzelyam.api.vanish.VanishAPI;
import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class SilentChestListener {

    private final SuperVanish plugin;

    public SilentChestListener(SuperVanish plugin) {
        this.plugin = plugin;
    }

    public void setupAnimationListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.HIGH,
                        PacketType.Play.Server.BLOCK_ACTION) {

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
                                Location location = position.toVector().toLocation(
                                        listener.getWorld());
                                Block block = listener.getWorld().getBlockAt(location);
                                if (!(block.getState() instanceof Chest)) {
                                    return;
                                }
                                Chest chest = (Chest) block.getState();
                                Inventory inventory = chest.getBlockInventory();
                                List<HumanEntity> humanViewers = inventory
                                        .getViewers();
                                for (HumanEntity entity : humanViewers) {
                                    if (entity instanceof Player)
                                        if (VanishAPI
                                                .isInvisible((Player) entity)) {
                                            // cancel it since one of the
                                            // viewers is
                                            // invisible
                                            e.setCancelled(true);
                                        }
                                }
                            }
                        } catch (Exception er) {
                            SilentChestListener.this.plugin.printException(er);
                        }
                    }
                });
    }

    public void setupSoundListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.HIGH,
                        PacketType.Play.Server.NAMED_SOUND_EFFECT) {

                    @Override
                    public void onPacketSending(PacketEvent e) {
                        try {
                            if (e.getPacketType() == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                                Player listener = e.getPlayer();
                                // is the sound from a chest?
                                if (!(e.getPacket().getStrings().read(0)
                                        .equalsIgnoreCase("random.chestopen") || e
                                        .getPacket().getStrings().read(0)
                                        .equalsIgnoreCase("random.chestclosed")))
                                    return;
                                // divide the location by 8, since it's a bit
                                // obfuscated
                                Location loc = new Location(
                                        listener.getWorld(), e.getPacket()
                                        .getIntegers().read(0) / 8, e
                                        .getPacket().getIntegers()
                                        .read(1) / 8, e.getPacket()
                                        .getIntegers().read(2) / 8);
                                Block block = listener.getWorld().getBlockAt(loc);
                                check:
                                if (!(block.getState() instanceof Chest)) {
                                    // search for adjacent blocks, too, since
                                    // the
                                    // position of the sound is not exact
                                    List<Location> adjacentBlockLocations = getAdjacentBlockLocations(loc);
                                    for (Location otherLocation : adjacentBlockLocations) {
                                        Block otherBlock = listener.getWorld()
                                                .getBlockAt(otherLocation);
                                        if (otherBlock.getState() instanceof Chest) {
                                            block = otherBlock;
                                            loc = otherLocation;
                                            break check;
                                        }
                                    }
                                    return;
                                }
                                // use this code if it's a closed chest since a
                                // closed chest has no viewers
                                if (e.getPacket().getStrings().read(0)
                                        .equalsIgnoreCase("random.chestclosed")) {
                                    for (Player p : listener.getWorld()
                                            .getPlayers()) {
                                        if (VanishAPI.isInvisible(p)
                                                && p.getLocation()
                                                .distanceSquared(loc) < 8.5) {
                                            // cancel it since an invisible
                                            // player
                                            // is
                                            // nearby
                                            e.setCancelled(true);
                                        }
                                    }
                                    return;
                                }
                                // check for invisible viewers
                                Chest chest = (Chest) block.getState();
                                Inventory inventory = chest.getBlockInventory();
                                List<HumanEntity> humanViewers = inventory
                                        .getViewers();
                                for (HumanEntity entity : humanViewers) {
                                    if (entity instanceof Player)
                                        if (VanishAPI
                                                .isInvisible((Player) entity)) {
                                            // cancel it since one of the
                                            // viewers is
                                            // invisible
                                            e.setCancelled(true);
                                        }
                                }
                            }
                        } catch (Exception er) {
                            SilentChestListener.this.plugin.printException(er);
                        }
                    }
                });
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
}
