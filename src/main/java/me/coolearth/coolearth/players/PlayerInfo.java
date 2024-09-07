package me.coolearth.coolearth.players;

import me.coolearth.coolearth.Util.Team;
import me.coolearth.coolearth.Util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlayerInfo {
    private final Map<Player, PlayerAddons> m_players = new HashMap<>();
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
            Team team = Util.getTeam(player);
            PlayerAddons value = new PlayerAddons(m_coolearth, team, player, true);
            m_players.put(player, value);
            if (team == Team.NONE) continue;
            plat.get(team).add(value);
        }
        for (Team team : Team.values()) {
            if (team == Team.NONE) continue;
            Map<Player, PlayerAddons> tempMap = new HashMap<>();
            for (PlayerAddons player : plat.get(team)) {
                tempMap.put(player.getPlayer(), player);
            }
            m_teams.put(team, new TeamInfo(team, m_coolearth, tempMap, m_players));
        }
    }

    public void resetPlayers() {
        stopLoops();
        Map<Team, ArrayList<PlayerAddons>> plat= new HashMap<>();
        for (Team team : Team.values()) {
            if (team == Team.NONE) continue;
            plat.put(team, new ArrayList<>());
        }
        for (Player player : m_players.keySet()) {
            Team team = Util.getTeam(player);
            PlayerAddons value = new PlayerAddons(m_coolearth,team, player, true);
            m_players.replace(player, value);
            if (team == Team.NONE) continue;
            plat.get(team).add(value);
        }
        for (Team team : Team.values()) {
            if (team == Team.NONE) continue;
            Map<Player, PlayerAddons> tempMap = new HashMap<>();
            for (PlayerAddons player : plat.get(team)) {
                tempMap.put(player.getPlayer(), player);
            }
            m_teams.replace(team, new TeamInfo(team, m_coolearth,tempMap, m_players));
        }
    }

    public void startTeamGenerators() {
        for (TeamInfo team : m_teams.values()) {
            team.setUpgradeLevel(0);
        }
    }

    public PlayerAddons getPlayersInfo(Player player) {
       return m_players.get(player);
    }

    public Map<Player, PlayerAddons> getPlayers() {
        return m_players;
    }

    public void updateTeams(Player player, Team team) {
        PlayerAddons playerAddons = m_players.get(player);
        if (playerAddons == null) return;
        m_teams.get(playerAddons.getTeam()).getMap().remove(player);
        playerAddons.setTeam(team);
        m_teams.get(team).getMap().put(player, playerAddons);
        Util.removeTeams(player);
        player.addScoreboardTag(team.getName());
    }
}