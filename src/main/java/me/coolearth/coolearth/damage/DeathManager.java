package me.coolearth.coolearth.damage;

import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.players.PlayerAddons;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.players.TeamInfo;
import me.coolearth.coolearth.scoreboard.Board;
import me.coolearth.coolearth.startstop.StopGame;
import org.bukkit.ChatColor;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class DeathManager {

    private final PlayerInfo m_playerInfo;
    private final Board m_board;
    private final StopGame m_stopGame;
    public DeathManager(PlayerInfo playerInfo, Board board, StopGame stopGame) {
        m_playerInfo = playerInfo;
        m_board = board;
        m_stopGame = stopGame;
    }

    public void onDamage(EntityDamageEvent event) {
        if (!event.getEntityType().equals(EntityType.PLAYER)) return;
        DamageType damageType = event.getDamageSource().getDamageType();
        if (damageType.equals(DamageType.PLAYER_EXPLOSION)) {
            event.setDamage(0);
        }
        else if (damageType.equals(DamageType.EXPLOSION)) {
            event.setDamage(2);
        }
    }

    public void onPlayerDeath(Player player) {
        if (!player.getScoreboardTags().contains("player") || !GlobalVariables.isGameActive()) return;
        PlayerAddons playersInfo = m_playerInfo.getPlayersInfo(player);
        if (playersInfo == null) return;
        playersInfo.onDeath();
        if (!playersInfo.isAlive()) {
            TeamUtil team1 = playersInfo.getTeam();
            Util.broadcastMessage(team1.getChatColor() + player.getName() + ChatColor.GRAY + " died " + "§b" + ChatColor.BOLD + "FINAL KILL!");
            player.sendMessage(ChatColor.RED + "You have been eliminated!");
            m_board.updateSpecificTeamsScoreboards(team1);
            if (!m_playerInfo.getTeamInfo(team1).isAnyoneOnTeamAlive()) {
                Util.broadcastMessage("\n" + ChatColor.BOLD + "TEAM ELIMINATED > " + team1.getChatColor() + team1.getName() + " Team " + ChatColor.RED + "has been eliminated!"+ "\n");
                Util.broadcastMessage("");
            }
            TeamUtil aliveTeam = null;
            for (TeamInfo team : m_playerInfo.getTeams().values()) {
                if (team.isAnyoneOnTeamAlive()) {
                    if (aliveTeam != null) return;
                    aliveTeam = team.getTeam();
                }
            }
            if (aliveTeam != null) {
                Util.broadcastMessage(aliveTeam.getChatColor() + aliveTeam.getName() + ChatColor.GRAY + " team won!");
            } else {
                Util.broadcastMessage("Game end");
            }
            m_stopGame.stop();
        } else {
            player.sendTitle(ChatColor.RED + "YOU DIED!",ChatColor.YELLOW + "You will respawn in " +ChatColor.RED + "5 " + ChatColor.YELLOW + "seconds!",0,20*1,0);
            Util.broadcastMessage(playersInfo.getTeam().getChatColor() + player.getName() + ChatColor.GRAY + " died");
        }
    }
}
