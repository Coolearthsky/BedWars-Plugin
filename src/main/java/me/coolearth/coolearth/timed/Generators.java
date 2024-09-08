package me.coolearth.coolearth.timed;

import com.comphenix.protocol.wrappers.Pair;
import me.coolearth.coolearth.Util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;


public class Generators {
    private final JavaPlugin m_coolearth;
    private final Map<Material, BukkitRunnable> m_spawners = new HashMap<>();

    public Generators(JavaPlugin coolearth) {
        m_coolearth = coolearth;
    }

    public void start() {
        Bukkit.getLogger().info("STARTING");
        List<Pair<Optional<Integer>, Integer>> diamondLoops = new ArrayList<>();
        diamondLoops.add(new Pair<>(Optional.of(16), 50));
        diamondLoops.add(new Pair<>(Optional.of(16), 40));
        diamondLoops.add(new Pair<>(Optional.empty(), 30));
        List<Pair<Optional<Integer>, Integer>> emeraldLoops = new ArrayList<>();
        emeraldLoops.add(new Pair<>(Optional.of(16), 60));
        emeraldLoops.add(new Pair<>(Optional.of(16), 45));
        emeraldLoops.add(new Pair<>(Optional.empty(), 30));
        setLoops(diamondLoops,Material.DIAMOND);
        setLoops(emeraldLoops,Material.EMERALD);
    }

    public void resetAllLoops() {
        for (BukkitRunnable runnable : m_spawners.values()) {
            runnable.cancel();
        }
        m_spawners.clear();
    }

    private void setLoops(List<Pair<Optional<Integer>, Integer>> loopsAndSeconds, Material material) {
        closeRunnable(material);
        int seconds = loopsAndSeconds.get(0).getSecond();
        if (!loopsAndSeconds.get(0).getFirst().isPresent()) {
            setInfinite(seconds, material);
            return;
        }
        int loops = loopsAndSeconds.get(0).getFirst().get();
        m_spawners.put(material, new BukkitRunnable() {
            int count = loops;
            public void run()
            {
                count--;
                if (count <= 0) {
                    loopsAndSeconds.remove(0);
                    setLoops(loopsAndSeconds, material);
                }
                setItem(material);
            }
        });
        m_spawners.get(material).runTaskTimer(m_coolearth, seconds*20L, seconds*20L);
    }

    private void setInfinite(double seconds, Material pair)  {
        closeRunnable(pair);
        m_spawners.put(pair, new BukkitRunnable() {
            public void run()
            {
                setItem(pair);
            }
        });
        m_spawners.get(pair).runTaskTimer(m_coolearth, (long) (seconds*20), (long) (seconds*20));
    }

    public void closeRunnable(Material pair) {
        BukkitRunnable runnable = m_spawners.get(pair);
        if (runnable != null) {
            runnable.cancel();
            m_spawners.remove(pair);
        }
    }

    private void setItem(Material material) {
        switch(material) {
            case EMERALD:
                spawnItemSmart(-27.5, 24, 31.5, material);
                spawnItemSmart(-27.5, 7, 31.5, material);
                break;
            case DIAMOND:
                spawnItemSmart(-1.5, 6,5.5, material);
                spawnItemSmart(-53.5, 6,57.5, material);
                spawnItemSmart(-53.5, 6,5.5, material);
                spawnItemSmart(-1.5, 6,57.5, material);
                break;
            default:
                throw new UnsupportedOperationException("Not valid material");
        }
    }

    private void spawnItemSmart(double x, double y, double z, Material material){
        int amount = 0;
        World world = Bukkit.getWorld("world");
        Location firstlocation = new Location(world, x, y, z);
        for (Item entity: world.getEntitiesByClass(Item.class)) {
            ItemStack itemStack = entity.getItemStack();
            if (material != itemStack.getType()) continue;
            if (!Util.locationsEqualIgnoringRot(firstlocation, entity.getLocation())) {
                continue;
            }
            amount++;
        }
        switch (material) {
            case EMERALD:
                if (amount >= 4) return;
                break;
            case DIAMOND:
                if (amount >= 8) return;
                break;
            default:
                throw new UnsupportedOperationException("Not a real item");
        }
        Util.spawnItem(firstlocation, material);
    }
}
