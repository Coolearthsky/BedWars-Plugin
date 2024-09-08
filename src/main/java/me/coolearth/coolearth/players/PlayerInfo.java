package me.coolearth.coolearth.players;

import me.coolearth.coolearth.Util.Team;
import me.coolearth.coolearth.Util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInfo {
    private final Map<UUID, PlayerAddons> m_players = new HashMap<>();
    private final Map<Team, TeamInfo> m_teams = new HashMap<>();
    private final JavaPlugin m_coolearth;

    public PlayerInfo(JavaPlugin coolearth) {
        m_coolearth = coolearth;
    }

    public TeamInfo getTeamInfo(Team team) {
        return m_teams.get(team);
    }

    public void stopLoops() {
        for (TeamInfo based : m_teams.values()) {
            based.stopAllLoops();
        }
        for (PlayerAddons based : m_players.values()) {
            based.stopAllLoops();
        }
    }

    public void stopAllPlayers() {
        m_teams.clear();
        m_players.clear();
    }

    public void startPlayers() {
        Map<Team, ArrayList<PlayerAddons>> plat = new HashMap<>();
        for (Team team : Team.values()) {
            if (team == Team.NONE) continue;
            plat.put(team, new ArrayList<>());
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerUUID = player.getUniqueId();
            Team team = Util.getTeam(player);
            PlayerAddons value = new PlayerAddons(m_coolearth, team, playerUUID);
            m_players.put(playerUUID, value);
            if (team == Team.NONE) continue;
            plat.get(team).add(value);
        }
        for (Team team : Team.values()) {
            if (team == Team.NONE) continue;
            Map<UUID, PlayerAddons> tempMap = new HashMap<>();
            for (PlayerAddons player : plat.get(team)) {
                tempMap.put(player.getPlayer(), player);
            }
            m_teams.put(team, new TeamInfo(team, m_coolearth, tempMap, () -> m_players));
        }
    }

    public void resetPlayers() {
        stopLoops();
        Map<Team, ArrayList<PlayerAddons>> plat = new HashMap<>();
        for (Team team : Team.values()) {
            if (team == Team.NONE) continue;
            plat.put(team, new ArrayList<>());
        }
        for (UUID playerUUID : m_players.keySet()) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null) continue;
            Team team = Util.getTeam(player);
            PlayerAddons value = new PlayerAddons(m_coolearth, team, playerUUID);
            m_players.replace(playerUUID, value);
            if (team == Team.NONE) continue;
            plat.get(team).add(value);
        }
        for (Team team : Team.values()) {
            if (team == Team.NONE) continue;
            Map<UUID, PlayerAddons> tempMap = new HashMap<>();
            for (PlayerAddons player : plat.get(team)) {
                tempMap.put(player.getPlayer(), player);
            }
            m_teams.replace(team, new TeamInfo(team, m_coolearth, tempMap, () -> m_players));
        }
    }

    public void startTeamGenerators() {
        for (TeamInfo team : m_teams.values()) {
            team.setUpgradeLevel(0);
        }
    }

    public PlayerAddons getPlayersInfo(Player player) {
        return m_players.get(player.getUniqueId());
    }

    public Map<Team, TeamInfo> getTeams() {
        return m_teams;
    }

    public Map<UUID, PlayerAddons> getPlayers() {
        return m_players;
    }

    public void updateTeams(Player player, Team team) {
        UUID playerUUID = player.getUniqueId();
        PlayerAddons playerAddons = m_players.get(playerUUID);
        if (playerAddons == null) return;
        m_teams.get(playerAddons.getTeam()).getMap().remove(playerUUID);
        playerAddons.setTeam(team);
        m_teams.get(team).getMap().put(playerUUID, playerAddons);
        Util.removeTeams(player);
        player.addScoreboardTag(team.getName());
    }
}