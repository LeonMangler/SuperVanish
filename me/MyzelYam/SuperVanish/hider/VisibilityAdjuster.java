package me.MyzelYam.SuperVanish.hider;

import java.util.List;

import me.MyzelYam.SuperVanish.api.PlayerHideEvent;
import me.MyzelYam.SuperVanish.api.PlayerShowEvent;
import me.MyzelYam.SuperVanish.config.MessagesCfg;
import me.MyzelYam.SuperVanish.hider.TabManager.SVTabAction;
import me.MyzelYam.SuperVanish.hooks.DynmapHook;
import me.MyzelYam.SuperVanish.hooks.EssentialsHook;
import me.confuser.barapi.BarAPI;
import me.libraryaddict.disguise.DisguiseAPI;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;

public class VisibilityAdjuster extends PlayerHider {

	private static VisibilityAdjuster instance;

	public static VisibilityAdjuster getInstance() {
		if (instance == null)
			return new VisibilityAdjuster();
		return instance;
	}

	public void hidePlayer(Player p) {
		try {
			if (p == null)
				throw new NullPointerException("The player cannot be null!");
			MessagesCfg mcfg = new MessagesCfg();
			FileConfiguration messages = mcfg.getConfig();
			String bossbar = messages
					.getString("Messages.BossBarVanishMessage");
			String vanishMessage = messages.getString("Messages.VanishMessage");
			String vanishMessageWithPermission = messages
					.getString("Messages.VanishMessageWithPermission");
			String onVanishMessage = messages.getString("Messages.OnVanish");
			if (getInvisiblePlayers().contains(p.getUniqueId().toString())) {
				System.err
						.println("[SuperVanish] Error: Could not hide player "
								+ p.getName() + ", he is already invisible!");
				return;
			}
			// /////////////////////////////////////////////////////////
			PlayerHideEvent e = new PlayerHideEvent(p);
			plugin.getServer().getPluginManager().callEvent(e);
			if (e.isCancelled()) {
				return;
			}
			// /////
			if (plugin.getServer().getPluginManager()
					.getPlugin("DisguiseCraft") != null) {
				DisguiseCraftAPI dcAPI = DisguiseCraft.getAPI();
				if (dcAPI.isDisguised(p)) {
					p.sendMessage("§c[SV] Please undisguise yourself.");
					return;
				}
			}
			if (plugin.getServer().getPluginManager()
					.getPlugin("LibsDisguises") != null) {
				if (DisguiseAPI.isDisguised(p)) {
					p.sendMessage("§c[SV] Please undisguise yourself.");
					return;
				}
			}
			if (plugin.getServer().getPluginManager().getPlugin("BarAPI") != null
					&& cfg.getBoolean("Configuration.Messages.UseBarAPI")) {
				BarAPI.setMessage(p, plugin.convertString(bossbar, p), 100f);
			}
			if (plugin.getServer().getPluginManager().getPlugin("Essentials") != null) {
				EssentialsHook.hidePlayer(p);
			}
			if (cfg.getBoolean("Configuration.Players.Fly.Enable")) {
				p.setAllowFlight(true);
			}
			if (plugin.getServer().getPluginManager().getPlugin("dynmap") != null) {
				DynmapHook.adjustVisibility(p, true);
			}
			// action bars
			if (plugin.getServer().getPluginManager().getPlugin("ProtocolLib") != null
					&& cfg.getBoolean("Configuration.Messages.DisplayActionBarsToInvisiblePlayers")) {
				ActionBarManager.getInstance(plugin).addActionBar(p);
			}
			// vanish msg
			if (cfg.getBoolean("Configuration.Messages.VanishReappearMessages.BroadcastMessageOnVanish")) {
				String msg1 = vanishMessage;
				String msg2 = vanishMessageWithPermission;
				for (Player ap : Bukkit.getOnlinePlayers()) {
					if (!ap.hasPermission("sv.see")) {
						if (!cfg.getBoolean("Configuration.Messages.VanishReappearMessages.SendMessageOnlyToAdmins")) {
							ap.sendMessage(plugin.convertString(msg1, p));
						}
					} else {
						if (!cfg.getBoolean("Configuration.Messages.VanishReappearMessages.SendMessageOnlyToUsers")) {
							if (!cfg.getBoolean("Configuration.Messages.VanishReappearMessages.SendDifferentMessages")) {
								ap.sendMessage(plugin.convertString(msg1, p));
							} else {
								if (ap.getUniqueId().toString()
										.equals(p.getUniqueId().toString()))
									ap.sendMessage(plugin
											.convertString(msg1, p));
								else
									ap.sendMessage(plugin
											.convertString(msg2, p));
							}
						}
					}
				}
			}
			// tab
			if (cfg.getBoolean("Configuration.Tablist.ChangeTabNames")) {
				TabManager.getInstance().adjustTabname(p,
						SVTabAction.SET_CUSTOM_TABNAME);
			}
			p.sendMessage(convertString(onVanishMessage, p));
			List<String> vpl = getInvisiblePlayers();
			vpl.add(p.getUniqueId().toString());
			plugin.pd.set("InvisiblePlayers", vpl);
			plugin.spd();
			// ghost team
			if (cfg.getBoolean("Configuration.Players.EnableGhostPlayers")
					&& plugin.ghostTeam != null) {
				if (!plugin.ghostTeam.hasPlayer(p)) {
					if (p.hasPermission("sv.see")
							|| p.hasPermission("sv.use")
							|| getInvisiblePlayers().contains(
									p.getUniqueId().toString()))
						plugin.ghostTeam.addPlayer(p);
				}
				// invis
				p.addPotionEffect(new PotionEffect(
						PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
			}
			// hide
			hideToAll(p);
		} catch (Exception e) {
			plugin.printException(e);
		}
	}

	public void showPlayer(Player p) {
		try {
			// check
			if (p == null)
				throw new NullPointerException("The player cannot be null!");
			// preparations
			final Player fp = p;
			MessagesCfg mcfg = new MessagesCfg();
			FileConfiguration messages = mcfg.getConfig();
			String bossbar = messages
					.getString("Messages.BossBarReappearMessage");
			String reappearMessage = messages
					.getString("Messages.ReappearMessage");
			String reappearMessageWithPermission = messages
					.getString("Messages.ReappearMessageWithPermission");
			String onReappearMessage = messages
					.getString("Messages.OnReappear");
			if (!getInvisiblePlayers().contains(p.getUniqueId().toString())) {
				System.err
						.println("[SuperVanish] Error: Could not show player "
								+ p.getName() + ", he is already visible!");
				return;
			}
			// event
			PlayerShowEvent e = new PlayerShowEvent(p);
			plugin.getServer().getPluginManager().callEvent(e);
			if (e.isCancelled()) {
				return;
			}
			// ghost
			if (cfg.getBoolean("Configuration.Players.EnableGhostPlayers")
					&& p.hasPotionEffect(PotionEffectType.INVISIBILITY))
				p.removePotionEffect(PotionEffectType.INVISIBILITY);
			// bar-api
			if (plugin.getServer().getPluginManager().getPlugin("BarAPI") != null
					&& cfg.getBoolean("Configuration.Messages.UseBarAPI")) {

				BarAPI.setMessage(p, plugin.convertString(bossbar, p), 100f);
				BarAPI.removeBar(fp);
				Bukkit.getServer().getScheduler()
						.scheduleSyncDelayedTask(plugin, new Runnable() {

							public void run() {
								BarAPI.removeBar(fp);
							}
						}, 20);
			}
			// fly
			if (cfg.getBoolean("Configuration.Players.Fly.DisableOnReappear")
					&& !p.hasPermission("sv.fly")
					&& p.getGameMode() != GameMode.CREATIVE
					&& p.getGameMode() != GameMode.SPECTATOR) {
				p.setAllowFlight(false);
			}
			// ess
			if (plugin.getServer().getPluginManager().getPlugin("Essentials") != null) {
				EssentialsHook.showPlayer(p);
			}
			// dynm
			if (plugin.getServer().getPluginManager().getPlugin("dynmap") != null) {
				DynmapHook.adjustVisibility(p, false);
			}
			// action bars
			if (plugin.getServer().getPluginManager().getPlugin("ProtocolLib") != null
					&& cfg.getBoolean("Configuration.Messages.DisplayActionBarsToInvisiblePlayers")) {
				ActionBarManager.getInstance(plugin).removeActionBar(p);
			}
			// join-msg
			if (cfg.getBoolean("Configuration.Messages.VanishReappearMessages.BroadcastMessageOnReappear")) {
				String msg1 = reappearMessage;
				String msg2 = reappearMessageWithPermission;
				for (Player ap : Bukkit.getOnlinePlayers()) {
					if (!ap.hasPermission("sv.see")) {
						if (!cfg.getBoolean("Configuration.Messages.VanishReappearMessages.SendMessageOnlyToAdmins")) {
							ap.sendMessage(plugin.convertString(msg1, p));
						}
					} else {
						if (!cfg.getBoolean("Configuration.Messages.VanishReappearMessages.SendMessageOnlyToUsers")) {
							if (!cfg.getBoolean("Configuration.Messages.VanishReappearMessages.SendDifferentMessages")) {
								ap.sendMessage(plugin.convertString(msg1, p));
							} else {
								if (ap.getUniqueId().toString()
										.equals(p.getUniqueId().toString()))
									ap.sendMessage(plugin
											.convertString(msg1, p));
								else
									ap.sendMessage(plugin
											.convertString(msg2, p));
							}
						}
					}
				}
			}
			// tab
			if (cfg.getBoolean("Configuration.Tablist.ChangeTabNames")) {
				TabManager.getInstance().adjustTabname(p,
						SVTabAction.RESTORE_NORMAL_TABNAME);
			}
			// chatmsg
			p.sendMessage(plugin.convertString(onReappearMessage, p));
			// lists
			List<String> vpl = getInvisiblePlayers();
			vpl.remove(p.getUniqueId().toString());
			plugin.pd.set("InvisiblePlayers", vpl);
			plugin.spd();
			// show
			showToAll(p);
		} catch (Exception e) {
			plugin.printException(e);
		}
	}
}