package me.coolearth.coolearth.timed;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ArmorStands {

    private final BukkitRunnable m_runnable;
    private final JavaPlugin m_coolearth;

    public ArmorStands(JavaPlugin coolearth) {
        m_coolearth = coolearth;
        m_runnable = new MyBukkitRunnable();
        m_runnable.runTaskTimer(m_coolearth,0,0);
    }

    private static class MyBukkitRunnable extends BukkitRunnable {
        float speed = 0;
        float accel = 0.75f;
        float swapAngle = 21f;
        float maxVel = 16.5f;
        @Override
        public void run() {
            speed += accel;
            if (speed == swapAngle) {
                accel *= -1;
            }
            if (speed == -swapAngle) {
                accel *= -1;
            }
            for (ArmorStand armorStand : Bukkit.getWorld("world").getEntitiesByClass(ArmorStand.class)) {
                if (!armorStand.getScoreboardTags().contains("spinning")) continue;
                Location location = armorStand.getLocation();
                float yaw;
                float vel;
                if (Math.abs(speed) <= maxVel) {
                    vel = speed;
                } else {
                    if (speed < 0) {
                        vel = -maxVel;
                    } else {
                        vel = maxVel;
                    }
                }
                yaw = (location.getYaw() + vel) % 360;
                location.setYaw(yaw);
                location.setY(location.getY() + vel/2000);
                armorStand.teleport(location);
            }
        }
    }
}
