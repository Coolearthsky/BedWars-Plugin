package me.coolearth.coolearth.scoreboard;

import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.players.TeamInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Optional;

public class Board {

    private final PlayerInfo m_playerInfo;

    public Board(PlayerInfo playerInfo) {
        m_playerInfo = playerInfo;
    }

    public void createNewScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("Bedwars", "dummy");

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Bedwars");
        objective.getScore(" ").setScore(4);

        int i = 0;
        for (TeamUtil team : TeamUtil.values()) {
            if (team.equals(TeamUtil.NONE)) continue;
            objective.getScore(createTeam(scoreboard, team, getOpt(team, m_playerInfo))).setScore(i);
            i++;
        }
        player.setScoreboard(scoreboard);
    }

    public void updateAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayersScoreboard(player);
        }
    }

    public void updateSpecificTeamsScoreboards(TeamUtil... team) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayersScoreboard(player, team);
        }
    }

    public void createNewScoreboardEmpty(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("Bedwars", "dummy");

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Bedwars");
        objective.getScore(" ").setScore(4);

        int i = 0;
        for (TeamUtil team : TeamUtil.values()) {
            if (team.equals(TeamUtil.NONE)) continue;
            objective.getScore(createTeam(scoreboard, team, Optional.empty())).setScore(i);
            i++;
        }
        player.setScoreboard(scoreboard);
    }
    private String createTeam(Scoreboard scoreboard, TeamUtil teamUtil, Optional<Integer> beds) {
        String name = teamUtil.getName();
        Team team = scoreboard.registerNewTeam(name);
        String key = teamUtil.getChatColor().toString();
        team.addEntry(key);
        team.setPrefix(key + name.charAt(0) + " " + ChatColor.WHITE + name + ": ");
        team.setSuffix(getChar(beds));
        return key;
    }

    private Optional<Integer> getOpt(TeamUtil team, PlayerInfo playerInfo) {
        TeamInfo teamInfo = playerInfo.getTeamInfo(team);
        if (teamInfo.hasBed()) {
            return Optional.empty();
        } else {
            return Optional.of(teamInfo.numberOfAlivePeopleOnTeam());
        }
    }

    public void updateAllTeamsScoreboardsOfSpecificTeamsBed(TeamUtil team) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayersScoreboard(player.getScoreboard(), team, Optional.of(m_playerInfo.getTeamInfo(team).numberOfPeopleOnTeam()));
        }
    }

    public void startScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (TeamUtil team : TeamUtil.values()) {
                if (team.equals(TeamUtil.NONE)) continue;
                if (player.getScoreboard().getObjective("Bedwars") != null) {
                    updatePlayersScoreboard(player.getScoreboard(), team, Optional.empty());
                } else {
                    createNewScoreboardEmpty(player);
                }
            }
        }
    }

    public void updatePlayersScoreboard(Scoreboard scoreboard, TeamUtil team, Optional<Integer> bed) {
        Team team1 = scoreboard.getTeam(team.getName());
        assert team1 != null;
        team1.setSuffix(getChar(bed));
    }

    public void updatePlayersScoreboard(Player player, TeamUtil... teams) {
        for (TeamUtil team : teams) {
            Team team1 = player.getScoreboard().getTeam(team.getName());
            team1.setSuffix(getChar(getOpt(team, m_playerInfo)));
        }
    }

    public void updatePlayersScoreboard(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        for (TeamUtil team : TeamUtil.values()) {
            if (team.equals(TeamUtil.NONE)) continue;
            updatePlayersScoreboard(scoreboard, team, getOpt(team, m_playerInfo));
        }
    }

    private String getChar(Optional<Integer> bed) {
        if (!bed.isPresent()) {
            return ChatColor.GREEN + "✔";
        } else {
            Integer string = bed.get();
            if (string == 0) return "§c✘";
            return ChatColor.GREEN + string.toString();
        }
    }
}
