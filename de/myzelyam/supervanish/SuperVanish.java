package de.myzelyam.supervanish;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import de.myzelyam.supervanish.cmd.CmdManager;
import de.myzelyam.supervanish.config.MessagesFile;
import de.myzelyam.supervanish.config.SettingsFile;
import de.myzelyam.supervanish.events.JoinEvent;
import de.myzelyam.supervanish.events.PlayerControl;
import de.myzelyam.supervanish.events.QuitEvent;
import de.myzelyam.supervanish.events.WorldChangeEvent;
import de.myzelyam.supervanish.hider.ActionBarManager;
import de.myzelyam.supervanish.hider.ServerlistAdjustments;
import de.myzelyam.supervanish.hider.SilentChestListeners;
import de.myzelyam.supervanish.hooks.DisguiseCraftHook;
import de.myzelyam.supervanish.hooks.LibsDisguisesHook;
import de.myzelyam.supervanish.hooks.SuperTrailsHook;
import de.myzelyam.supervanish.hooks.TrailGUIHook;
import me.confuser.barapi.BarAPI;
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
import org.bukkit.scoreboard.Team;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

public class SuperVanish extends JavaPlugin {

    public static final boolean SERVER_IS_ONE_DOT_SEVEN = Bukkit.getVersion()
            .contains("(MC: 1.7");

    private final List<String> nonRequiredConfigUpdates = Collections.singletonList("5.4.4-5.4.5");
    private final List<String> nonRequiredMsgUpdates = Arrays.asList(
            "5.3.1-5.4.5", "5.3.2-5.4.5", "5.3.3-5.4.5", "5.3.4-5.4.5",
            "5.3.5-5.4.5", "5.4.0-5.4.5", "5.4.1-5.4.5", "5.4.2-5.4.5",
            "5.4.3-5.4.5", "5.4.4-5.4.5");
    public boolean requiresCfgUpdate = false;
    public boolean requiresMsgUpdate = false;

    public Team ghostTeam;

    public MessagesFile messagesFile;
    public FileConfiguration messages;

    public SettingsFile settingsFile;
    public FileConfiguration settings;

