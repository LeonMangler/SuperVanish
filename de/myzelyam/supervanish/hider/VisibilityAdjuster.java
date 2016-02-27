package de.myzelyam.supervanish.hider;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.config.MessagesFile;
import de.myzelyam.supervanish.hider.TabManager.SVTabAction;
import de.myzelyam.supervanish.hooks.DynmapHook;
import de.myzelyam.supervanish.hooks.EssentialsHook;
import de.myzelyam.supervanish.utils.OneDotEightUtils;
import me.MyzelYam.SuperVanish.api.PlayerHideEvent;
import me.MyzelYam.SuperVanish.api.PlayerShowEvent;
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

@SuppressWarnings("deprecation")
public class VisibilityAdjuster extends PlayerHider {

    private static VisibilityAdjuster instance;

    public static VisibilityAdjuster getInstance() {
        if (instance == null)
            instance = new VisibilityAdjuster();
        return instance;
    }

    @Override
    public void hidePlayer(Player p) {
        try {
            if (p == null)
                throw new NullPointerException("The player cannot be null!");
            MessagesFile messagesCfg = new MessagesFile();
            FileConfiguration messages = messagesCfg.getConfig();
            String bossBar = messages
                    .getString("Messages.BossBarVanishMessage");
            String vanishBroadcast = messages.getString("Messages.VanishMessage");
            String vanishBroadcastWithPermission = messages
                    .getString("Messages.VanishMessageWithPermission");
            String onVanishMessage = messages.getString("Messages.OnVanish");
            if (getInvisiblePlayers().contains(p.getUniqueId().toString())) {
                Bukkit.getLogger().log(Level.WARNING, "[SuperVanish] Error: Could not hide player "
                        + p.getName() + ", he is already invisible!");
                return;
            }
            // /////////////////////////////////////////////////////////
            PlayerHideEvent event = new PlayerHideEvent(p);
            de.myzelyam.api.vanish.PlayerHideEvent deprecatedEvent = new de.myzelyam.api.vanish.PlayerHideEvent(
                    p);
            plugin.getServer().getPluginManager().callEvent(event);
            plugin.getServer().getPluginManager().callEvent(deprecatedEvent);
            if (event.isCancelled() || deprecatedEvent.isCancelled()) {
                return;
            }
            // /////
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
            if (plugin.getServer().getPluginManager()
                    .getPlugin("BarAPI") != null
                    && settings.getBoolean("Configuration.Messages.UseBarAPI")) {
                BarAPI.setMessage(p, plugin.convertString(bossBar, p), 100f);
            }
            if (plugin.getServer().getPluginManager()
                    .getPlugin("Essentials") != null
                    && settings.getBoolean(
                    "Configuration.Hooks.EnableEssentialsHook")) {
                EssentialsHook.hidePlayer(p);
            }
            if (settings.getBoolean("Configuration.Players.Fly.Enable")) {
                p.setAllowFlight(true);
            }
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
                ActionBarManager.getInstance(plugin).addActionBar(p);
            }
            // vanish msg
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
            // tab
            if (settings.getBoolean("Configuration.Tablist.ChangeTabNames")) {
                TabManager.getInstance().adjustTabname(p,
                        SVTabAction.SET_CUSTOM_TABNAME);
            }
            p.sendMessage(convertString(onVanishMessage, p));
            List<String> vpl = getInvisiblePlayers();
            vpl.add(p.getUniqueId().toString());
            plugin.playerData.set("InvisiblePlayers", vpl);
            plugin.savePlayerData();
            // ghost team
            if (settings.getBoolean("Configuration.Players.EnableGhostPlayers")
                    && plugin.ghostTeam != null) {
                if (!plugin.ghostTeam.hasPlayer(p)) {
                    if (p.hasPermission("sv.see") || p.hasPermission("sv.use")
                            || getInvisiblePlayers()
                            .contains(p.getUniqueId().toString()))
                        plugin.ghostTeam.addPlayer(p);
                }
                // invis
                p.addPotionEffect(new PotionEffect(
                        PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
            }
            // night vision
            if (settings.getBoolean("Configuration.Players.AddNightVision"))
                p.addPotionEffect(new PotionEffect(
                        PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
            // hide
            hideToAll(p);
        } catch (Exception e) {
            plugin.printException(e);
        }
    }

    @Override
    public void showPlayer(Player p) {
        showPlayer(p, false);
    }

    @Override
    public void showPlayer(Player p, boolean hideJoinMsg) {
        try {
            // check
            if (p == null)
                throw new NullPointerException("The player cannot be null!");
            // preparations
            final Player fp = p;
            MessagesFile messagesCfg = new MessagesFile();
            FileConfiguration messages = messagesCfg.getConfig();
            String bossbar = messages
                    .getString("Messages.BossBarReappearMessage");
            String reappearBroadcast = messages
                    .getString("Messages.ReappearMessage");
            String reappearBroadcastWithPermission = messages
                    .getString("Messages.ReappearMessageWithPermission");
            String onReappearMessage = messages
                    .getString("Messages.OnReappear");
            if (!getInvisiblePlayers().contains(p.getUniqueId().toString())) {
                Bukkit.getLogger().log(Level.WARNING, "[SuperVanish] Error: Could not show player "
                        + p.getName() + ", he is already visible!");
                return;
            }
            // event
            PlayerShowEvent event = new PlayerShowEvent(p);
            de.myzelyam.api.vanish.PlayerShowEvent deprecatedEvent = new de.myzelyam.api.vanish.PlayerShowEvent(
                    p);
            plugin.getServer().getPluginManager().callEvent(event);
            plugin.getServer().getPluginManager().callEvent(deprecatedEvent);
            if (event.isCancelled() || deprecatedEvent.isCancelled()) {
                return;
            }
            // ghost
            if (settings.getBoolean("Configuration.Players.EnableGhostPlayers")
                    && p.hasPotionEffect(PotionEffectType.INVISIBILITY))
                p.removePotionEffect(PotionEffectType.INVISIBILITY);
            // bar-api
            if (plugin.getServer().getPluginManager()
                    .getPlugin("BarAPI") != null
                    && settings.getBoolean("Configuration.Messages.UseBarAPI")) {
                BarAPI.setMessage(p, plugin.convertString(bossbar, p), 100f);
                BarAPI.removeBar(fp);
                Bukkit.getServer().getScheduler()
                        .scheduleSyncDelayedTask(plugin, new Runnable() {

                            @Override
                            public void run() {
                                BarAPI.removeBar(fp);
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
            // essentials
            if (plugin.getServer().getPluginManager()
                    .getPlugin("Essentials") != null
                    && settings.getBoolean(
                    "Configuration.Hooks.EnableEssentialsHook")) {
                EssentialsHook.showPlayer(p);
            }
            // dynmap
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
                ActionBarManager.getInstance(plugin).removeActionBar(p);
            }
            // join msg
            if (settings.getBoolean(
                    "Configuration.Messages.VanishReappearMessages.BroadcastMessageOnReappear")
                    && !hideJoinMsg) {
                for (Player ap : Bukkit.getOnlinePlayers()) {
                    if (!(ap.hasPermission("sv.see") && settings.getBoolean(
                            "Configuration.Players.EnableSeePermission"))) {
                        if (!settings.getBoolean(
                                "Configuration.Messages.VanishReappearMessages.SendMessageOnlyToAdmins")) {
                            ap.sendMessage(plugin.convertString(reappearBroadcast, p));
                        }
                    } else {
                        if (!settings.getBoolean(
                                "Configuration.Messages.VanishReappearMessages.SendMessageOnlyToUsers")) {
                            if (!settings.getBoolean(
                                    "Configuration.Messages.VanishReappearMessages.SendDifferentMessages")) {
                                ap.sendMessage(plugin.convertString(reappearBroadcast, p));
                            } else {
                                if (ap.getUniqueId().toString()
                                        .equals(p.getUniqueId().toString()))
                                    ap.sendMessage(
                                            plugin.convertString(reappearBroadcast, p));
                                else
                                    ap.sendMessage(
                                            plugin.convertString(reappearBroadcastWithPermission, p));
                            }
                        }
                    }
                }
            }
            // tab
            if (settings.getBoolean("Configuration.Tablist.ChangeTabNames")) {
                TabManager.getInstance().adjustTabname(p,
                        SVTabAction.RESTORE_NORMAL_TABNAME);
            }
            // chat msg
            p.sendMessage(plugin.convertString(onReappearMessage, p));
            // lists
            List<String> vpl = getInvisiblePlayers();
            vpl.remove(p.getUniqueId().toString());
            plugin.playerData.set("InvisiblePlayers", vpl);
            plugin.savePlayerData();
            // night vision
            if (settings.getBoolean("Configuration.Players.AddNightVision"))
                p.removePotionEffect(PotionEffectType.NIGHT_VISION);
            // show
            showToAll(p);
        } catch (Exception e) {
            plugin.printException(e);
        }
    }
}