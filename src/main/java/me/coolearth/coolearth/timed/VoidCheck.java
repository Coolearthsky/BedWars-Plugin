package me.coolearth.coolearth.timed;

import me.coolearth.coolearth.damage.DeathManager;
import me.coolearth.coolearth.global.Constants;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class VoidCheck {
    private final JavaPlugin m_coolearth;
    private final DeathManager m_deathManager;

    private BukkitRunnable m_runnable;

    public VoidCheck(JavaPlugin coolearth, DeathManager deathManager) {
        m_coolearth = coolearth;
        m_deathManager = deathManager;
        m_runnable = null;
    }

    public void startVoidCheck() {
        m_runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.getScoreboardTags().contains("player")) continue;
                    GameMode gameMode = player.getGameMode();
                    if ((player.getLocation().getY() > 80 || player.getLocation().getY() < -40) && gameMode != GameMode.SPECTATOR) {
                        player.teleport(Constants.getSpawn());
                        m_deathManager.onPlayerDeath(player, getMessage(player));
                    }
                }
            }
        };
        m_runnable.runTaskTimer(m_coolearth, 0, 0);
    }

    private String getMessage(Player player) {
        Player killer = player.getKiller();
        if (killer != null) {
            return player.getName() + " didn't want to live in the same world as " + killer.getName();
        } else {
            return player.getName() + " fell out of the world";
        }
    }

    public void stopVoidCheck() {
        if (m_runnable != null) {
            m_runnable.cancel();
        }
    }
}
