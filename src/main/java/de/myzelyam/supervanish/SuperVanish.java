/*
 * Copyright © 2015, Leon Mangler and the SuperVanish contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish;

import com.comphenix.protocol.ProtocolLibrary;
import de.myzelyam.api.vanish.VanishAPI;
import de.myzelyam.supervanish.commands.VanishCommand;
import de.myzelyam.supervanish.config.ConfigMgr;
import de.myzelyam.supervanish.listeners.*;
import de.myzelyam.supervanish.features.FeatureMgr;
import de.myzelyam.supervanish.hooks.PluginHookMgr;
import de.myzelyam.supervanish.net.UpdateNotifier;
import de.myzelyam.supervanish.utils.ExceptionLogger;
import de.myzelyam.supervanish.utils.VersionUtil;
import de.myzelyam.supervanish.visibility.*;
import de.myzelyam.supervanish.visibility.hiders.PreventionHider;
import lombok.Getter;
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
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class SuperVanish extends JavaPlugin implements SuperVanishPlugin {

    public static final String[] NON_REQUIRED_SETTINGS_UPDATES = {"6.0.0", "6.0.1", "6.0.2", "6.0.3",
            "6.0.4", "6.0.5", "6.1.0", "6.1.1", "6.1.2", "6.1.3", "6.1.4", "6.1.5", "6.1.6", "6.1.7",
            "6.1.8", "6.2.0", "6.2.1", "6.2.2", "6.2.3", "6.2.4", "6.2.5", "6.2.6", "6.2.7", "6.2.8",
            "6.2.9", "6.2.10", "6.2.11", "6.2.12", "6.2.13", "6.2.14", "6.2.15", "6.2.16", "6.2.17",
            "6.2.18", "6.2.19"},
            NON_REQUIRED_MESSAGES_UPDATES = {"6.0.0", "6.0.1", "6.0.2", "6.0.3", "6.0.4", "6.0.5", "6.1.0",
                    "6.1.1", "6.1.2", "6.1.3", "6.1.4", "6.1.5", "6.1.6", "6.1.7", "6.1.8", "6.2.0", "6.2.1",
                    "6.2.2", "6.2.3", "6.2.4", "6.2.5", "6.2.6", "6.2.7", "6.2.8", "6.2.9", "6.2.10", "6.2.11",
                    "6.2.12", "6.2.13", "6.2.14", "6.2.15", "6.2.16", "6.2.17", "6.2.18", "6.2.19"};

    @Getter
    private boolean useProtocolLib;
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
    private Set<VanishPlayer> vanishPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        try {
            useProtocolLib = getServer().getPluginManager().isPluginEnabled("ProtocolLib");
            if (!useProtocolLib) log(Level.INFO,
                    "Please install ProtocolLib to be able to use all SuperVanish features: " +
                            "https://www.spigotmc.org/resources/protocollib.1997/");
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
            if (versionUtil.isOneDotXOrHigher(8) && useProtocolLib)
                actionBarMgr = new ActionBarMgr(this);
            if (useProtocolLib && ServerListPacketListener.isEnabled(this))
                ServerListPacketListener.register(this);
            if (useProtocolLib)
                PlayerSpawnPacketListener.register(this);
            registerEvents();
            new PluginHookMgr(this);
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
            // API already loaded by other plugin
        }
    }

    @Override
    public void onDisable() {
        try {
            if (featureMgr != null) featureMgr.disableFeatures();
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
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean itemPickUps = getPlayerData().getBoolean(
                    "PlayerData." + player.getUniqueId() + ".itemPickUps",
                    getSettings().getBoolean("InvisibilityFeatures.DefaultPickUpItemsOption"));
            boolean vanished = vanishStateMgr.isVanished(player.getUniqueId());
            createVanishPlayer(player, itemPickUps);
            if (vanished) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers())
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
        getServer().getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
        if (useProtocolLib)
            ProtocolLibrary.getProtocolManager().removePacketListeners(this);
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
        for (VanishPlayer vanishPlayer : vanishPlayers) {
            if (vanishPlayer.getPlayerUUID().equals(player.getUniqueId())) {
                return vanishPlayer;
            }
        }
        // ensure that there is always a vanish player
        boolean itemPickUps = getPlayerData().getBoolean(
                "PlayerData." + player.getUniqueId() + ".itemPickUps",
                getSettings().getBoolean("InvisibilityFeatures.DefaultPickUpItemsOption"));
        final VanishPlayer vanishPlayer = new VanishPlayer(player, this, itemPickUps);
        vanishPlayers.add(vanishPlayer);
        return vanishPlayer;
    }

    public void createVanishPlayer(Player player, boolean itemPickUps) {
        VanishPlayer vanishPlayer = new VanishPlayer(player, this, itemPickUps);
        vanishPlayers.add(vanishPlayer);
    }

    public void removeVanishPlayer(VanishPlayer vanishPlayer) {
        vanishPlayers.remove(vanishPlayer);
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
}