    public File playerDataFile = new File(
            this.getDataFolder().getPath() + File.separator + "playerdata.yml");
    public FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerDataFile);


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
            checkForReload();
            checkGhostPlayers();
            if (getServer().getPluginManager()
                    .getPlugin("ProtocolLib") != null) {
                ServerlistAdjustments.setupProtocolLib();
                if (settings.getBoolean("Configuration.Players.SilentOpenChest")) {
                    SilentChestListeners.setupAnimationListener();
                    SilentChestListeners.setupSoundListener();
                }
            }
        } catch (Exception e) {
            printException(e);
        }
    }

    @Override
    public void onDisable() {
        SVUtils.settings = null;
        SVUtils.playerData = null;
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

    @SuppressWarnings("deprecation")
    private void checkGhostPlayers() {
        try {
            if (settings.getBoolean("Configuration.Players.EnableGhostPlayers")) {
                List<String> invisiblePlayers = playerData.getStringList("InvisiblePlayers");
                ghostTeam = Bukkit.getServer().getScoreboardManager()
                        .getMainScoreboard().getTeam("SuperVanishGT");
                if (ghostTeam == null) {
                    ghostTeam = Bukkit.getServer().getScoreboardManager()
                            .getMainScoreboard()
                            .registerNewTeam("SuperVanishGT");
                }
                ghostTeam.setCanSeeFriendlyInvisibles(true);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!ghostTeam.hasPlayer(p)) {
                        if (p.hasPermission("sv.see")
                                || p.hasPermission("sv.use")
                                || invisiblePlayers.contains(p.getUniqueId().toString()))
                            ghostTeam.addPlayer(p);
                    } else {
                        if (!(p.hasPermission("sv.see")
                                || p.hasPermission("sv.use")
                                || invisiblePlayers.contains(p.getUniqueId().toString()))) {
                            ghostTeam.removePlayer(p);
                        }
                    }
                }
            }
        } catch (Exception e) {
            printException(e);
        }
    }

    private void checkForReload() {
        try {
            List<String> invisiblePlayers = playerData.getStringList("InvisiblePlayers");
            // boss bars
            if (getServer().getPluginManager().getPlugin("BarAPI") != null
                    && settings.getBoolean("Configuration.Messages.UseBarAPI")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (invisiblePlayers.contains(p.getUniqueId().toString())) {
                        String onVanish = messages.getString("Messages.OnVanish");
                        BarAPI.setMessage(p, convertString(onVanish, p), 100f);
                    }
                }
            }
            // action bars
            if (getServer().getPluginManager().getPlugin("ProtocolLib") != null
                    && settings.getBoolean(
                    "Configuration.Messages.DisplayActionBarsToInvisiblePlayers")
                    && !SuperVanish.SERVER_IS_ONE_DOT_SEVEN) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (invisiblePlayers.contains(p.getUniqueId().toString())) {
                        ActionBarManager.getInstance(this).addActionBar(p);
                    }
                }
            }
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
            EventPriority priority = EventPriority.valueOf(configSetting);
            return priority == null ? EventPriority.NORMAL : priority;
        } catch (Exception e) {
            printException(e);
            return EventPriority.NORMAL;
        }
    }

    private void registerEvents() {
        try {
            PluginManager pluginManager = this.getServer().getPluginManager();
            // general
            pluginManager.registerEvents(new PlayerControl(), this);
            pluginManager.registerEvents(WorldChangeEvent.getInstance(), this);
            // hooks
            if (pluginManager.isPluginEnabled("LibsDisguises") && settings
                    .getBoolean("Configuration.Hooks.EnableLibsDisguisesHook"))
                pluginManager.registerEvents(new LibsDisguisesHook(), this);
            if (pluginManager.isPluginEnabled("DisguiseCraft") && settings
                    .getBoolean("Configuration.Hooks.EnableDisguiseCraftHook"))
                pluginManager.registerEvents(new DisguiseCraftHook(), this);
            if (pluginManager.isPluginEnabled("TrailGUI") && settings
                    .getBoolean("Configuration.Hooks.EnableTrailGUIHook", true))
                TrailGUIHook.replaceMoveListener();
            if (pluginManager.isPluginEnabled("SuperTrails") && settings.getBoolean(
                    "Configuration.Hooks.EnableSuperTrailsHook", true))
                new SuperTrailsHook(this);
            // join
            JoinEvent joinEvent = new JoinEvent();
            pluginManager.registerEvent(PlayerJoinEvent.class, joinEvent,
                    getEventPriority(PlayerJoinEvent.class), joinEvent, this,
                    false);
            // quit
            QuitEvent quitEvent = new QuitEvent();
            pluginManager.registerEvent(PlayerQuitEvent.class, quitEvent,
                    getEventPriority(PlayerQuitEvent.class), quitEvent, this,
                    false);
        } catch (Exception e) {
            printException(e);
        }
    }

    public void printException(Exception e) {
        Logger logger = Bukkit.getLogger();
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
                    "[SuperVanish] An exception occurred while trying to print a detailed stacktrace, printing an undetailed stacktrace of both exceptions:");
            logger.log(SEVERE, "ORIGINAL EXCEPTION:");
            e.printStackTrace();
            logger.log(SEVERE, "SECOND EXCEPTION:");
            e2.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel,
                             String[] args) {
        new CmdManager(cmd, sender, args, cmdLabel);
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
            // check if same
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
        for (String updatesString : (checkCfg ? nonRequiredConfigUpdates
                : nonRequiredMsgUpdates)) {
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

    public String getMsg(String msg) {
        String rn = messages.getString("Messages." + msg);
        if (rn == null)
            rn = "SV: Unavailable message in messages.yml: " + msg;
        return rn;
    }

    // override the standard config-api
    @Override
    public FileConfiguration getConfig() {
        return settings;
    }

    @Override
    public void saveDefaultConfig() {
        settingsFile.saveDefaultConfig();
    }
}