/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.visibility;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.utils.OneDotNineUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

public class TeamMgr implements Listener {

    private final boolean enable, push;
    private final String suffix, prefix;
    private SuperVanish plugin;

    public TeamMgr(SuperVanish plugin) {
        this.plugin = plugin;
        prefix = plugin.settings.getString("Configuration.Tablist.TabPrefix", "");
        suffix = plugin.settings.getString("Configuration.Tablist.TabSuffix", "");
        push = plugin.settings.getBoolean("Configuration.Players.DisablePush");
        enable = !prefix.equals("") || !suffix.equals("") || push;
        if (enable) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    public void onReload() {
        if (enable)
            for (Player p : plugin.getOnlineInvisiblePlayers())
                addToTeam(p);
    }

    public void addToTeam(Player p) {
        if (!enable) return;
        // never use main scoreboard
        if (p.getScoreboard() == Bukkit.getScoreboardManager()
                .getMainScoreboard())
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        Team team = p.getScoreboard().getTeam("Vanished");
        if (team == null) {
            team = p.getScoreboard().registerNewTeam("Vanished");
        }
        try {
            if (suffix != null)
                team.setSuffix(
                        ChatColor.translateAlternateColorCodes('&', suffix));
            if (prefix != null) {
                team.setPrefix(
                        ChatColor.translateAlternateColorCodes('&', prefix));
            }
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("[SuperVanish] Either the TabPrefix or TabSuffix is too long!");
        }
        if (push && plugin.isOneDotXOrHigher(9)) {
            OneDotNineUtils.setNoPushForTeam(team);
        }
        team.addEntry(p.getName());
    }

    public void removeFromTeam(Player p) {
        if (!enable) return;
        Team team = p.getScoreboard().getTeam("Vanished");
        if (team == null)
            return;
        //noinspection deprecation
        team.removePlayer(p);
        if (team.getEntries().isEmpty())
            team.unregister();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVanish(PlayerHideEvent e) {
        final Player p = e.getPlayer();
        new BukkitRunnable() {

            @Override
            public void run() {
                addToTeam(p);
            }
        }.runTaskLater(plugin, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReappear(PlayerShowEvent e) {
        final Player p = e.getPlayer();
        new BukkitRunnable() {

            @Override
            public void run() {
                removeFromTeam(p);
            }
        }.runTaskLater(plugin, 1);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (plugin.getOnlineInvisiblePlayers().contains(e.getPlayer()))
            addToTeam(e.getPlayer());
    }
}
