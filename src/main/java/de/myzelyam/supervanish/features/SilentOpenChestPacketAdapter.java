/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.features;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import static com.comphenix.protocol.PacketType.Play.Server.*;

public class SilentOpenChestPacketAdapter extends PacketAdapter {

    private final SilentOpenChest silentOpenChest;

    private boolean suppressErrors = false;

    public SilentOpenChestPacketAdapter(SilentOpenChest silentOpenChest) {
        super(silentOpenChest.plugin, ListenerPriority.LOW, PLAYER_INFO, ABILITIES,
                ENTITY_METADATA, GAME_STATE_CHANGE, NAMED_ENTITY_SPAWN);
        this.silentOpenChest = silentOpenChest;
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        try {
            Player receiver = event.getPlayer();
            if (receiver == null) return;
            if (event.getPacketType() == PLAYER_INFO) {
                if (silentOpenChest.hasSilentlyOpenedChest(receiver))
                    event.setCancelled(true);

            } else if (event.getPacketType() == GAME_STATE_CHANGE) {
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
            } else if (event.getPacketType() == NAMED_ENTITY_SPAWN) {
                if (silentOpenChest.plugin.getVanishStateMgr().isVanished(
                        receiver.getUniqueId())) {
                    if (!silentOpenChest.hasSilentlyOpenedChest(receiver)) return;
                    Entity entity = event.getPacket().getEntityModifier(receiver.getWorld()).read(0);
                    if (entity instanceof Player) {
                        Player p = (Player) entity;
                        if (p.getGameMode() == GameMode.SPECTATOR) {
                            event.setCancelled(true);
                        }
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
