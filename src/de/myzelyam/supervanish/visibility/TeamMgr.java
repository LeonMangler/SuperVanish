package de.myzelyam.supervanish.visibility;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TeamMgr {

    private boolean enabled, noPushEnabled;
    private String suffix, prefix;
    private SuperVanish plugin;
    private Table<Scoreboard, UUID, Team> scoreboardUUIDPrevTeamTable = HashBasedTable.create();

    public TeamMgr(SuperVanish plugin) {
        this.plugin = plugin;
        prefix = plugin.settings.getString("Configuration.Tablist.TabPrefix", "");
        prefix = prefix.length() > 16 ? prefix.substring(0, 16) : prefix;
        suffix = plugin.settings.getString("Configuration.Tablist.TabSuffix", "");
        suffix = suffix.length() > 16 ? suffix.substring(0, 16) : suffix;
        noPushEnabled = plugin.settings.getBoolean("Configuration.Players.DisablePush");
        enabled = !prefix.equals("") || !suffix.equals("") || noPushEnabled;
    }

    public void setVanished(Player p, Team prev) {
        if (!enabled) return;
        for (Scoreboard scoreboard : getAllScoreboards()) {
            Team team = scoreboard.getTeam("Vanished");
            if (team == null) {
                team = scoreboard.registerNewTeam("Vanished");
                team.setSuffix(
                        ChatColor.translateAlternateColorCodes('&', suffix));
                team.setPrefix(
                        ChatColor.translateAlternateColorCodes('&', prefix));
                if (plugin.isOneDotXOrHigher(9) && noPushEnabled) {
                    team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
                }
            }
            Team previousTeam = prev != null ? prev : p.getScoreboard().getEntryTeam(p.getName());
            if (previousTeam != null) {
                scoreboardUUIDPrevTeamTable.put(scoreboard, p.getUniqueId(), previousTeam);
                if (team.getEntries().size() < 2) {
                    if (prefix.equals("")) team.setPrefix(previousTeam.getPrefix());
                    if (suffix.equals("")) team.setSuffix(previousTeam.getSuffix());
                }
            }
            team.addEntry(p.getName());
        }
    }

    public void setNormal(Player p) {
        if (!enabled) return;
        for (Scoreboard scoreboard : getAllScoreboards()) {
            Team team = scoreboard.getTeam("Vanished");
            if (team == null)
                continue;
            team.removeEntry(p.getName());
            if (!scoreboardUUIDPrevTeamTable.contains(scoreboard, p.getUniqueId()))
                continue;
            Team previousTeam = scoreboardUUIDPrevTeamTable.get(scoreboard, p.getUniqueId());
            try {
                previousTeam.addEntry(p.getName());
            } catch (IllegalStateException ignored) {
            }
        }
    }

    private Set<Scoreboard> getAllScoreboards() {
        Set<Scoreboard> scoreboards = new HashSet<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            scoreboards.add(p.getScoreboard());
        }
        return scoreboards;
    }

    public boolean isOnlyPlayerWithScoreboard(Player p, Scoreboard scoreboard) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getScoreboard() == scoreboard && onlinePlayer != p) return false;
        }
        return true;
    }

    public void adjustLeavingScoreboard(Scoreboard scoreboard) {
        if (!enabled) return;
        Team team = scoreboard.getTeam("Vanished");
        if (team == null)
            return;
        // remove currently online vanished players
        for (Player vanished : plugin.getOnlineInvisiblePlayers()) {
            team.removeEntry(vanished.getName());
            Team previousTeam = scoreboardUUIDPrevTeamTable.get(scoreboard, vanished.getUniqueId());
            if (previousTeam == null) continue;
            try {
                previousTeam.addEntry(vanished.getName());
            } catch (IllegalStateException ignored) {
            }
        }
    }

    public void adjustJoiningScoreboard(Scoreboard scoreboard) {
        if (!enabled) return;
        Team team = scoreboard.getTeam("Vanished");
        if (team == null) {
            team = scoreboard.registerNewTeam("Vanished");
            team.setSuffix(
                    ChatColor.translateAlternateColorCodes('&', suffix));
            team.setPrefix(
                    ChatColor.translateAlternateColorCodes('&', prefix));
            if (plugin.isOneDotXOrHigher(9) && noPushEnabled) {
                team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            }
        }
        // add currently online vanished players
        for (Player vanished : plugin.getOnlineInvisiblePlayers()) {
            Team previousTeam = scoreboardUUIDPrevTeamTable.contains(scoreboard, vanished.getUniqueId())
                    ? scoreboardUUIDPrevTeamTable.get(scoreboard, vanished.getUniqueId()) : null;
            if (previousTeam != null && team.getEntries().size() < 2) {
                try {
                    if (prefix.equals("")) team.setPrefix(previousTeam.getPrefix());
                    if (suffix.equals("")) team.setSuffix(previousTeam.getSuffix());
                } catch (IllegalStateException ignored) {
                }
            }
            team.addEntry(vanished.getName());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
