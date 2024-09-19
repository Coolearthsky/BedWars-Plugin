package me.coolearth.coolearth.timed;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class ArmorStands {

    private final BukkitRunnable m_runnable;
    private final JavaPlugin m_coolearth;
    private final Map<ArmorStand, Double> initialY = new HashMap<>();

    public ArmorStands(JavaPlugin coolearth) {
        m_coolearth = coolearth;
        m_runnable = new MyBukkitRunnable();
        m_runnable.runTaskTimer(m_coolearth,0,0);
    }

    private void rotateArmorStandsConstantSpeed() {
        double rotationSpeed = 2.0; // degrees per tick

        for (ArmorStand armorStand : Bukkit.getWorld("world").getEntitiesByClass(ArmorStand.class)) {
            // Get current yaw (rotation around the Y axis)
            Location location = armorStand.getLocation();
            float currentYaw = location.getYaw();

            // Update the yaw to rotate the armor stand
            float newYaw = currentYaw + (float) rotationSpeed;
            if (newYaw >= 360.0f) {
                newYaw -= 360.0f;
            }
            location.setYaw(newYaw);
            armorStand.teleport(location);
        }
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
//            Bukkit.getLogger().info(((Float) speed).toString());
            for (ArmorStand armorStand : Bukkit.getWorld("world").getEntitiesByClass(ArmorStand.class)) {
                if (!armorStand.getScoreboardTags().contains("generator")) continue;
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
