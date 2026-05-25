/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish;

import de.myzelyam.api.vanish.VanishAPI;
import de.myzelyam.supervanish.commands.VanishCommand;
import de.myzelyam.supervanish.config.ConfigMgr;
import de.myzelyam.supervanish.listeners.*;
import de.myzelyam.supervanish.features.FeatureMgr;
import de.myzelyam.supervanish.hooks.PluginHookMgr;
import de.myzelyam.supervanish.net.UpdateNotifier;
import de.myzelyam.supervanish.scheduler.SchedulerFactory;
import de.myzelyam.supervanish.scheduler.TaskScheduler;
import de.myzelyam.supervanish.utils.ExceptionLogger;
import de.myzelyam.supervanish.utils.VersionUtil;
import de.myzelyam.supervanish.visibility.ActionBarMgr;
import de.myzelyam.supervanish.visibility.FileVanishStateMgr;
import de.myzelyam.supervanish.visibility.PaperServerPingListener;
import de.myzelyam.supervanish.visibility.ServerListPacketListener;
import de.myzelyam.supervanish.visibility.VisibilityChanger;
import de.myzelyam.supervanish.visibility.hiders.PreventionHider;
import de.myzelyam.supervanish.visibility.nms.PacketListener;
import de.myzelyam.supervanish.visibility.nms.ServerListPacketHandler;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import ca.spottedleaf.concurrentutil.map.SWMRHashTable;
import java.util.logging.Level;

public class SuperVanish extends JavaPlugin implements SuperVanishPlugin {

    // Versions that don't require config updates (consolidated for memory efficiency)
    public static final String[] NON_REQUIRED_SETTINGS_UPDATES = {
            "6.0.0", "6.0.1", "6.0.2", "6.0.3", "6.0.4", "6.0.5",
            "6.1.0", "6.1.1", "6.1.2", "6.1.3", "6.1.4", "6.1.5", "6.1.6", "6.1.7", "6.1.8",
            "6.2.0", "6.2.1", "6.2.2", "6.2.3", "6.2.4", "6.2.5", "6.2.6", "6.2.7", "6.2.8",
            "6.2.9", "6.2.10", "6.2.11", "6.2.12", "6.2.13", "6.2.14", "6.2.15", "6.2.16",
            "6.2.17", "6.2.18", "6.2.19", "6.2.20", "6.2.21"
    };
    public static final String[] NON_REQUIRED_MESSAGES_UPDATES = {
            "6.0.0", "6.0.1", "6.0.2", "6.0.3", "6.0.4", "6.0.5",
            "6.1.0", "6.1.1", "6.1.2", "6.1.3", "6.1.4", "6.1.5", "6.1.6", "6.1.7", "6.1.8",
            "6.2.0", "6.2.1", "6.2.2", "6.2.3", "6.2.4", "6.2.5", "6.2.6", "6.2.7", "6.2.8",
            "6.2.9", "6.2.10", "6.2.11", "6.2.12", "6.2.13", "6.2.14", "6.2.15", "6.2.16",
            "6.2.17", "6.2.18", "6.2.19", "6.2.20", "6.2.21"
    };

    @Getter
    private ActionBarMgr actionBarMgr;
    @Getter
    private FileVanishStateMgr vanishStateMgr;
    @Getter
    private VersionUtil versionUtil;
    @Getter
    private ConfigMgr configMgr;
    @Getter
    private FeatureMgr featureMgr;
    @Getter
    private PlaceholderConverter placeholderConverter;
    @Getter
    private VanishCommand command;
    @Getter
    private VisibilityChanger visibilityChanger;
    @Getter
    private UpdateNotifier updateNotifier;
    @Getter
    private LoginListener loginListener;
    @Getter
    private LayeredPermissionChecker layeredPermissionChecker;
    @Getter
    private PluginHookMgr pluginHookMgr;
    @Getter
    private PacketListener packetListener;
    private final Map<UUID, VanishPlayer> vanishPlayers = new SWMRHashTable<>();
    private static TaskScheduler scheduler;

