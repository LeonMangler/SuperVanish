/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import de.myzelyam.api.vanish.VanishAPI;
import de.myzelyam.supervanish.cmd.CommandMgr;
import de.myzelyam.supervanish.config.MessagesFile;
import de.myzelyam.supervanish.config.SettingsFile;
import de.myzelyam.supervanish.events.GeneralEventListener;
import de.myzelyam.supervanish.events.JoinEvent;
import de.myzelyam.supervanish.events.QuitEvent;
import de.myzelyam.supervanish.events.WorldChangeEvent;
import de.myzelyam.supervanish.hooks.*;
import de.myzelyam.supervanish.utils.ProtocolLibPacketUtils;
import de.myzelyam.supervanish.visibility.*;
import me.MyzelYam.SuperVanish.api.SVAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

public class SuperVanish extends JavaPlugin {

    private static final List<String> NON_REQUIRED_SETTINGS_UPDATES = Collections.singletonList("5.8.2-5.8.3");
    private static final List<String> NON_REQUIRED_MESSAGES_UPDATES = Collections.singletonList("5.8.2-5.8.3");
    public boolean requiresCfgUpdate = false;
    public boolean requiresMsgUpdate = false;
    public boolean packetNightVision = false;

    public MessagesFile messagesFile;
    public FileConfiguration messages;

    public SettingsFile settingsFile;
    public FileConfiguration settings;

