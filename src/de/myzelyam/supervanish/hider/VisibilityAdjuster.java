/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.hider;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.config.MessagesFile;
import de.myzelyam.supervanish.hider.TabMgr.TabAction;
import de.myzelyam.supervanish.hooks.DynmapHook;
import de.myzelyam.supervanish.hooks.EssentialsHook;
import de.myzelyam.supervanish.utils.OneDotEightUtils;
import me.confuser.barapi.BarAPI;
import me.libraryaddict.disguise.DisguiseAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;

import java.util.List;
import java.util.logging.Level;

public class VisibilityAdjuster {

    private final SuperVanish plugin;

    private final PlayerHider hider;

    private final FileConfiguration settings;

    public VisibilityAdjuster(SuperVanish plugin) {
        this.plugin = plugin;
        this.settings = plugin.settings;
        hider = new PlayerHider(plugin);
    }

    public void hidePlayer(Player p) {
        try {
            // check p
            if (p == null)
                throw new NullPointerException("The player cannot be null!");
            // preparations
            MessagesFile messagesCfg = new MessagesFile();
            FileConfiguration messages = messagesCfg.getConfig();
            String bossBar = messages
                    .getString("Messages.BossBarVanishMessage");
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
            //call event
            PlayerHideEvent event = new PlayerHideEvent(p);
            @SuppressWarnings("deprecation")
            me.MyzelYam.SuperVanish.api.PlayerHideEvent deprecatedEvent =
                    new me.MyzelYam.SuperVanish.api.PlayerHideEvent(p);
            plugin.getServer().getPluginManager().callEvent(event);
            plugin.getServer().getPluginManager().callEvent(deprecatedEvent);
            if (event.isCancelled() || deprecatedEvent.isCancelled()) {
                return;
            }
            // /////
            // DisguiseCraft hook
            if (plugin.getServer().getPluginManager()
                    .getPlugin("DisguiseCraft") != null
                    && settings.getBoolean(
                    "Configuration.Hooks.EnableDisguiseCraftHook")) {
                DisguiseCraftAPI dcAPI = DisguiseCraft.getAPI();
                if (dcAPI.isDisguised(p)) {
                    p.sendMessage(
                            ChatColor.RED + "[SV] Please undisguise yourself.");
                    return;
                }
            }
            // LibsDisguises hook
            if (plugin.getServer().getPluginManager()
                    .getPlugin("LibsDisguises") != null
                    && settings.getBoolean(
                    "Configuration.Hooks.EnableLibsDisguisesHook")) {
                if (DisguiseAPI.isDisguised(p)) {
                    p.sendMessage(
                            ChatColor.RED + "[SV] Please undisguise yourself.");
                    return;
                }
            }
            // BarAPI hook
            if (plugin.getServer().getPluginManager()
                    .getPlugin("BarAPI") != null
                    && settings.getBoolean("Configuration.Messages.UseBarAPI")) {
                BarAPI.setMessage(p, plugin.convertString(bossBar, p), 100f);
            }
            // Essentials hook
            if (plugin.getServer().getPluginManager()
                    .getPlugin("Essentials") != null
                    && settings.getBoolean(
                    "Configuration.Hooks.EnableEssentialsHook")) {
                EssentialsHook.hidePlayer(p);
            }
            // fly check
            if (settings.getBoolean("Configuration.Players.Fly.Enable")) {
                p.setAllowFlight(true);
            }
            // dynmap hook
            if (plugin.getServer().getPluginManager()
                    .getPlugin("dynmap") != null
                    && settings.getBoolean("Configuration.Hooks.EnableDynmapHook")) {
                DynmapHook.adjustVisibility(p, true);
            }
            // action bars
            if (plugin.getServer().getPluginManager()
                    .getPlugin("ProtocolLib") != null
                    && settings.getBoolean(
                    "Configuration.Messages.DisplayActionBarsToInvisiblePlayers")
                    && !SuperVanish.SERVER_IS_ONE_DOT_SEVEN) {
                plugin.getActionBarMgr().addActionBar(p);
            }
            // vanish broadcast
            if (settings.getBoolean(
                    "Configuration.Messages.VanishReappearMessages.BroadcastMessageOnVanish")) {
                for (Player ap : Bukkit.getOnlinePlayers()) {
                    if (!(ap.hasPermission("sv.see") && settings.getBoolean(
                            "Configuration.Players.EnableSeePermission"))) {
                        if (!settings.getBoolean(
                                "Configuration.Messages.VanishReappearMessages.SendMessageOnlyToAdmins")) {
                            ap.sendMessage(plugin.convertString(vanishBroadcast, p));
                        }
                    } else {
                        if (!settings.getBoolean(
                                "Configuration.Messages.VanishReappearMessages.SendMessageOnlyToUsers")) {
                            if (!settings.getBoolean(
                                    "Configuration.Messages.VanishReappearMessages.SendDifferentMessages")) {
                                ap.sendMessage(plugin.convertString(vanishBroadcast, p));
                            } else {
                                if (ap.getUniqueId().toString()
                                        .equals(p.getUniqueId().toString()))
                                    ap.sendMessage(
                                            plugin.convertString(vanishBroadcast, p));
                                else
                                    ap.sendMessage(
                                            plugin.convertString(vanishBroadcastWithPermission, p));
                            }
                        }
                    }
                }
            }
            // adjust tablist
            if (settings.getBoolean("Configuration.Tablist.ChangeTabNames")) {
                plugin.getTabMgr().adjustTabname(p,
                        TabAction.SET_CUSTOM_TABNAME);
            }
            // send message
            p.sendMessage(plugin.convertString(onVanishMessage, p));
            // adjust playerdata.yml file
            List<String> invisiblePlayers = plugin.getAllInvisiblePlayers();
            invisiblePlayers.add(p.getUniqueId().toString());
            plugin.playerData.set("InvisiblePlayers", invisiblePlayers);
            plugin.savePlayerData();
            // ghost team
            if (settings.getBoolean("Configuration.Players.EnableGhostPlayers")
                    && plugin.ghostTeam != null) {
                //noinspection deprecation
                if (!plugin.ghostTeam.hasPlayer(p)) {
                    if (p.hasPermission("sv.see") || p.hasPermission("sv.use")
                            || plugin.getAllInvisiblePlayers()
                            .contains(p.getUniqueId().toString()))
                        //noinspection deprecation
                        plugin.ghostTeam.addPlayer(p);
                }
                // add invisibility potion
                p.addPotionEffect(new PotionEffect(
                        PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
            }
            // add night vision potion
            if (settings.getBoolean("Configuration.Players.AddNightVision"))
                p.addPotionEffect(new PotionEffect(
                        PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
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
                throw new NullPointerException("The player cannot be null!");
            // preparations
            MessagesFile messagesCfg = new MessagesFile();
            FileConfiguration messages = messagesCfg.getConfig();
            String bossBar = messages
                    .getString("Messages.BossBarReappearMessage");
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
            @SuppressWarnings("deprecation")
            me.MyzelYam.SuperVanish.api.PlayerShowEvent deprecatedEvent =
                    new me.MyzelYam.SuperVanish.api.PlayerShowEvent(p);
            plugin.getServer().getPluginManager().callEvent(event);
            plugin.getServer().getPluginManager().callEvent(deprecatedEvent);
            if (event.isCancelled() || deprecatedEvent.isCancelled()) {
                return;
            }
            // remove invisibility potion effect
            if (settings.getBoolean("Configuration.Players.EnableGhostPlayers")
                    && p.hasPotionEffect(PotionEffectType.INVISIBILITY))
                p.removePotionEffect(PotionEffectType.INVISIBILITY);
            // BarAPI hook
            if (plugin.getServer().getPluginManager()
                    .getPlugin("BarAPI") != null
                    && settings.getBoolean("Configuration.Messages.UseBarAPI")) {
                BarAPI.setMessage(p, plugin.convertString(bossBar, p), 100f);
                BarAPI.removeBar(p);
                Bukkit.getServer().getScheduler()
                        .scheduleSyncDelayedTask(plugin, new Runnable() {

                            @Override
                            public void run() {
                                BarAPI.removeBar(p);
                            }
                        }, 20);
            }
            // fly
            if (settings.getBoolean("Configuration.Players.Fly.DisableOnReappear")
                    && !p.hasPermission("sv.fly")
                    && p.getGameMode() != GameMode.CREATIVE
                    && (SuperVanish.SERVER_IS_ONE_DOT_SEVEN || !OneDotEightUtils.isSpectator(p))) {
                p.setAllowFlight(false);
            }
            // essentials hook
            if (plugin.getServer().getPluginManager()
                    .getPlugin("Essentials") != null
                    && settings.getBoolean(
                    "Configuration.Hooks.EnableEssentialsHook")) {
                EssentialsHook.showPlayer(p);
            }
            // dynmap hook
            if (plugin.getServer().getPluginManager()
                    .getPlugin("dynmap") != null
                    && settings.getBoolean("Configuration.Hooks.EnableDynmapHook")) {
                DynmapHook.adjustVisibility(p, false);
            }
            // action bars
            if (plugin.getServer().getPluginManager()
                    .getPlugin("ProtocolLib") != null
                    && settings.getBoolean(
                    "Configuration.Messages.DisplayActionBarsToInvisiblePlayers")
                    && !SuperVanish.SERVER_IS_ONE_DOT_SEVEN) {
                plugin.getActionBarMgr().removeActionBar(p);
            }
            // reappear broadcast
            if (settings.getBoolean(
                    "Configuration.Messages.VanishReappearMessages.BroadcastMessageOnReappear")
                    && !hideJoinMsg) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (!(onlinePlayer.hasPermission("sv.see") && settings.getBoolean(
                            "Configuration.Players.EnableSeePermission"))) {
                        if (!settings.getBoolean(
                                "Configuration.Messages.VanishReappearMessages.SendMessageOnlyToAdmins")) {
                            onlinePlayer.sendMessage(plugin.convertString(reappearBroadcast, p));
                        }
                    } else {
                        if (!settings.getBoolean(
                                "Configuration.Messages.VanishReappearMessages.SendMessageOnlyToUsers")) {
                            if (!settings.getBoolean(
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
            // adjust tablist
            if (settings.getBoolean("Configuration.Tablist.ChangeTabNames")) {
                plugin.getTabMgr().adjustTabname(p,
                        TabAction.RESTORE_NORMAL_TABNAME);
            }
            // chat message
            p.sendMessage(plugin.convertString(onReappearMessage, p));
            // adjust playerdata.yml file
            List<String> invisiblePlayers = plugin.getAllInvisiblePlayers();
            invisiblePlayers.remove(p.getUniqueId().toString());
            plugin.playerData.set("InvisiblePlayers", invisiblePlayers);
            plugin.savePlayerData();
            // remove night vision
            if (settings.getBoolean("Configuration.Players.AddNightVision"))
                p.removePotionEffect(PotionEffectType.NIGHT_VISION);
            // show player
            hider.showToAll(p);
        } catch (Exception e) {
            plugin.printException(e);
        }
    }

    public PlayerHider getHider() {
        return hider;
    }
}