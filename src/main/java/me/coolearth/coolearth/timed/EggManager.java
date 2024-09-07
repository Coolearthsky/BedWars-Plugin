package me.coolearth.coolearth.timed;

import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.block.BlockManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class EggManager {

    private final JavaPlugin m_coolearth;
    private final Map<Projectile, BukkitRunnable> m_runnableMap = new HashMap<>();

    public EggManager(JavaPlugin coolearth){
        m_coolearth = coolearth;
    }

    public void newEgg(Projectile egg, BlockManager blockManager) {
        BukkitRunnable runnable = new BukkitRunnable() {
            private final Player player = (Player) egg.getShooter();
            private Location prevLocation = Util.convertToBlockLocation(egg.getLocation());
            private final Location initLocation = Util.convertToBlockLocation(egg.getLocation());
            @Override
            public void run() {
                Location location = Util.convertToBlockLocation(egg.getLocation());
                if (prevLocation.equals(location)) return;
                if (!blockManager.checkIfPlacable(location)) return;
                if (initLocation.equals(prevLocation)) {
                    prevLocation = location;
                    return;
                }
                switch (Util.getTeam(player)) {
                    case RED:
                        egg.getWorld().setBlockData(prevLocation, Material.RED_WOOL.createBlockData());
                        break;
                    case YELLOW:
                        egg.getWorld().setBlockData(prevLocation, Material.YELLOW_WOOL.createBlockData());
                        break;
                    case GREEN:
                        egg.getWorld().setBlockData(prevLocation, Material.LIME_WOOL.createBlockData());
                        break;
                    case BLUE:
                        egg.getWorld().setBlockData(prevLocation, Material.BLUE_WOOL.createBlockData());
                        break;
                    case NONE:
                        egg.getWorld().setBlockData(prevLocation, Material.WHITE_WOOL.createBlockData());
                        break;
                    default:
                        throw new UnsupportedOperationException("Not a team");
                }
                blockManager.add(prevLocation);
                prevLocation = location;
            }
        };
        runnable.runTaskTimer(m_coolearth, 3, 0);
        m_runnableMap.put(egg, runnable);
    }

    public void eggDies(Projectile egg) {
        m_runnableMap.get(egg).cancel();
        m_runnableMap.remove(egg);
    }

    public void resetAllLoops() {
        for (BukkitRunnable runnable : m_runnableMap.values()) {
            runnable.cancel();
        }
        m_runnableMap.clear();
    }
}
