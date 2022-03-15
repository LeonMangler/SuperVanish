/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.features;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.supervanish.SuperVanish;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_EFFECT;
import static com.comphenix.protocol.PacketType.Play.Server.REMOVE_ENTITY_EFFECT;

public class NightVision extends Feature implements Runnable {

    private boolean suppressErrors = false;

    public static final int INFINITE_POTION_EFFECT_LENGTH = 32767;

    public NightVision(SuperVanish plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 20 * 60 * 2, 20 * 60 * 2);
    }

    @Override
    public boolean isActive() {
        return plugin.getSettings().getBoolean("InvisibilityFeatures.NightVisionEffect");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVanish(PlayerHideEvent e) {
        Player p = e.getPlayer();
        sendAddPotionEffect(p, new PotionEffect(PotionEffectType.NIGHT_VISION,
                INFINITE_POTION_EFFECT_LENGTH, 0));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReappear(PlayerShowEvent e) {
        Player p = e.getPlayer();
        sendRemovePotionEffect(p, PotionEffectType.NIGHT_VISION);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId()))
            return;
        sendAddPotionEffect(p, new PotionEffect(PotionEffectType.NIGHT_VISION,
                INFINITE_POTION_EFFECT_LENGTH, 0));
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getVanishStateMgr().isVanished(p.getUniqueId()))
            return;
        sendAddPotionEffect(p, new PotionEffect(PotionEffectType.NIGHT_VISION,
                INFINITE_POTION_EFFECT_LENGTH, 0));
    }

    @Override
    public void run() {
        // renew every now and then to prevent blinking bug
        for (UUID uuid : plugin.getVanishStateMgr().getOnlineVanishedPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            sendRemovePotionEffect(p, PotionEffectType.NIGHT_VISION);
            sendAddPotionEffect(p, new PotionEffect(PotionEffectType.NIGHT_VISION,
                    INFINITE_POTION_EFFECT_LENGTH, 0));
        }
    }

    private void sendAddPotionEffect(Player p, PotionEffect effect) {
        PacketContainer packet = new PacketContainer(ENTITY_EFFECT);
        //noinspection deprecation
        int effectID = effect.getType().getId();
        int amplifier = effect.getAmplifier();
        int duration = effect.getDuration();
        int entityID = p.getEntityId();
        // 1.18.2 changed effectID from byte to integer
        if (plugin.getVersionUtil().isOneDotXOrHigher(18) && packet.getIntegers().size() >= 3) {
            packet.getIntegers().write(0, entityID);
            packet.getIntegers().write(1, effectID);
            packet.getBytes().write(0, (byte) amplifier);
            packet.getIntegers().write(2, duration);
            // hide particles in 1.9
            packet.getBytes().write(1, (byte) 0);
        } else {
            packet.getIntegers().write(0, entityID);
            packet.getBytes().write(0, (byte) effectID);
            packet.getBytes().write(1, (byte) amplifier);
            packet.getIntegers().write(1, duration);
            // hide particles in 1.9
            packet.getBytes().write(2, (byte) 0);
        }
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Cannot send packet", e);
        } catch (Exception | NoClassDefFoundError e) {
            if (!suppressErrors) {
                plugin.logException(e);
                plugin.getLogger().warning("IMPORTANT: Please make sure that you are using the latest " +
                        "dev-build of ProtocolLib and that your server is up-to-date! This error likely " +
                        "happened inside of ProtocolLib code which is out of SuperVanish's control. It's part " +
                        "of an optional feature module and can be removed safely by disabling " +
                        "NightVisionEffect in the config file. Please report this " +
                        "error if you can reproduce it on an up-to-date server with only latest " +
                        "ProtocolLib and latest SV installed.");
                suppressErrors = true;
            }
        }
    }

    private void sendRemovePotionEffect(Player p, PotionEffectType type) {
        PacketContainer packet = new PacketContainer(REMOVE_ENTITY_EFFECT);
        //noinspection deprecation
        final int effectID = type.getId();
        final int entityID = p.getEntityId();
        // 1.7 and below
        if (!plugin.getVersionUtil().isOneDotXOrHigher(8)) {
            packet.getIntegers().write(0, entityID);
            packet.getBytes().write(0, (byte) effectID);
        } else if (plugin.getVersionUtil().isOneDotX(8)) {
            // 1.8
            packet.getIntegers().write(0, entityID);
            packet.getIntegers().write(1, effectID);
        } else {
            // 1.9 and higher
            packet.getEffectTypes().write(0, type);
            packet.getIntegers().write(0, entityID);
        }
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Cannot send packet", e);
        } catch (Exception | NoClassDefFoundError e) {
            if (!suppressErrors) {
                plugin.logException(e);
                plugin.getLogger().warning("IMPORTANT: Please make sure that you are using the latest " +
                        "dev-build of ProtocolLib and that your server is up-to-date! This error likely " +
                        "happened inside of ProtocolLib code which is out of SuperVanish's control. It's part " +
                        "of an optional feature module and can be removed safely by disabling " +
                        "NightVisionEffect in the config file. Please report this " +
                        "error if you can reproduce it on an up-to-date server with only latest " +
                        "ProtocolLib and latest SV installed.");
                suppressErrors = true;
            }
        }
    }
}
