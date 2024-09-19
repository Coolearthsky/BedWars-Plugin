package me.coolearth.coolearth.timed;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class VoidCheck {
    private final JavaPlugin m_coolearth;

    private BukkitRunnable m_runnable;

    public VoidCheck(JavaPlugin coolearth) {
        m_coolearth = coolearth;
        m_runnable = null;
    }

    public void startVoidCheck() {
        m_runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.getScoreboardTags().contains("player")) return;
                    GameMode gameMode = player.getGameMode();
                    if (player.getLocation().getY() < -40 && gameMode != GameMode.SPECTATOR) {
                        player.setHealth(0);
                    }
                }
            }
        };
        m_runnable.runTaskTimer(m_coolearth, 0, 0);
    }

    public void stopVoidCheck() {
        if (m_runnable != null) {
            m_runnable.cancel();
        }
    }
}
