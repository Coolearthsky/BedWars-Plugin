package me.coolearth.coolearth.listener;

import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.damage.DeathManager;
import me.coolearth.coolearth.global.Constants;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.players.PlayerAddons;
import me.coolearth.coolearth.players.TeamInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DeathListener implements Listener {
    private final DeathManager m_deathManager;
    public DeathListener(DeathManager deathManager) {
        m_deathManager = deathManager;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        m_deathManager.onDamage(event);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        event.setDeathMessage("");
        m_deathManager.onPlayerDeath(event.getEntity());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        event.setRespawnLocation(Constants.getSpawn());
    }
}
