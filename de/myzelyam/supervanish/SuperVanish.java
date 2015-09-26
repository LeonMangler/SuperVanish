package de.myzelyam.supervanish;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

import de.myzelyam.supervanish.cmd.CmdManager;
import de.myzelyam.supervanish.config.ConfigCfg;
import de.myzelyam.supervanish.config.MessagesCfg;
import de.myzelyam.supervanish.events.JoinEvent;
import de.myzelyam.supervanish.events.PlayerControl;
import de.myzelyam.supervanish.events.QuitEvent;
import de.myzelyam.supervanish.events.WorldChangeEvent;
import de.myzelyam.supervanish.hider.ActionBarManager;
import de.myzelyam.supervanish.hider.ServerlistAdjustments;
import de.myzelyam.supervanish.hider.SilentChestListeners;
import de.myzelyam.supervanish.hooks.DisguiseCraftHook;
import de.myzelyam.supervanish.hooks.LibsDisguisesHook;

public class SuperVanish extends JavaPlugin {

	private final List<String> nonRequiredConfigUpdates = Arrays.asList();

	private final List<String> nonRequiredMsgsUpdates = Arrays.asList(
			"5.3.1-5.4.2", "5.3.2-5.4.2", "5.3.3-5.4.2", "5.3.4-5.4.2",
			"5.3.5-5.4.2", "5.4.0-5.4.2", "5.4.1-5.4.2");

	public static final boolean SERVER_IS_ONE_DOT_SEVEN = Bukkit.getVersion()
			.contains("(MC: 1.7");

	public boolean requiresCfgUpdate = false;

	public boolean requiresMsgsUpdate = false;

	public Team ghostTeam;

	public FileConfiguration msgs;

	public FileConfiguration cfg;

	public File pdf = new File(this.getDataFolder().getPath() + File.separator
			+ "playerdata.yml");

	public void spd() {
		try {
			pd.save(pdf);
		} catch (IOException e) {
			printException(e);
		}
	}

	public FileConfiguration pd = YamlConfiguration.loadConfiguration(pdf);

	public MessagesCfg mcfg;

	public ConfigCfg ccfg;

	@Override
	public void onEnable() {
		try {
			prepareConfig();
			registerEvents();
			checkForReload();
			checkGhostPlayers();
			if (getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
				ServerlistAdjustments.setupProtocolLib();
				if (cfg.getBoolean("Configuration.Players.SilentOpenChest")) {
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
		SVUtils.cfg = null;
		SVUtils.pd = null;
	}

	private void prepareConfig() {
		try {
			// messages
			mcfg = new MessagesCfg();
			mcfg.saveDefaultConfig();
			this.msgs = mcfg.getConfig();
			// config
			ccfg = new ConfigCfg();
			ccfg.saveDefaultConfig();
			this.cfg = ccfg.getConfig();
			// playerdata
			pd.options().header(
					"SuperVanish v" + getDescription().getVersion()
							+ " - Playerdata");
			pd.options().copyHeader(true);
			spd();
			// check for updates
			checkConfig();
		} catch (Exception e) {
			printException(e);
		}
	}

	private void checkGhostPlayers() {
		try {
			if (cfg.getBoolean("Configuration.Players.EnableGhostPlayers")) {
				List<String> vpl = pd.getStringList("InvisiblePlayers");
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
								|| vpl.contains(p.getUniqueId().toString()))
							ghostTeam.addPlayer(p);
					} else {
						if (p.hasPermission("sv.see")
								|| p.hasPermission("sv.use")
								|| vpl.contains(p.getUniqueId().toString())) {
						} else
							ghostTeam.removePlayer(p);
					}
				}
			}
		} catch (Exception e) {
			printException(e);
		}
	}

