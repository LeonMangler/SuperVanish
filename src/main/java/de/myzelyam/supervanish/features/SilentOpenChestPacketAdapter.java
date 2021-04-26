package de.myzelyam.supervanish.features;

import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static com.comphenix.protocol.PacketType.Play.Server.*;

public class SilentOpenChestPacketAdapter extends PacketAdapter {

    private final SilentOpenChest silentOpenChest;

    private boolean suppressErrors = false;

    public SilentOpenChestPacketAdapter(SilentOpenChest silentOpenChest) {
        super(silentOpenChest.plugin, ListenerPriority.LOW, PLAYER_INFO, ABILITIES,
                ENTITY_METADATA);
        this.silentOpenChest = silentOpenChest;
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        try {
            Player receiver = event.getPlayer();
            if (receiver == null) return;
            if (event.getPacketType() == PLAYER_INFO) {
                // multiple events share same packet object
                event.setPacket(event.getPacket().shallowClone());

                List<PlayerInfoData> infoDataList = new ArrayList<>(
                        event.getPacket().getPlayerInfoDataLists().read(0));
                for (PlayerInfoData infoData : ImmutableList.copyOf(infoDataList)) {
                    if (!silentOpenChest.plugin.getVisibilityChanger().getHider()
                            .isHidden(infoData.getProfile().getUUID(), receiver)
                            && silentOpenChest.plugin.getVanishStateMgr()
                            .isVanished(infoData.getProfile().getUUID())) {
                        Player vanishedTabPlayer = Bukkit.getPlayer(infoData.getProfile().getUUID());
                        if (infoData.getGameMode() == EnumWrappers.NativeGameMode.SPECTATOR
                                && silentOpenChest.hasSilentlyOpenedChest(vanishedTabPlayer)
                                && event.getPacket().getPlayerInfoAction().read(0)
                                == EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE) {
                            int latency;
                            try {
                                latency = infoData.getLatency();
                            } catch (NoSuchMethodError e) {
                                latency = 21;
                            }
                            PlayerInfoData newData = new PlayerInfoData(infoData.getProfile(),
                                    latency, EnumWrappers.NativeGameMode.SURVIVAL,
                                    infoData.getDisplayName());
                            infoDataList.remove(infoData);
                            infoDataList.add(newData);
                        }
                    }
                }
                event.getPacket().getPlayerInfoDataLists().write(0, infoDataList);
            } else if (event.getPacketType() == GAME_STATE_CHANGE) {
                // Currently unused due to ProtocolLib class loading bug
                if (silentOpenChest.plugin.getVanishStateMgr().isVanished(
                        receiver.getUniqueId())) {
                    try {
                        if (event.getPacket().getIntegers().read(0) != 3) return;
                    } catch (FieldAccessException e) {
                        // TODO
                    }
                    if (!silentOpenChest.hasSilentlyOpenedChest(receiver)) return;
                    event.setCancelled(true);
                }
            } else if (event.getPacketType() == ABILITIES) {
                if (silentOpenChest.plugin.getVanishStateMgr().isVanished(
                        receiver.getUniqueId())) {
                    if (!silentOpenChest.hasSilentlyOpenedChest(receiver)) return;
                    event.setCancelled(true);
                }
            } else if (event.getPacketType() == ENTITY_METADATA) {
                int entityID = event.getPacket().getIntegers().read(0);
                if (entityID == receiver.getEntityId()) {
                    if (silentOpenChest.plugin.getVanishStateMgr().isVanished(
                            receiver.getUniqueId())) {
                        if (!silentOpenChest.hasSilentlyOpenedChest(receiver)) return;
                        event.setCancelled(true);
                    }
                }
            }
        } catch (Exception | NoClassDefFoundError e) {
            if (!suppressErrors) {
                if (e.getMessage() == null
                        || !e.getMessage().endsWith("is not supported for temporary players.")) {
                    silentOpenChest.plugin.logException(e);
                    silentOpenChest.plugin.getLogger().warning("IMPORTANT: Please make sure that you are using the latest " +
                            "dev-build of ProtocolLib and that your server is up-to-date! This error likely " +
                            "happened inside of ProtocolLib code which is out of SuperVanish's control. It's part " +
                            "of an optional feature module and can be removed safely by disabling " +
                            "OpenChestsSilently in the config file. Please report this " +
                            "error if you can reproduce it on an up-to-date server with only latest " +
                            "ProtocolLib and latest SV installed.");
                    suppressErrors = true;
                }
            }
        }
    }
}
