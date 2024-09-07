package me.coolearth.coolearth.timed;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SpongeManager {
    private final JavaPlugin m_coolearth;

    public SpongeManager(JavaPlugin coolearth) {
        m_coolearth = coolearth;
    }

    public void placeSponge(Block block) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                block.setBlockData(Material.AIR.createBlockData());
            }
        };
        runnable.runTaskLater(m_coolearth, 0);
    }
}
