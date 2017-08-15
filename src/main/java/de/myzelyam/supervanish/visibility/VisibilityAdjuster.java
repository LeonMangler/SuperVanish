/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.visibility;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.api.vanish.VanishAPI;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.hooks.DynmapHook;
import de.myzelyam.supervanish.hooks.EssentialsHook;
import de.myzelyam.supervanish.utils.OneDotEightUtils;
import de.myzelyam.supervanish.utils.ProtocolLibPacketUtils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.logging.Level;

public class VisibilityAdjuster {

    private final SuperVanish plugin;

    private final PlayerHider hider;

    public VisibilityAdjuster(SuperVanish plugin) {
        this.plugin = plugin;
        hider = new PlayerHider(plugin);
    }

    private FileConfiguration getSettings() {
        return plugin.settings;
    }

    public void hidePlayer(Player p) {
        try {
            // check p
            if (p == null)
                throw new IllegalArgumentException("The player cannot be null!");
            FileConfiguration messages = plugin.messages;
            String vanishBroadcast = messages.getString("Messages.VanishMessage");
            String vanishBroadcastWithPermission = messages
                    .getString("Messages.VanishMessageWithPermission");
            String onVanishMessage = messages.getString("Messages.OnVanish");
            // check state
            if (plugin.getAllInvisiblePlayers().contains(p.getUniqueId().toString())) {
                Bukkit.getLogger().log(Level.WARNING, "[SuperVanish] Error: Could not hide player "
                        + p.getName() + ", he is already invisible!");
                return;
            }
            // call event
            PlayerHideEvent event = new PlayerHideEvent(p);
            plugin.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) return;
            // Essentials hook
            if (plugin.getServer().getPluginManager().getPlugin("Essentials") != null
                    && getSettings().getBoolean("Configuration.Hooks.EnableEssentialsHook")) {
                EssentialsHook.hidePlayer(p);
            }
            // fly check
            if (getSettings().getBoolean("Configuration.Players.Fly.Enable")) {
                p.setAllowFlight(true);
            }
            // dynmap hook
            if (plugin.getServer().getPluginManager().getPlugin("dynmap") != null
                    && getSettings().getBoolean("Configuration.Hooks.EnableDynmapHook")) {
                DynmapHook.adjustVisibility(p, true);
            }
            // action bars
            if (getSettings().getBoolean("Configuration.Messages.DisplayActionBarsToInvisiblePlayers")
                    && plugin.getActionBarMgr() != null) {
                plugin.getActionBarMgr().addActionBar(p);
            }
            // adjust playerdata.yml file
            List<String> invisiblePlayers = plugin.getAllInvisiblePlayers();
            invisiblePlayers.add(p.getUniqueId().toString());
            plugin.playerData.set("InvisiblePlayers", invisiblePlayers);
            plugin.savePlayerData();
            // metadata
            p.setMetadata("vanished", new FixedMetadataValue(plugin, true));
            // tab packet
            if (plugin.getTablistPacketMgr() != null) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (!plugin.canSee(onlinePlayer, p) || p == onlinePlayer) continue;
                    plugin.getTablistPacketMgr().sendGameModeChangePacket(onlinePlayer, p, true);
                }
            }
            // vanish broadcast
            if (getSettings().getBoolean(
                    "Configuration.Messages.VanishReappearMessages.BroadcastMessageOnVanish")) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (!VanishAPI.canSee(onlinePlayer, p)) {
                        if (!getSettings().getBoolean(
                                "Configuration.Messages.VanishReappearMessages.SendMessageOnlyToAdmins")) {
                            onlinePlayer.sendMessage(plugin.convertString(vanishBroadcast, p));
                        }
                    } else {
                        if (!getSettings().getBoolean(
                                "Configuration.Messages.VanishReappearMessages.SendMessageOnlyToUsers")) {
                            if (!getSettings().getBoolean(
                                    "Configuration.Messages.VanishReappearMessages.SendDifferentMessages")) {
                                onlinePlayer.sendMessage(plugin.convertString(vanishBroadcast, p));
                            } else {
                                if (onlinePlayer.getUniqueId().toString()
                                        .equals(p.getUniqueId().toString()))
                                    onlinePlayer.sendMessage(
                                            plugin.convertString(vanishBroadcast, p));
                                else
                                    onlinePlayer.sendMessage(
                                            plugin.convertString(vanishBroadcastWithPermission, p));
                            }
                        }
                    }
                }
            }
            // send message
            p.sendMessage(plugin.convertString(onVanishMessage, p));
            // add night vision potion
            if (getSettings().getBoolean("Configuration.Players.AddNightVision"))
                try {
                    if (plugin.packetNightVision)
                        plugin.getProtocolLibPacketUtils().sendAddPotionEffect(p, new PotionEffect(
                                PotionEffectType.NIGHT_VISION, ProtocolLibPacketUtils.INFINITE_POTION_DURATION, 0));
                    else
                        p.addPotionEffect(new PotionEffect(
                                PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
                } catch (Exception e) {
                    plugin.getLogger().warning("Cannot apply night vision: " + e.getMessage());
                }
            // teams
            if (plugin.getTeamMgr() != null)
                plugin.getTeamMgr().setCantPush(p);
            // hide player
            hider.hideToAll(p);
        } catch (Exception e) {
            plugin.printException(e);
        }
    }

    public void showPlayer(Player p) {
        showPlayer(p, false);
    }

    public void showPlayer(final Player p, boolean hideJoinMsg) {
        try {
            // check p
            if (p == null)
                throw new IllegalArgumentException("The player cannot be null!");
            // preparations
            FileConfiguration messages = plugin.messages;
            String reappearBroadcast = messages
                    .getString("Messages.ReappearMessage");
            String reappearBroadcastWithPermission = messages
                    .getString("Messages.ReappearMessageWithPermission");
            String onReappearMessage = messages
                    .getString("Messages.OnReappear");
            // check state
            if (!plugin.getAllInvisiblePlayers().contains(p.getUniqueId().toString())) {
                Bukkit.getLogger().log(Level.WARNING, "[SuperVanish] Error: Could not show player "
                        + p.getName() + ", he is already visible!");
                return;
            }
            // call event
            PlayerShowEvent event = new PlayerShowEvent(p);
            plugin.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) return;
            // fly
            if (getSettings().getBoolean("Configuration.Players.Fly.DisableOnReappear")
                    && !p.hasPermission("sv.fly")
                    && p.getGameMode() != GameMode.CREATIVE
                    && (!plugin.isOneDotXOrHigher(8) || !OneDotEightUtils.isSpectator(p))) {
                p.setAllowFlight(false);
            }
            // essentials hook
            if (plugin.getServer().getPluginManager().getPlugin("Essentials") != null
                    && getSettings().getBoolean("Configuration.Hooks.EnableEssentialsHook")) {
                EssentialsHook.showPlayer(p);
            }
            // dynmap hook
            if (plugin.getServer().getPluginManager().getPlugin("dynmap") != null
                    && getSettings().getBoolean("Configuration.Hooks.EnableDynmapHook")) {
                DynmapHook.adjustVisibility(p, false);
            }
            // action bars
            if (getSettings().getBoolean("Configuration.Messages.DisplayActionBarsToInvisiblePlayers")
                    && plugin.getActionBarMgr() != null) {
                plugin.getActionBarMgr().removeActionBar(p);
            }
            // reappear broadcast
            if (getSettings().getBoolean(
                    "Configuration.Messages.VanishReappearMessages.BroadcastMessageOnReappear")
                    && !hideJoinMsg) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (!VanishAPI.canSee(onlinePlayer, p)) {
                        if (!getSettings().getBoolean(
                                "Configuration.Messages.VanishReappearMessages.SendMessageOnlyToAdmins")) {
                            onlinePlayer.sendMessage(plugin.convertString(reappearBroadcast, p));
                        }
                    } else {
                        if (!getSettings().getBoolean(
                                "Configuration.Messages.VanishReappearMessages.SendMessageOnlyToUsers")) {
                            if (!getSettings().getBoolean(
                                    "Configuration.Messages.VanishReappearMessages.SendDifferentMessages")) {
                                onlinePlayer.sendMessage(plugin.convertString(reappearBroadcast, p));
                            } else {
                                if (onlinePlayer.getUniqueId().toString()
                                        .equals(p.getUniqueId().toString()))
                                    onlinePlayer.sendMessage(
                                            plugin.convertString(reappearBroadcast, p));
                                else
                                    onlinePlayer.sendMessage(
                                            plugin.convertString(reappearBroadcastWithPermission, p));
                            }
                        }
                    }
                }
            }
            // chat message
            p.sendMessage(plugin.convertString(onReappearMessage, p));
            // adjust playerdata.yml file
            List<String> invisiblePlayers = plugin.getAllInvisiblePlayers();
            invisiblePlayers.remove(p.getUniqueId().toString());
            plugin.playerData.set("InvisiblePlayers", invisiblePlayers);
            plugin.savePlayerData();
            // metadata
            p.removeMetadata("vanished", plugin);
            // remove night vision
            if (getSettings().getBoolean("Configuration.Players.AddNightVision")) try {
                if (plugin.packetNightVision) {
                    plugin.getProtocolLibPacketUtils().sendRemovePotionEffect(p,
                            PotionEffectType.NIGHT_VISION);
                } else
                    p.removePotionEffect(PotionEffectType.NIGHT_VISION);
            } catch (Exception e) {
                plugin.getLogger().warning("Cannot remove night vision: " + e.getMessage());
            }
            // teams
            if (plugin.getTeamMgr() != null)
                plugin.getTeamMgr().setCanPush(p);
            // show player
            hider.showToAll(p);
            // tab packet
            if (plugin.getTablistPacketMgr() != null) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (!plugin.canSee(onlinePlayer, p) || p == onlinePlayer) continue;
                    plugin.getTablistPacketMgr().sendGameModeChangePacket(onlinePlayer, p, false);
                }
            }
        } catch (Exception e) {
            plugin.printException(e);
        }
    }

    public PlayerHider getHider() {
        return hider;
    }
}