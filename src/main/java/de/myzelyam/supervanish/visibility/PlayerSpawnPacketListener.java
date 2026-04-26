package de.myzelyam.supervanish.visibility;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import static com.comphenix.protocol.PacketType.Play.Server.*;

public class PlayerSpawnPacketListener extends PacketAdapter {

    private final SuperVanish plugin;

    public PlayerSpawnPacketListener(SuperVanish plugin) {
        super(plugin, ListenerPriority.NORMAL, NAMED_ENTITY_SPAWN);
        this.plugin = plugin;
    }

    public static void register(SuperVanish plugin) {
        if (plugin.getVersionUtil().isOneDotXOrHigher(17)) {
            ProtocolLibrary.getProtocolManager().addPacketListener(new PlayerSpawnPacketListener(plugin));
        }

    }

    @Override
    public void onPacketSending(PacketEvent event) {
        // This prevents packet leaking vanished players around a non-admin player when he logs in
        try {
            if (event.getPacketType() == NAMED_ENTITY_SPAWN) {
                Player p = event.getPlayer();
                Entity entity = event.getPacket().getEntityModifier(p.getWorld()).read(0);
                if (entity instanceof Player) {
                    Player target = (Player) entity;
                    if (plugin.getVanishStateMgr().isVanished(target.getUniqueId()) && plugin.getLayeredPermissionChecker().hasPermissionToSee(p, target)) {
                        event.setCancelled(true);
                    }
                }
            }
        } catch (Exception er) {
            plugin.logException(er);
        }
    }
}
