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
import org.bukkit.scoreboard.*;

import java.util.Objects;
import java.util.Optional;

public class Board {

    public static void createNewScoreboard(Player player, Materials material, int timeSeconds, int level) {
        Bukkit.getLogger().info("Bro");
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective health = scoreboard.registerNewObjective("health", Criteria.HEALTH, "Health");
        health.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        Objective objective = scoreboard.registerNewObjective("Bedwars", "dummy");

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "BED WARS");

        PlayerAddons playersInfo = PlayerInfo.getPlayersInfo(player);
        if (playersInfo == null) return;
        String kills = createKills(scoreboard, "Kills", "kills", playersInfo.getKills());
        objective.getScore(kills).setScore(2);
        String finalKills = createKills(scoreboard, "Final Kills", "finalKills", playersInfo.getFinalKills());
        objective.getScore(finalKills).setScore(1);
        String bedsBroken = createKills(scoreboard, "Beds Broken", "bedsBroken", playersInfo.getBedsBroken());
        objective.getScore(bedsBroken).setScore(0);
        objective.getScore("   ").setScore(3);

        player.setPlayerListHeader(ChatColor.AQUA + "Welcome to Bedwars!");
        updateFooter(playersInfo);

        TeamUtil[] values = TeamUtil.values();
        int i = values.length-1+4;
        for (TeamUtil team : values) {
            if (team.equals(TeamUtil.NONE)) continue;
            i--;
            objective.getScore(createTeam(scoreboard, team, getOpt(team),Util.getTeam(player))).setScore(i);
        }
        objective.getScore("  ").setScore(8);
        objective.getScore(createTimer(scoreboard, material, timeSeconds, level)).setScore(9);
        objective.getScore(" ").setScore(10);

