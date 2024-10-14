package me.coolearth.coolearth.timed;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class HealthUpdate {

    private final JavaPlugin m_coolearth;

    public HealthUpdate(JavaPlugin coolearth) {
        m_coolearth = coolearth;
    }

    public void fixHealth() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    player.setHealth(20);
                }
            };
            runnable.runTaskLater(m_coolearth, 2);
        }
    }
}