	private void checkForReload() {
		try {
			List<String> vpl = pd.getStringList("InvisiblePlayers");
			// boss bars
			if (getServer().getPluginManager().getPlugin("BarAPI") != null
					&& cfg.getBoolean("Configuration.Messages.UseBarAPI")) {
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (vpl.contains(p.getUniqueId().toString())) {
						String onVanish = msgs.getString("Messages.OnVanish");
						BarAPI.setMessage(p, convertString(onVanish, p), 100f);
					}
				}
			}
			// action bars
			if (getServer().getPluginManager().getPlugin("ProtocolLib") != null
					&& cfg.getBoolean("Configuration.Messages.DisplayActionBarsToInvisiblePlayers")
					&& !SuperVanish.SERVER_IS_ONE_DOT_SEVEN) {
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (vpl.contains(p.getUniqueId().toString())) {
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
			String cfgp = cfg.getString("Configuration.CompatibilityOptions."
					+ eventName + "Priority");
			if (cfgp == null)
				return EventPriority.NORMAL;
			EventPriority priority = EventPriority.valueOf(cfgp);
			return priority == null ? EventPriority.NORMAL : priority;
		} catch (Exception e) {
			printException(e);
			return EventPriority.NORMAL;
		}
	}

	private void registerEvents() {
		try {
			PluginManager pm = this.getServer().getPluginManager();
			// general
			pm.registerEvents(new PlayerControl(), this);
			pm.registerEvents(WorldChangeEvent.getInstance(), this);
			// hooks
			if (pm.getPlugin("LibsDisguises") != null
					&& cfg.getBoolean("Configuration.Hooks.EnableLibsDisguisesHook"))
				pm.registerEvents(new LibsDisguisesHook(), this);
			if (pm.getPlugin("DisguiseCraft") != null
					&& cfg.getBoolean("Configuration.Hooks.EnableDisguiseCraftHook"))
				pm.registerEvents(new DisguiseCraftHook(), this);
			// join
			JoinEvent jevent = new JoinEvent();
			pm.registerEvent(PlayerJoinEvent.class, jevent,
					getEventPriority(PlayerJoinEvent.class), jevent, this,
					false);
			// quit
			QuitEvent qevent = new QuitEvent();
			pm.registerEvent(PlayerQuitEvent.class, qevent,
					getEventPriority(PlayerQuitEvent.class), qevent, this,
					false);
		} catch (Exception e) {
			printException(e);
		}
	}

	public void printException(Exception e) {
		try {
			System.err.println("[SuperVanish] Unknown Exception occurred!");
			if (requiresCfgUpdate || requiresMsgsUpdate) {
				System.err
						.println("[SuperVanish] You have an outdated configuration,");
				System.err
						.println("[SuperVanish] regenerating it by using '/sv updatecfg' might fix this problem.");
			} else
				System.err.println("[SuperVanish] Please report this issue!");
			System.err.println("Message: ");
			System.err.println("  " + e.getMessage());
			System.err.println("General information: ");
			String plugins = "";
			for (Plugin pl : Bukkit.getServer().getPluginManager().getPlugins()) {
				if (pl.getName().equalsIgnoreCase("SuperVanish"))
					continue;
				plugins = plugins + pl.getName() + " v"
						+ pl.getDescription().getVersion().toString() + ", ";
			}
			System.err.println("  ServerVersion: "
					+ this.getServer().getVersion().toString());
			System.err.println("  PluginVersion: "
					+ this.getDescription().getVersion().toString());
			System.err.println("  ServerPlugins: " + plugins);
			System.err.println("StackTrace: ");
			e.printStackTrace();
			System.err.println("[SuperVanish] Please include this information");
			System.err.println("[SuperVanish] if you report the issue.");
		} catch (Exception e2) {
			System.err
					.println("[SuperVanish] An exception occurred while trying to print a detailed stacktrace, printing an undetailed stacktrace of both exceptions:");
			System.err.println("ORIGINAL EXCEPTION:");
			e.printStackTrace();
			System.err.println("SECOND EXCEPTION:");
			e2.printStackTrace();
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String cmdLabel, String[] args) {
		new CmdManager(cmd, sender, args, cmdLabel);
		return true;
	}

	public void checkConfig() {
		try {
			String currentCfgVersion = cfg.getString("ConfigVersion");
			String newestVersion = getDescription().getVersion();
			String currentMsgsVersion = msgs.getString("MessagesVersion");
			this.requiresMsgsUpdate = requiresUpdate(currentMsgsVersion,
					newestVersion, false);
			this.requiresCfgUpdate = requiresUpdate(currentCfgVersion,
					newestVersion, true);
			// check if same
			if (newestVersion.equals(currentCfgVersion))
				this.requiresCfgUpdate = false;
			if (newestVersion.equals(currentMsgsVersion))
				this.requiresMsgsUpdate = false;
		} catch (Exception e) {
			printException(e);
		}
	}

	private boolean requiresUpdate(String currentVersion, String newestVersion,
			boolean checkCfg) {
		if (currentVersion == null)
			return true;
		for (String s : (checkCfg ? nonRequiredConfigUpdates
				: nonRequiredMsgsUpdates)) {
			String[] splitted = s.split("-");
			if (currentVersion.equalsIgnoreCase(splitted[0])
					&& newestVersion.equalsIgnoreCase(splitted[1]))
				return false;
		}
		return true;
	}

	public String convertString(String msg, Object unspecifiedPlayer) {
		try {
			replaceVariables: {
				if (unspecifiedPlayer instanceof OfflinePlayer
						&& !(unspecifiedPlayer instanceof Player)) {
					// offline player
					OfflinePlayer specifiedPlayer = (OfflinePlayer) unspecifiedPlayer;
					// replace permissionsex prefix and suffix
					if (getServer().getPluginManager().getPlugin(
							"PermissionsEx") != null) {
						msg = msg.replace("%prefix", "").replace("%suffix", "");
					}
					// replace essentials nick names
					if (getServer().getPluginManager().getPlugin("Essentials") != null) {
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
					// replace permissionsex prefix and suffix
					if (getServer().getPluginManager().getPlugin(
							"PermissionsEx") != null) {
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
					if (getServer().getPluginManager().getPlugin("Essentials") != null) {
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
				if (unspecifiedPlayer instanceof CommandSender
						&& !(unspecifiedPlayer instanceof Player)) {
					// console
					// replace permissionsex prefixes
					if (getServer().getPluginManager().getPlugin(
							"PermissionsEx") != null) {
						msg = msg.replace("%prefix", "").replace("%suffix", "");
					}
					// replace general variables
					msg = msg.replace("%d", "Console").replace("%p", "Console")
							.replace("%t", "Console");
					break replaceVariables;
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
		String rn = msgs.getString("Messages." + msg);
		if (rn == null)
			rn = "SV: Unavailable message in messages.yml: " + msg;
		return rn;
	}

	// override the standart config-api
	@Override
	public FileConfiguration getConfig() {
		return cfg;
	}

	@Override
	public void saveDefaultConfig() {
		ccfg.saveDefaultConfig();
	}
}