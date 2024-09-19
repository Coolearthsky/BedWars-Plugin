package me.coolearth.coolearth.listener;

import me.coolearth.coolearth.players.PlayerJoinLeaveManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final PlayerJoinLeaveManager m_playerInfo;

    public PlayerListener(PlayerJoinLeaveManager playerInfo) {
        m_playerInfo = playerInfo;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        m_playerInfo.onPlayerJoin(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        m_playerInfo.onPlayerQuit(event.getPlayer());
    }
}