    private File playerDataFile = new File(
            this.getDataFolder().getPath() + File.separator + "playerdata.yml");
    public FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerDataFile);

    private VisibilityAdjuster visibilityAdjuster;
    private ActionBarMgr actionBarMgr;
    private TeamMgr teamMgr;
    private ProtocolLibPacketUtils protocolLibPacketUtils;


    public void savePlayerData() {
        try {
            playerData.save(playerDataFile);
        } catch (IOException e) {
            printException(e);
        }
    }

    @Override
    public void onEnable() {
        try {
            prepareConfig();
            registerEvents();
            visibilityAdjuster = new VisibilityAdjuster(this);
            VanishAPI.setPlugin(this);
            //noinspection deprecation
            SVAPI.setPlugin(this);
            teamMgr = new TeamMgr(this);
            if (getServer().getPluginManager()
                    .getPlugin("ProtocolLib") != null) {
                protocolLibPacketUtils = new ProtocolLibPacketUtils(this);
                packetNightVision = true;
                if (isOneDotXOrHigher(8))
                    actionBarMgr = new ActionBarMgr(this);
                new ServerListPacketListener(this).registerListener();
                if (settings.getBoolean("Configuration.Players.SilentOpenChest")
                        && isOneDotXOrHigher(8)) {
                    new SilentChestListeners_v3(this);
                }
            }
            new ForcedInvisibilityTask(this).start();
            checkForReload();
        } catch (Exception e) {
            printException(e);
        }
    }

    @Override
    public void onDisable() {
        VanishAPI.setPlugin(null);
        //noinspection deprecation
        SVAPI.setPlugin(null);
    }

    private void prepareConfig() {
        try {
            // messages
            messagesFile = new MessagesFile();
            messagesFile.saveDefaultConfig();
            this.messages = messagesFile.getConfig();
            // config
            settingsFile = new SettingsFile();
            settingsFile.saveDefaultConfig();
            this.settings = settingsFile.getConfig();
            // player data
            playerData.options().header("SuperVanish v" + getDescription().getVersion()
                    + " - Player data");
            playerData.options().copyHeader(true);
            savePlayerData();
            // check for updates
            checkConfig();
        } catch (Exception e) {
            printException(e);
        }
    }

    private void checkForReload() {
        try {
            Collection<Player> invisiblePlayers = getOnlineInvisiblePlayers();
            // action bars
            if (getServer().getPluginManager().getPlugin("ProtocolLib") != null
                    && settings.getBoolean(
                    "Configuration.Messages.DisplayActionBarsToInvisiblePlayers")
                    && !isOneDotX(7) && actionBarMgr != null) {
                for (Player p : invisiblePlayers) {
                    actionBarMgr.addActionBar(p);
                }
            }
            // teams
            teamMgr.onReload();
        } catch (Exception e) {
            printException(e);
        }
    }

    private EventPriority getEventPriority(Class<? extends Event> clazz) {
        try {
            String eventName = clazz.getSimpleName();
            String configSetting = settings.getString("Configuration.CompatibilityOptions."
                    + eventName + "Priority");
            if (configSetting == null)
                return EventPriority.NORMAL;
            return EventPriority.valueOf(configSetting);
        } catch (Exception e) {
            printException(e);
            return EventPriority.NORMAL;
        }
    }

    private void registerEvents() {
        try {
            PluginManager pluginManager = this.getServer().getPluginManager();
            // general
            pluginManager.registerEvents(new GeneralEventListener(this), this);
            // world change event
            pluginManager.registerEvents(new WorldChangeEvent(this), this);
            // plugin hooks
            String currentHook = "Unknown";
            try {
                if (pluginManager.isPluginEnabled("LibsDisguises") && settings
                        .getBoolean("Configuration.Hooks.EnableLibsDisguisesHook")) {
                    currentHook = "LibsDisguises";
                    pluginManager.registerEvents(new LibsDisguisesHook(), this);
                }
                if (pluginManager.isPluginEnabled("DisguiseCraft") && settings
                        .getBoolean("Configuration.Hooks.EnableDisguiseCraftHook")) {
                    currentHook = "DisguiseCraft";
                    pluginManager.registerEvents(new DisguiseCraftHook(), this);
                }
                if (pluginManager.isPluginEnabled("TrailGUI") && settings
                        .getBoolean("Configuration.Hooks.EnableTrailGUIHook", true)) {
                    currentHook = "TrailGUI";
                    TrailGUIHook.replaceMoveListener();
                }
                if (pluginManager.isPluginEnabled("SuperTrails") && settings.getBoolean(
                        "Configuration.Hooks.EnableSuperTrailsHook", true)) {
                    currentHook = "SuperTrails";
                    new SuperTrailsHook(this);
                }
                if (pluginManager.isPluginEnabled("Citizens") && settings.getBoolean(
                        "Configuration.Hooks.EnableCitizensHook", true)) {
                    currentHook = "Citizens";
                    new CitizensHook(this);
                }
                if (pluginManager.isPluginEnabled("EnjinMinecraftPlugin") && settings.getBoolean(
                        "Configuration.Hooks.EnableEnjinMinecraftPluginHook", true)) {
                    currentHook = "EnjinMinecraftPlugin";
                    new EnjinMinecraftPluginHook(this);
                }
            } catch (Throwable throwable) {
                if (throwable instanceof ThreadDeath || throwable instanceof VirtualMachineError) throw throwable;
                getLogger().log(Level.WARNING, "[SuperVanish] Failed to hook into " + currentHook
                        + ", please report this if you are using the latest version of that plugin: "
                        + throwable.getMessage());
                // just continue normally, don't let another plugin break SV!
            }
            // join event
            JoinEvent joinEvent = new JoinEvent(this);
            pluginManager.registerEvent(PlayerJoinEvent.class, joinEvent,
                    getEventPriority(PlayerJoinEvent.class), joinEvent, this,
                    false);
            // quit event
            QuitEvent quitEvent = new QuitEvent(this);
            pluginManager.registerEvent(PlayerQuitEvent.class, quitEvent,
                    getEventPriority(PlayerQuitEvent.class), quitEvent, this,
                    false);
        } catch (Exception e) {
            printException(e);
        }
    }

    public void printException(Exception e) {
        Logger logger = getLogger();
        try {
            logger.log(SEVERE, "[SuperVanish] Unknown Exception occurred!");
            if (requiresCfgUpdate || requiresMsgUpdate) {
                logger.log(SEVERE,
                        "[SuperVanish] You have an outdated configuration,");
                logger.log(SEVERE,
                        "[SuperVanish] regenerating it by using '/sv updatecfg' might fix this problem.");
            } else
                logger.log(SEVERE, "[SuperVanish] Please report this issue!");
            logger.log(SEVERE, "Message: ");
            logger.log(SEVERE, "  " + e.getMessage());
            logger.log(SEVERE, "General information: ");
            String plugins = "";
            for (Plugin plugin : Bukkit.getServer().getPluginManager()
                    .getPlugins()) {
                if (plugin.getName().equalsIgnoreCase("SuperVanish"))
                    continue;
                plugins = plugins + plugin.getName() + " v"
                        + plugin.getDescription().getVersion() + ", ";
            }
            logger.log(SEVERE, "  ServerVersion: "
                    + getServer().getVersion());
            logger.log(SEVERE, "  PluginVersion: "
                    + getDescription().getVersion());
            logger.log(SEVERE, "  ServerPlugins: " + plugins);
            logger.log(SEVERE, "StackTrace: ");
            e.printStackTrace();
            logger.log(SEVERE, "[SuperVanish] Please include this information");
            logger.log(SEVERE, "[SuperVanish] if you report the issue.");
        } catch (Exception e2) {
            logger.log(SEVERE,
                    "[SuperVanish] An exception occurred while trying to print a detailed stacktrace, "
                            + "printing an undetailed stacktrace of both exceptions:");
            logger.log(SEVERE, "ORIGINAL EXCEPTION:");
            e.printStackTrace();
            logger.log(SEVERE, "SECOND EXCEPTION:");
            e2.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel,
                             String[] args) {
        new CommandMgr(this, cmd, sender, args, cmdLabel);
        return true;
    }

    public void checkConfig() {
        try {
            String currentCfgVersion = settings.getString("ConfigVersion");
            String newestVersion = getDescription().getVersion();
            String currentMsgsVersion = messages.getString("MessagesVersion");
            this.requiresMsgUpdate = requiresUpdate(currentMsgsVersion,
                    newestVersion, false);
            this.requiresCfgUpdate = requiresUpdate(currentCfgVersion,
                    newestVersion, true);
            // check if equal
            if (newestVersion.equals(currentCfgVersion))
                this.requiresCfgUpdate = false;
            if (newestVersion.equals(currentMsgsVersion))
                this.requiresMsgUpdate = false;
        } catch (Exception e) {
            printException(e);
        }
    }

    private boolean requiresUpdate(String currentVersion, String newestVersion,
                                   boolean checkCfg) {
        if (currentVersion == null)
            return true;
        for (String updatesString : (checkCfg ? NON_REQUIRED_SETTINGS_UPDATES
                : NON_REQUIRED_MESSAGES_UPDATES)) {
            String[] splittedUpdatesString = updatesString.split("-");
            if (currentVersion.equalsIgnoreCase(splittedUpdatesString[0])
                    && newestVersion.equalsIgnoreCase(splittedUpdatesString[1]))
                return false;
        }
        return true;
    }

    public String convertString(String msg, Object unspecifiedPlayer) {
        try {
            replaceVariables:
            {
                if (unspecifiedPlayer instanceof OfflinePlayer
                        && !(unspecifiedPlayer instanceof Player)) {
                    // offline player
                    OfflinePlayer specifiedPlayer = (OfflinePlayer) unspecifiedPlayer;
                    // replace PEX prefix and suffix
                    if (getServer().getPluginManager()
                            .getPlugin("PermissionsEx") != null) {
                        msg = msg.replace("%prefix", "").replace("%suffix", "");
                    }
                    // replace essentials nick names
                    if (getServer().getPluginManager()
                            .getPlugin("Essentials") != null) {
                        msg = msg.replace("%nick", specifiedPlayer.getName());
                    }
                    // replace general variables
                    msg = msg.replace("%d", specifiedPlayer.getName())
                            .replace("%p", specifiedPlayer.getName())
                            .replace("%t", specifiedPlayer.getName());
                    break replaceVariables;
                }
                if (unspecifiedPlayer instanceof Player) {
                    // player
                    Player specifiedPlayer = (Player) unspecifiedPlayer;
                    // replace PEX prefix and suffix
                    if (getServer().getPluginManager()
                            .getPlugin("PermissionsEx") != null) {
                        PermissionUser user = PermissionsEx
                                .getUser(specifiedPlayer);
                        if (user != null) {
                            if (user.getPrefix() != null)
                                msg = msg.replace("%prefix", user.getPrefix());
                            if (user.getSuffix() != null)
                                msg = msg.replace("%suffix", user.getSuffix());
                        }
                    }
                    // replace essentials nick names
                    if (getServer().getPluginManager()
                            .getPlugin("Essentials") != null) {
                        Essentials ess = (Essentials) Bukkit.getServer()
                                .getPluginManager().getPlugin("Essentials");
                        User u = ess.getUser(specifiedPlayer);
                        if (u != null)
                            if (u.getNickname() != null)
                                msg = msg.replace("%nick", u.getNickname());
                    }
                    // replace general variables
                    msg = msg
                            .replace("%d",
                                    "" + specifiedPlayer.getDisplayName())
                            .replace("%p", "" + specifiedPlayer.getName())
                            .replace("%t",
                                    "" + specifiedPlayer.getPlayerListName());
                    break replaceVariables;
                }
                if (unspecifiedPlayer instanceof CommandSender) {
                    // console
                    // replace PEX prefixes
                    if (getServer().getPluginManager()
                            .getPlugin("PermissionsEx") != null) {
                        msg = msg.replace("%prefix", "").replace("%suffix", "");
                    }
                    // replace general variables
                    msg = msg.replace("%d", "Console").replace("%p", "Console")
                            .replace("%t", "Console");
                }
            }
            // add color
            msg = ChatColor.translateAlternateColorCodes('&', msg);
            // return replaced message
            return msg;
        } catch (Exception e) {
            printException(e);
            return "SV-Error occurred; more information in console";
        }
    }

    public String getMsg(String path) {
        String message = messages.getString("Messages." + path);
        if (message == null) {
            // get default value if not present
            message = messagesFile.getDefaultConfig().getString("Messages." + path);
        }
        return message;
    }

    public boolean isOneDotX(int majorRelease) {
        String version = getServer().getClass().getPackage().getName()
                .replace(".", ",").split(",")[3];
        return version.contains("v1_" + majorRelease + "_R");
    }

    public boolean isOneDotXOrHigher(int majorRelease) {
        String version = getServer().getClass().getPackage().getName()
                .replace(".", ",").split(",")[3];
        for (int i = majorRelease; i < 20; i++)
            if (version.contains("v1_" + i + "_R")) return true;
        return version.contains("v2_");
    }

    // override the standard Config API
    @Override
    public FileConfiguration getConfig() {
        return settings;
    }

    @Override
    public void saveDefaultConfig() {
        settingsFile.saveDefaultConfig();
    }

    public List<String> getAllInvisiblePlayers() {
        return playerData.getStringList("InvisiblePlayers");
    }

    public Collection<Player> getOnlineInvisiblePlayers() {
        Collection<Player> onlineInvisiblePlayers = new HashSet<>();
        List<String> allInvisiblePlayers = getAllInvisiblePlayers();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (allInvisiblePlayers.contains(player.getUniqueId().toString())) {
                onlineInvisiblePlayers.add(player);
            }
        }
        return onlineInvisiblePlayers;
    }

    public ActionBarMgr getActionBarMgr() {
        return actionBarMgr;
    }

    public VisibilityAdjuster getVisibilityAdjuster() {
        return visibilityAdjuster;
    }

    public ProtocolLibPacketUtils getProtocolLibPacketUtils() {
        return protocolLibPacketUtils;
    }
}