    @Override
    public void onEnable() {
        try {
            // Initialize bStats Metrics
            int pluginId = 29495;
            Metrics metrics = new Metrics(this, pluginId);

            scheduler = SchedulerFactory.create(this);
            configMgr = new ConfigMgr(this);
            configMgr.prepareFiles();
            placeholderConverter = new PlaceholderConverter(this);
            layeredPermissionChecker = new LayeredPermissionChecker(this);
            command = new VanishCommand(this);
            versionUtil = new VersionUtil(this);
            vanishStateMgr = new FileVanishStateMgr(this);
            if (getSettings().getBoolean("MiscellaneousOptions.UpdateChecker.Enable", true))
                updateNotifier = new UpdateNotifier(this);
            visibilityChanger = new VisibilityChanger(new PreventionHider(this), this);
            if (versionUtil.isOneDotXOrHigher(8))
                actionBarMgr = new ActionBarMgr(this);
            if (ServerListPacketListener.isEnabled(this))
                ServerListPacketListener.register(this);
            if (ServerListPacketHandler.isEnabled(this))
                getServer().getPluginManager().registerEvents(new ServerListPacketHandler(this), this);
            packetListener = new PacketListener(this);
            for (Player online : Bukkit.getOnlinePlayers()) {
                packetListener.injectPlayer(online);
            }
            registerEvents();
            pluginHookMgr = new PluginHookMgr(this);
            featureMgr = new FeatureMgr(this);
            featureMgr.enableFeatures();
            if (!Bukkit.getOnlinePlayers().isEmpty())
                onReload();
        } catch (Exception e) {
            logException(e);
        }
        try {
            VanishAPI.setPlugin(this);
        } catch (NoSuchMethodError ignored) {
        }
    }

    public static TaskScheduler getScheduler() {
      return scheduler;
    }

    @Override
    public void onDisable() {
        try {
            if (scheduler != null) {
                scheduler.cancelAllTasks();
            }
            if (actionBarMgr != null) {
                actionBarMgr.shutdown();
            }
            if (packetListener != null) {
                packetListener.uninjectAll();
            }
            if (featureMgr != null) featureMgr.disableFeatures();
            if (visibilityChanger != null && visibilityChanger.getHider() != null) {
                visibilityChanger.getHider().clearAll();
            }
            vanishPlayers.clear();
            VanishAPI.setPlugin(null);
        } catch (Throwable e) {
            if (e instanceof ThreadDeath || e instanceof VirtualMachineError) throw e;
            if (!(e instanceof NoClassDefFoundError | e instanceof NoSuchMethodError)) {
                e.printStackTrace();
            }
        }
    }

    private void onReload() {
        final java.util.List<Player> players = new java.util.ArrayList<>(Bukkit.getOnlinePlayers());
        for (Player player : players) {
            boolean itemPickUps = getDefaultItemPickups(player);
            boolean vanished = vanishStateMgr.isVanished(player.getUniqueId());
            createVanishPlayer(player, itemPickUps);
            if (vanished) {
                for (Player onlinePlayer : players)
                    if (!hasPermissionToSee(onlinePlayer, player))
                        visibilityChanger.getHider().setHidden(player, onlinePlayer, true);
            }
            if (getSettings().getBoolean("MessageOptions.DisplayActionBar")
                    && vanished && actionBarMgr != null) {
                actionBarMgr.addActionBar(player);
            }
        }
    }

    public void reload() {
        scheduler.cancelAllTasks();
        HandlerList.unregisterAll(this);
        onDisable();
        onEnable();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return this.command.tabComplete(command, sender, alias, args);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.command.execute(command, sender, label, args);
        return true;
    }

