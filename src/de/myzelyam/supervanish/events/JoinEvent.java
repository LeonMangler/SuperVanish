/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.events;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.visibility.TabMgr.TabAction;
import de.myzelyam.supervanish.hooks.EssentialsHook;
import me.confuser.barapi.BarAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class JoinEvent implements EventExecutor, Listener {
    private final SuperVanish plugin;

    public JoinEvent(SuperVanish plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration getSettings() {
        return plugin.settings;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(Listener listener, Event event) {
        try {
            if (event instanceof PlayerJoinEvent) {
                PlayerJoinEvent e = (PlayerJoinEvent) event;
                final Player p = e.getPlayer();
                final List<String> invisiblePlayers = plugin.getAllInvisiblePlayers();
                // compatibility delays
                int tabDelay = getSettings()
                        .getInt("Configuration.CompatibilityOptions.ActionDelay.TabNameChangeDelayOnJoinInTicks");
                if (!getSettings().getBoolean("Configuration.CompatibilityOptions.ActionDelay.Enable"))
                    tabDelay = 0;
                int invisibilityDelay = getSettings()
                        .getInt("Configuration.CompatibilityOptions.ActionDelay.InvisibilityPotionDelayOnJoinInTicks");
                if (!getSettings().getBoolean("Configuration.CompatibilityOptions.ActionDelay.Enable"))
                    invisibilityDelay = 0;
                // ghost players
                if (getSettings().getBoolean("Configuration.Players.EnableGhostPlayers")
                        && plugin.ghostTeam != null
                        && !plugin.ghostTeam.hasPlayer(p)) {
                    if (p.hasPermission("sv.see") || p.hasPermission("sv.use")
                            || invisiblePlayers.contains(p.getUniqueId().toString()))
                        plugin.ghostTeam.addPlayer(p);
                }
                // Join-Message
                if (getSettings().getBoolean(
                        "Configuration.Messages.HideNormalJoinAndLeaveMessagesWhileInvisible",
                        true)
                        && invisiblePlayers.contains(p.getUniqueId().toString())) {
                    e.setJoinMessage(null);
                }
                // vanished:
                if (invisiblePlayers.contains(p.getUniqueId().toString())) {
                    // Essentials
                    if (plugin.getServer().getPluginManager()
                            .getPlugin("Essentials") != null
                            && getSettings().getBoolean("Configuration.Hooks.EnableEssentialsHook")) {
                        EssentialsHook.hidePlayer(p);
                    }
                    // remember message
                    if (getSettings().getBoolean("Configuration.Messages.RememberInvisiblePlayersOnJoin")) {
                        p.sendMessage(plugin.convertString(
                                plugin.getMsg("RememberMessage"), p));
                    }
                    // BAR-API
                    if (plugin.getServer().getPluginManager()
                            .getPlugin("BarAPI") != null
                            && getSettings().getBoolean("Configuration.Messages.UseBarAPI")) {
                        displayBossBar(p);
                    }
                    // hide
                    plugin.getVisibilityAdjuster().getHider().hideToAll(p);
                    // re-add invisibility
                    if (getSettings().getBoolean("Configuration.Players.EnableGhostPlayers")) {
                        boolean isInvisible = false;
                        for (PotionEffect potionEffect : p.getActivePotionEffects())
                            if (potionEffect.getType() == PotionEffectType.INVISIBILITY) isInvisible = true;
                        if (!isInvisible) {
                            if (invisibilityDelay > 0) {
                                Bukkit.getServer().getScheduler()
                                        .scheduleSyncDelayedTask(plugin, new Runnable() {

                                            @Override
                                            public void run() {
                                                p.addPotionEffect(new PotionEffect(
                                                        PotionEffectType.INVISIBILITY,
                                                        Integer.MAX_VALUE, 1));
                                            }
                                        }, invisibilityDelay);
                            } else {
                                p.addPotionEffect(new PotionEffect(
                                        PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
                            }
                        }
                    }
                    // re-add action bar
                    if (plugin.getServer().getPluginManager()
                            .getPlugin("ProtocolLib") != null
                            && getSettings().getBoolean("Configuration.Messages.DisplayActionBarsToInvisiblePlayers")
                            && !SuperVanish.SERVER_IS_ONE_DOT_SEVEN) {
                        plugin.getActionBarMgr().addActionBar(p);
                    }
                    //
                }
                // not necessarily vanished:
                //
                // hide vanished players to player
                plugin.getVisibilityAdjuster().getHider().hideAllInvisibleTo(p);
                // TAB //
                if (getSettings().getBoolean("Configuration.Tablist.ChangeTabNames")
                        && invisiblePlayers.contains(p.getUniqueId().toString())) {
                    if (tabDelay > 0) {
                        Bukkit.getServer()
                                .getScheduler()
                                .scheduleSyncDelayedTask(plugin,
                                        new Runnable() {

                                            @Override
                                            public void run() {
                                                plugin.getTabMgr()
                                                        .adjustTabname(
                                                                p,
                                                                TabAction.SET_CUSTOM_TABNAME);
                                            }
                                        }, tabDelay);
                    } else {
                        plugin.getTabMgr().adjustTabname(p,
                                TabAction.SET_CUSTOM_TABNAME);
                    }
                }
                // remove invisibility if required
                if (plugin.playerData.getBoolean("PlayerData.Player."
                        + p.getUniqueId().toString() + ".remInvis")
                        && !invisiblePlayers.contains(p.getUniqueId().toString())) {
                    removeInvisibility(p);
                }
            }
        } catch (Exception er) {
            plugin.printException(er);
        }
    }

    private void displayBossBar(final Player p) {
        final String bossBarVanishMessage = plugin.getMsg("Messages.BossBarVanishMessage");
        String bossBarRememberMessage = plugin.getMsg("Messages.BossBarRememberMessage");
        BarAPI.setMessage(p, plugin.convertString(bossBarRememberMessage, p), 100f);
        Bukkit.getServer().getScheduler()
                .scheduleSyncDelayedTask(plugin, new Runnable() {

                    @Override
                    public void run() {
                        final List<String> invisiblePlayers = plugin.playerData
                                .getStringList("InvisiblePlayers");
                        if (invisiblePlayers.contains(p.getUniqueId().toString()))
                            BarAPI.setMessage(p, plugin.convertString(bossBarVanishMessage, p),
                                    100f);
                    }
                }, 100);
    }

    private void removeInvisibility(Player p) {
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
        plugin.playerData.set("PlayerData.Player." + p.getUniqueId().toString() + ".remInvis",
                null);
        plugin.savePlayerData();
    }
}