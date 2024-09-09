package me.coolearth.coolearth.listener;

import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.Constants;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.players.PlayerAddons;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.players.TeamInfo;
import me.coolearth.coolearth.scoreboard.Board;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class PlayerListener implements Listener {

    private final PlayerInfo m_playerInfo;
    private final JavaPlugin m_coolearth;
    private final Board m_board;

    public PlayerListener(PlayerInfo playerInfo, Board board, JavaPlugin coolearth) {
        m_playerInfo = playerInfo;
        m_coolearth = coolearth;
        m_board = board;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player1 = event.getPlayer();
        if (player1.getLastPlayed() == 0) {
            player1.addScoreboardTag("player");
        }
        UUID player = player1.getUniqueId();
        Bukkit.getLogger().info(player1.getAddress().getAddress().getAddress().toString());
        Bukkit.getLogger().info(player1.getAddress().toString());
        if (!player1.getScoreboardTags().contains("player") || !GlobalVariables.isGameActive()) {
            player1.setGameMode(GameMode.SPECTATOR);
            player1.teleport(Constants.getSpawn());
            return;
        }
        if (player1.getScoreboard().getObjective("Bedwars") != null) {
            m_board.updatePlayersScoreboard(player1);
        } else {
            m_board.createNewScoreboard(player1);
        }
        if (m_playerInfo.getPlayers().containsKey(player)) return;
        TeamUtil team = Util.getMostEmptyAliveTeam(m_playerInfo);
        if (team == null) {
            player1.setGameMode(GameMode.SPECTATOR);
            player1.teleport(Constants.getSpawn());
            return;
        }
        PlayerAddons value = new PlayerAddons(m_coolearth, team, player);
        m_playerInfo.getPlayers().put(player, value);
        TeamInfo teamInfo = m_playerInfo.getTeamInfo(team);
        teamInfo.getMap().put(player, value);
        Util.removeTeams(player1);
        player1.addScoreboardTag(team.getName());
        Util.setupPlayerFromStart(player1);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

//        Board.updateScoreboards();
    }
}