    private void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new GeneralListener(this), this);
        pluginManager.registerEvents(new PlayerBlockModifyListener(this), this);
        pluginManager.registerEvents(new WorldChangeListener(this), this);
        if (versionUtil.isOneDotXOrHigher(10)) {
            pluginManager.registerEvents(new TabCompleteListener(this), this);
        }
        pluginManager.registerEvents(loginListener = new LoginListener(this), this);
        JoinListener joinListener = new JoinListener(this);
        pluginManager.registerEvent(PlayerJoinEvent.class, joinListener,
                getEventPriority(PlayerJoinEvent.class), joinListener, this, false);
        QuitListener quitListener = new QuitListener(this);
        pluginManager.registerEvent(PlayerQuitEvent.class, quitListener,
                getEventPriority(PlayerQuitEvent.class), quitListener, this, false);
    }

    private EventPriority getEventPriority(Class<? extends Event> eventClass) {
        try {
            String eventName = eventClass.getSimpleName();
            String configString = getSettings().getString("CompatibilityOptions." + eventName + "Priority");
            if (configString == null) return EventPriority.NORMAL;
            EventPriority priority = EventPriority.valueOf(configString);
            return priority;
        } catch (Exception e) {
            logException(e);
            return EventPriority.NORMAL;
        }
    }

    public String replacePlaceholders(String msg, Object... additionalPlayerInfo) {
        return placeholderConverter.replacePlaceholders(msg, additionalPlayerInfo);
    }

    public String getMessage(String path) {
        String message = getMessages().getString("Messages." + path);
        if (message == null) {
            message = configMgr.getMessagesFile().getDefaultConfig().getString("Messages." + path);
        }
        return message;
    }

    public VanishPlayer getVanishPlayer(Player player) {
        UUID id = player.getUniqueId();
        VanishPlayer vp = vanishPlayers.get(id);
        if (vp == null) {
            vp = new VanishPlayer(player, this, getDefaultItemPickups(player));
            vanishPlayers.put(id, vp);
        }
        return vp;
    }

    public VanishPlayer createVanishPlayer(Player player, boolean itemPickUps) {
        UUID id = player.getUniqueId();
        VanishPlayer vp = new VanishPlayer(player, this, itemPickUps);
        vanishPlayers.put(id, vp);
        return vp;
    }

    private boolean getDefaultItemPickups(Player player) {
        return getPlayerData().getBoolean(
                "PlayerData." + player.getUniqueId() + ".itemPickUps",
                getSettings().getBoolean("InvisibilityFeatures.DefaultPickUpItemsOption"));
    }

    public void removeVanishPlayer(VanishPlayer vanishPlayer) {
        if (vanishPlayer != null) vanishPlayers.remove(vanishPlayer.getPlayerUUID());
    }

    public void sendMessage(CommandSender p, String messagesYmlPath, Object... additionalPlayerInfo) {
        String message;
        if (!messagesYmlPath.contains(" ") && getMessage(messagesYmlPath) != null)
            message = getMessage(messagesYmlPath);
        else message = messagesYmlPath;
        if ("".equalsIgnoreCase(message) || "".equalsIgnoreCase(messagesYmlPath))
            return;
        message = replacePlaceholders(message, additionalPlayerInfo);
        p.sendMessage(message);
    }

    public boolean canSee(Player viewer, Player viewed) {
        return !visibilityChanger.getHider().isHidden(viewed, viewer);
    }

    public boolean hasPermissionToVanish(CommandSender sender) {
        return layeredPermissionChecker.hasPermissionToVanish(sender);
    }

    public boolean hasPermissionToSee(Player viewer, Player viewed) {
        return layeredPermissionChecker.hasPermissionToSee(viewer, viewed);
    }

    public int getLayeredPermissionLevel(CommandSender sender, String permission) {
        return layeredPermissionChecker.getLayeredPermissionLevel(sender, permission);
    }

    public void refreshCachedPermissionLevels(Player player) {
        try {
            if (player == null) return;
            VanishPlayer vp = getVanishPlayer(player);
            if (vp != null) vp.refreshPermissionLevels(player);
        } catch (Exception e) {
            logException(e);
        }
    }

    public void refreshCachedPermissionLevelsAll() {
        for (Player p : getServer().getOnlinePlayers()) {
            refreshCachedPermissionLevels(p);
        }
    }

    public FileConfiguration getSettings() {
        return configMgr.getSettings();
    }

    public FileConfiguration getMessages() {
        return configMgr.getMessages();
    }

    public FileConfiguration getPlayerData() {
        return configMgr.getPlayerData();
    }

    @Override
    public FileConfiguration getConfig() {
        return getSettings();
    }

    @Override
    public void log(Level level, String msg) {
        getLogger().log(level, msg);
    }

    @Override
    public void log(Level level, String msg, Throwable ex) {
        getLogger().log(level, msg, ex);
    }

    @Override
    public void logException(Throwable e) {
        ExceptionLogger.logException(e, this);
    }

    public void setVanishMetadata(Player player, boolean vanished) {
        if (vanished) {
            player.setMetadata("vanished", new FixedMetadataValue(this, true));
        } else {
            player.removeMetadata("vanished", this);
        }
    }
}