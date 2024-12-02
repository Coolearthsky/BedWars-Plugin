package me.coolearth.coolearth.players;

import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInfo {
    private static final Map<UUID, PlayerAddons> m_players = new HashMap<>();
    private static final Map<TeamUtil, TeamInfo> m_teams = new HashMap<>();
    private static JavaPlugin m_coolearth;

    public static void register(JavaPlugin coolearth) {
        m_coolearth = coolearth;
    }

    public static TeamInfo getTeamInfo(TeamUtil team) {
        return m_teams.get(team);
    }

    public static void stopLoops() {
        for (TeamInfo based : m_teams.values()) {
            based.stopAllLoops();
        }
        for (PlayerAddons based : m_players.values()) {
            based.stopAllLoops();
        }
        stopAllPlayers();
    }

    private static void stopAllPlayers() {
        m_teams.clear();
        m_players.clear();
    }

    public static void startPlayers() {
        Map<TeamUtil, ArrayList<PlayerAddons>> plat = new HashMap<>();
        for (TeamUtil team : TeamUtil.values()) {
            if (team == TeamUtil.NONE) continue;
            plat.put(team, new ArrayList<>());
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerUUID = player.getUniqueId();
            TeamUtil team = Util.getTeam(player);
            PlayerAddons value = new PlayerAddons(m_coolearth, team, playerUUID);
            m_players.put(playerUUID, value);
            if (team == TeamUtil.NONE) continue;
            plat.get(team).add(value);
        }
        for (TeamUtil team : TeamUtil.values()) {
            if (team == TeamUtil.NONE) continue;
            Map<UUID, PlayerAddons> tempMap = new HashMap<>();
            for (PlayerAddons player : plat.get(team)) {
                tempMap.put(player.getPlayer(), player);
            }
            m_teams.put(team, new TeamInfo(team, m_coolearth, tempMap, () -> m_players));
        }
    }

    public static void startTeamGenerators() {
        for (TeamInfo team : m_teams.values()) {
            team.startSpawning();
        }
    }

    public static PlayerAddons getPlayersInfo(Player player) {
        return m_players.get(player.getUniqueId());
    }

    public static Map<TeamUtil, TeamInfo> getTeams() {
        return m_teams;
    }

    public static Map<UUID, PlayerAddons> getPlayers() {
        return m_players;
    }

    public static void updateTeams(Player player, TeamUtil team) {
        UUID playerUUID = player.getUniqueId();
        PlayerAddons playerAddons = m_players.get(playerUUID);
        if (playerAddons == null) return;
        TeamInfo teamInfo = m_teams.get(playerAddons.getTeam());
        teamInfo.getPeopleOnTeam().remove(playerUUID);
        TeamInfo teamInfo1 = m_teams.get(team);
        playerAddons.setTeam(team, teamInfo1.hasBed());
        teamInfo1.getPeopleOnTeam().put(playerUUID, playerAddons);
        Util.removeTeams(player);
        player.addScoreboardTag(team.getName());
        playerAddons.updateWoolState();
    }
}