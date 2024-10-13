package me.coolearth.coolearth.scoreboard;

import me.coolearth.coolearth.Util.Materials;
import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.math.MathUtil;
import me.coolearth.coolearth.math.RomanNumber;
import me.coolearth.coolearth.players.PlayerAddons;
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
    public void createNewScoreboard(Player player, Materials material, int time, int level) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("Bedwars", "dummy");

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "BED WARS");

        PlayerAddons playersInfo = PlayerInfo.getPlayersInfo(player);
        if (playersInfo == null) return;
        objective.getScore(createKills(scoreboard, "Kills", "kills", playersInfo.getKills())).setScore(2);
        objective.getScore(createKills(scoreboard, "Final Kills", "finalKills", playersInfo.getFinalKills())).setScore(1);
        objective.getScore(createKills(scoreboard, "Beds Broken","bedsBroken", playersInfo.getBedsBroken())).setScore(0);
        objective.getScore("   ").setScore(3);

        TeamUtil[] values = TeamUtil.values();
        int i = values.length-1+4;
        for (TeamUtil team : values) {
            if (team.equals(TeamUtil.NONE)) continue;
            i--;
            objective.getScore(createTeam(scoreboard, team, getOpt(team),Util.getTeam(player))).setScore(i);
        }
        objective.getScore("  ").setScore(8);
        objective.getScore(createTimer(scoreboard, material, time, level)).setScore(9);
        objective.getScore(" ").setScore(10);
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
        int i = values.length-1+4;
        for (TeamUtil team : values) {
            if (team.equals(TeamUtil.NONE)) continue;
            i--;
            objective.getScore(createTeam(scoreboard, team, Optional.empty(), Util.getTeam(player))).setScore(i);
        }
        objective.getScore("  ").setScore(8);
        objective.getScore(createTimer(scoreboard, Materials.DIAMOND,6*60,2)).setScore(9);
        objective.getScore(" ").setScore(10);

        objective.getScore(createKills(scoreboard, "Kills", "kills",0)).setScore(2);
        objective.getScore(createKills(scoreboard, "Final Kills", "finalKills",0)).setScore(1);
        objective.getScore(createKills(scoreboard, "Beds Broken","bedsBroken",0)).setScore(0);
        objective.getScore("    ").setScore(3);

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

    private String createKills(Scoreboard scoreboard, String displayName, String name, int stat) {
        Team team = scoreboard.registerNewTeam(name);
        String key = ChatColor.WHITE + displayName + ": ";
        team.addEntry(key);
        team.setPrefix("");
        team.setSuffix(ChatColor.GREEN + "" + stat);
        return key;
    }

    private String createTeam(Scoreboard scoreboard, TeamUtil teamUtil, Optional<Integer> beds, TeamUtil playerTeam) {
        String name = teamUtil.getName();
        Team team = scoreboard.registerNewTeam(name);
        String key = teamUtil.getChatColor().toString();
        team.addEntry(key);
        String you = "";
        if (teamUtil.equals(playerTeam)) {
            you = ChatColor.GRAY + " YOU";
        }
        team.setPrefix(key + name.charAt(0) + " " + ChatColor.WHITE + name + ": ");
        team.setSuffix(getChar(beds) + you);
        return key;
    }

    private Optional<Integer> getOpt(TeamUtil team) {
        TeamInfo teamInfo = PlayerInfo.getTeamInfo(team);
        if (teamInfo.hasBed()) {
            return Optional.empty();
        } else {
            return Optional.of(teamInfo.numberOfAlivePeopleOnTeam());
        }
    }

    public void updateAllTeamsScoreboardsOfSpecificTeamsBed(TeamUtil team) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayersScoreboard(player, team, Optional.of(PlayerInfo.getTeamInfo(team).numberOfPeopleOnTeam()));
        }
    }

    public void startScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (TeamUtil team : TeamUtil.values()) {
                if (team.equals(TeamUtil.NONE)) continue;
                if (player.getScoreboard().getObjective("Bedwars") != null) {
                    updatePlayersScoreboard(player, team, Optional.empty());
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

    public void updatePlayersScoreboard(Player player, TeamUtil team, Optional<Integer> bed) {
        String you = "";
        if (team.equals(Util.getTeam(player))) {
            you = ChatColor.GRAY + " YOU";
        }
        Team team1 = player.getScoreboard().getTeam(team.getName());
        assert team1 != null;
        team1.setSuffix(getChar(bed) + you);
    }

    public void updatePlayersScoreboard(Player player, TeamUtil... teams) {
        for (TeamUtil team : teams) {
            updatePlayersScoreboard(player,team,getOpt(team));
        }
    }

    public void updatePlayersScoreboard(Player player) {
        for (TeamUtil team : TeamUtil.values()) {
            if (team.equals(TeamUtil.NONE)) continue;
            updatePlayersScoreboard(player, team, getOpt(team));
        }
    }

    public void updatePlayersScoreboardKills(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        PlayerAddons addons = PlayerInfo.getPlayersInfo(player);
        if (addons == null) return;
        addons.killedSomeone();
        scoreboard.getTeam("kills").setSuffix(ChatColor.GREEN + "" + addons.getKills());
    }

    public void updatePlayersScoreboardFinalKills(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        PlayerAddons addons = PlayerInfo.getPlayersInfo(player);
        if (addons == null) return;
        addons.finalKilledSomeone();
        scoreboard.getTeam("finalKills").setSuffix(ChatColor.GREEN + "" + addons.getFinalKills());
    }

    public void updatePlayersScoreboardBreakBed(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        PlayerAddons addons = PlayerInfo.getPlayersInfo(player);
        if (addons == null) return;
        addons.bedBrokeSomeone();
        scoreboard.getTeam("bedsBroken").setSuffix(ChatColor.GREEN + "" + addons.getBedsBroken());
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
