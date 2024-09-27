package me.coolearth.coolearth.players;

import me.coolearth.coolearth.PacketManager.ArmorPackets;
import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.Constants;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.scoreboard.Board;
import me.coolearth.coolearth.timed.Generators;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class PlayerJoinLeaveManager {
    private final PlayerInfo m_playerInfo;
    private final JavaPlugin m_coolearth;
    private final Generators m_generators;

    public PlayerJoinLeaveManager(PlayerInfo playerInfo, Generators generators, JavaPlugin coolearth) {
        m_playerInfo = playerInfo;
        m_coolearth = coolearth;
        m_generators = generators;
    }

    public void onPlayerJoin(Player player1) {
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
        m_generators.updateSafe(player1);
        if (m_playerInfo.getPlayers().containsKey(player)) {
            if (m_playerInfo.getPlayersInfo(player1).getAlive() && player1.getGameMode().equals(GameMode.SPECTATOR)) {
                m_playerInfo.getPlayersInfo(player1).onRespawn();
            }
            return;
        };
        TeamUtil team = Util.getMostEmptyAliveTeam(m_playerInfo);
        if (team == null) {
            player1.setGameMode(GameMode.SPECTATOR);
            player1.teleport(Constants.getSpawn());
            return;
        }
        PlayerAddons value = new PlayerAddons(m_coolearth, team, player);
        m_playerInfo.getPlayers().put(player, value);
        TeamInfo teamInfo = m_playerInfo.getTeamInfo(team);
        teamInfo.createUpgradesForPlayer(player);
        teamInfo.getPeopleOnTeam().put(player, value);
        Util.removeTeams(player1);
        player1.addScoreboardTag(team.getName());
        Util.setupPlayerFromStart(player1);
    }

    public void onPlayerQuit(Player player) {
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        ArmorPackets.stopLoop(player.getUniqueId());
    }
}
