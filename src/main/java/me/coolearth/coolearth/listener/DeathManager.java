package me.coolearth.coolearth.listener;

import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.Constants;
import me.coolearth.coolearth.players.PlayerAddons;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.scoreboard.Board;
import org.bukkit.Bukkit;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DeathManager implements Listener {
    private final PlayerInfo m_playerInfo;
    private final Board m_board;
    public DeathManager(PlayerInfo playerInfo, Board board) {
        m_playerInfo = playerInfo;
        m_board = board;
    }

    @EventHandler
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

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        event.setDeathMessage("");
        Player player = event.getEntity();
        PlayerAddons playersInfo = m_playerInfo.getPlayersInfo(player);
        if (playersInfo == null) return;
        playersInfo.onDeath();
        if (!playersInfo.isAlive()) {
            m_board.updateSpecificTeamsScoreboards(playersInfo.getTeam());
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        event.setRespawnLocation(Constants.getSpawn());
    }
}