        for (TeamUtil teamUtil : TeamUtil.values()) {
            if (teamUtil.equals(TeamUtil.NONE)) continue;
            Team team = scoreboard.registerNewTeam(teamUtil.getName().charAt(0) + "");
            team.setPrefix(teamUtil.getChatColor() + ChatColor.BOLD.toString() + teamUtil.getName().charAt(0) + " ");
            team.setColor(teamUtil.getChatColor());
            team.setCanSeeFriendlyInvisibles(true);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getScoreboardTags().contains("player") || Util.getTeam(p) != teamUtil) continue ;
                team.addEntry(p.getName());
            }
        }

        player.setScoreboard(scoreboard);
    }

    public static void updateFooter(PlayerAddons playersInfo) {
        Bukkit.getPlayer(playersInfo.getPlayer()).setPlayerListFooter(ChatColor.AQUA + "Kills: " + ChatColor.YELLOW + playersInfo.getKills() + " " + ChatColor.AQUA + "Final Kills: " + ChatColor.YELLOW + playersInfo.getFinalKills() + " " + ChatColor.AQUA + "Beds Broken: " + ChatColor.YELLOW + playersInfo.getBedsBroken());
    }

    public static void updateAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getScoreboardTags().contains("player")) {
                updatePlayersScoreboard(player);
            }
        }
    }

    public static void endScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard());
            player.setPlayerListFooter( "");
        }
    }

    public static void updateSpecificTeamsScoreboards(TeamUtil... team) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayersScoreboard(player, team);
        }
    }

    public static void createNewScoreboardEmpty(Player player) {
        createNewScoreboard(player, Materials.DIAMOND,360,2);
    }

    private static String createTimer(Scoreboard scoreboard, Materials materials, int timeSeconds, int level) {
        Team team = scoreboard.registerNewTeam("timer");
        String key = ChatColor.WHITE.toString();
        team.addEntry(key);
        team.setPrefix("");
        team.setSuffix(materials.getName() + " " + RomanNumber.toRoman(level) + " in " + ChatColor.GREEN + MathUtil.convertToTime(timeSeconds));
        return key;
    }

    private static String createKills(Scoreboard scoreboard, String displayName, String name, int stat) {
        Team team = scoreboard.registerNewTeam(name);
        String key = displayName + ": ";
        team.addEntry(key);
        team.setPrefix("");
        team.setSuffix(ChatColor.GREEN + "" + stat);
        return key;
    }

    private static String createTeam(Scoreboard scoreboard, TeamUtil teamUtil, Optional<Integer> beds, TeamUtil playerTeam) {
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

    private static Optional<Integer> getOpt(TeamUtil team) {
        TeamInfo teamInfo = PlayerInfo.getTeamInfo(team);
        if (teamInfo.hasBed()) {
            return Optional.empty();
        } else {
            return Optional.of(teamInfo.numberOfAlivePeopleOnTeam());
        }
    }

    public static void updateAllTeamsScoreboardsOfSpecificTeamsBed(TeamUtil team) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayersScoreboard(player, team, Optional.of(PlayerInfo.getTeamInfo(team).numberOfPeopleOnTeam()));
        }
    }

    public static void startScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getScoreboard().getObjective("Bedwars") != null) {
                for (TeamUtil team : TeamUtil.values()) {
                    if (team.equals(TeamUtil.NONE)) continue;
                    updatePlayersScoreboard(player, team, Optional.empty());
                }
            } else {
                player.setHealth(19);
                createNewScoreboardEmpty(player);
            }
        }
    }

    public static void updateTime(Materials materials, int time, int level) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (materials == null) {
                player.getScoreboard().getTeam("timer").setSuffix("Dragon is coming trust guys");
                continue;
            }
            player.getScoreboard().getTeam("timer").setSuffix(materials.getName() + " " + RomanNumber.toRoman(level) + " in " + ChatColor.GREEN + MathUtil.convertToTime(time));
        }
    }

    public static void updatePlayersScoreboard(Player player, TeamUtil team, Optional<Integer> bed) {
        Scoreboard scoreboard = player.getScoreboard();
        Team scoreboardTeam = scoreboard.getTeam(team.getName().charAt(0) + "");
        String name = player.getName();
        TeamUtil playerTeam = Util.getTeam(player);
        if (scoreboardTeam.hasEntry(name)) {
            if (!playerTeam.equals(team)) {
                scoreboardTeam.removeEntry(name);
                scoreboard.getTeam(playerTeam.getName().charAt(0) + "").addEntry(name);
            }
        }
        String you = "";
        if (team.equals(playerTeam)) {
            you = ChatColor.GRAY + " YOU";
        }
        Team team1 = scoreboard.getTeam(team.getName());
        if (team1 == null) return;
        team1.setSuffix(getChar(bed) + you);
    }

    public static void updatePlayersScoreboard(Player player, TeamUtil... teams) {
        for (TeamUtil team : teams) {
            updatePlayersScoreboard(player,team,getOpt(team));
        }
    }

    public static void updatePlayersScoreboard(Player player) {
        for (TeamUtil team : TeamUtil.values()) {
            if (team.equals(TeamUtil.NONE)) continue;
            updatePlayersScoreboard(player, team, getOpt(team));
        }
    }

    public static void updatePlayersScoreboardKills(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        PlayerAddons addons = PlayerInfo.getPlayersInfo(player);
        if (addons == null) return;
        addons.killedSomeone();
        updateFooter(addons);
        scoreboard.getTeam("kills").setSuffix(ChatColor.GREEN + "" + addons.getKills());
    }

    public static void updatePlayersScoreboardFinalKills(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        PlayerAddons addons = PlayerInfo.getPlayersInfo(player);
        if (addons == null) return;
        addons.finalKilledSomeone();
        updateFooter(addons);
        scoreboard.getTeam("finalKills").setSuffix(ChatColor.GREEN + "" + addons.getFinalKills());
    }

    public static void updatePlayersScoreboardBreakBed(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        PlayerAddons addons = PlayerInfo.getPlayersInfo(player);
        if (addons == null) return;
        addons.bedBrokeSomeone();
        updateFooter(addons);
        scoreboard.getTeam("bedsBroken").setSuffix(ChatColor.GREEN + "" + addons.getBedsBroken());
    }

    public static void updatePlayersScoreboardSafe(Player player, Materials material, int time, int level) {
        if (player.getScoreboard().getObjective("Bedwars") != null) {
            updatePlayersScoreboard(player);
            updateTime(material, time, level);
        } else {
            createNewScoreboard(player, material, time, level);
        }
    }

    private static String getChar(Optional<Integer> bed) {
        if (!bed.isPresent()) {
            return ChatColor.GREEN + "✔";
        } else {
            Integer string = bed.get();
            if (string == 0) return "§c✘";
            return ChatColor.GREEN + string.toString();
        }
    }
}
