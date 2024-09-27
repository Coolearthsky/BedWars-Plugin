package me.coolearth.coolearth.scoreboard;

import me.coolearth.coolearth.Util.Materials;
import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.math.MathUtil;
import me.coolearth.coolearth.math.RomanNumber;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.players.TeamInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Objects;
import java.util.Optional;

public class Board {

    private final PlayerInfo m_playerInfo;

    public Board(PlayerInfo playerInfo) {
        m_playerInfo = playerInfo;
    }

    public void createNewScoreboard(Player player, Materials material, int time, int level) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("Bedwars", "dummy");

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "BED WARS");

        TeamUtil[] values = TeamUtil.values();
        int i = values.length-1;
        for (TeamUtil team : values) {
            if (team.equals(TeamUtil.NONE)) continue;
            objective.getScore(createTeam(scoreboard, team, getOpt(team, m_playerInfo))).setScore(i);
            i--;
        }
        objective.getScore("  ").setScore(4);
        objective.getScore(createTimer(scoreboard, material, time, level)).setScore(5);
        objective.getScore(" ").setScore(6);
        player.setScoreboard(scoreboard);
    }

    public void updateAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayersScoreboard(player);
        }
    }

    public void endScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard());
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
        objective.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "BED WARS");

        TeamUtil[] values = TeamUtil.values();
        int i = values.length-1;
        for (TeamUtil team : values) {
            if (team.equals(TeamUtil.NONE)) continue;
            objective.getScore(createTeam(scoreboard, team, Optional.empty())).setScore(i);
            i--;
        }
        objective.getScore("  ").setScore(4);
        objective.getScore(createTimer(scoreboard, Materials.DIAMOND,6*60,2)).setScore(5);
        objective.getScore(" ").setScore(6);

        player.setScoreboard(scoreboard);
    }

    private String createTimer(Scoreboard scoreboard, Materials materials, int time, int level) {
        Team team = scoreboard.registerNewTeam("timer");
        String key = ChatColor.WHITE.toString();
        team.addEntry(key);
        team.setPrefix("");
        team.setSuffix(materials.getName() + " " + RomanNumber.toRoman(level) + " in " + ChatColor.GREEN + MathUtil.convertToTime(time));
        return key;
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

    public void updateTime(Materials materials, int time, int level) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (materials == null) {
                player.getScoreboard().getTeam("timer").setSuffix("Dragon is coming trust guys");
                return;
            }
            player.getScoreboard().getTeam("timer").setSuffix(materials.getName() + " " + RomanNumber.toRoman(level) + " in " + ChatColor.GREEN + MathUtil.convertToTime(time));
        }
    }

    public void updatePlayersScoreboard(Scoreboard scoreboard, TeamUtil team, Optional<Integer> bed) {
        Team team1 = scoreboard.getTeam(team.getName());
        assert team1 != null;
        team1.setSuffix(getChar(bed));
    }

    public void updatePlayersScoreboard(Player player, TeamUtil... teams) {
        for (TeamUtil team : teams) {
            updatePlayersScoreboard(player.getScoreboard(),team,getOpt(team, m_playerInfo));
        }
    }

    public void updatePlayersScoreboard(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        for (TeamUtil team : TeamUtil.values()) {
            if (team.equals(TeamUtil.NONE)) continue;
            updatePlayersScoreboard(scoreboard, team, getOpt(team, m_playerInfo));
        }
    }

    public void updatePlayersScoreboardSafe(Player player, Materials material, int time, int level) {
        if (player.getScoreboard().getObjective("Bedwars") != null) {
            updatePlayersScoreboard(player);
            updateTime(material, time, level);
        } else {
            createNewScoreboard(player, material, time, level);
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
