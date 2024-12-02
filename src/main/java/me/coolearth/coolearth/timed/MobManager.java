package me.coolearth.coolearth.timed;

import org.bukkit.entity.Mob;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class MobManager {
    private final JavaPlugin m_coolearth;

    public MobManager(JavaPlugin coolearth) {
        m_coolearth = coolearth;
    }

    public void placeMob(Mob mob, long delaySeconds) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (mob.isDead()) return;
                mob.remove();
            }
        };
        runnable.runTaskLater(m_coolearth, delaySeconds * 20);
    }
}