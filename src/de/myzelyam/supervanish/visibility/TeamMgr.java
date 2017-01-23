package de.myzelyam.supervanish.visibility;

import de.myzelyam.supervanish.SuperVanish;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class TeamMgr {

    private boolean enable;

    public TeamMgr(SuperVanish plugin) {
        enable = plugin.settings.getBoolean("Configuration.Players.DisablePush");
    }

    public void setCantPush(Player p) {
        if (!enable) return;
        Team team = p.getScoreboard().getTeam("Vanished");
        if (team == null) {
            team = p.getScoreboard().registerNewTeam("Vanished");
        }
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        team.addEntry(p.getName());
    }

    public void setCanPush(Player p) {
        if (!enable) return;
        Team team = p.getScoreboard().getTeam("Vanished");
        if (team != null)
            team.removeEntry(p.getName());
    }

    public boolean isEnabled() {
        return enable;
    }
}
