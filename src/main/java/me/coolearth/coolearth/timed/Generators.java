package me.coolearth.coolearth.timed;

import me.coolearth.coolearth.Util.Materials;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.Constants;
import me.coolearth.coolearth.math.RomanNumber;
import me.coolearth.coolearth.scoreboard.Board;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Generators {
    private final JavaPlugin m_coolearth;
    private final Board m_board;
    private BukkitRunnable m_spawners = null;
    private int seconds = 0;
    private Materials upgrade = Materials.DIAMOND;
    private int level = 0;
    private final Map<Materials, Integer> currentLevel = new HashMap<>();
    private double[] m_timeBetweenUpdatesMinutes = null;

    public Generators(JavaPlugin coolearth, Board board) {
        m_coolearth = coolearth;
        m_board = board;
    }

    public void start() {
        Map<Materials, List<Integer>> loopTimes = new HashMap<>();
        List<Integer> emerald = Arrays.asList(50,40,30);
        List<Integer> diamond = Arrays.asList(30,20,15);
        loopTimes.put(Materials.DIAMOND,diamond);
        loopTimes.put(Materials.EMERALD,emerald);
        setLoops(loopTimes,
                6,12,17,22);
    }

    public void resetAllLoops() {
        if (m_spawners == null) return;
        m_spawners.cancel();
        upgrade = Materials.DIAMOND;
        seconds = 0;
        currentLevel.clear();
        level = 0;
        m_timeBetweenUpdatesMinutes = null;
        m_spawners = null;
    }

    private void setLoops(Map<Materials, List<Integer>> loopTimes, double... timeBetweenUpdatesMinutes) {
        m_timeBetweenUpdatesMinutes = timeBetweenUpdatesMinutes;
        m_spawners = new BukkitRunnable() {
            {
                currentLevel.put(Materials.DIAMOND, 0);
                currentLevel.put(Materials.EMERALD, 0);
            }
            public void run()
            {
                seconds++;
                for (Materials materials:Constants.getGenMaterials()) {
                    if (seconds % loopTimes.get(materials).get(currentLevel.get(materials)) == 0) {
                        if (!Bukkit.getOnlinePlayers().isEmpty()) {
                            setItem(materials);
                        }
                    }
                }
                for (ArmorStand armorStand : Bukkit.getWorld("world").getEntitiesByClass(ArmorStand.class)) {
                    Set<String> scoreboardTags = armorStand.getScoreboardTags();
                    if (!scoreboardTags.contains("generator") || !scoreboardTags.contains("timed")) continue;
                    int time;
                    if (scoreboardTags.contains(Materials.DIAMOND.getName())) {
                        Integer i = loopTimes.get(Materials.DIAMOND).get(currentLevel.get(Materials.DIAMOND));
                        time = i - seconds % i;
                    } else {
                        Integer i = loopTimes.get(Materials.EMERALD).get(currentLevel.get(Materials.EMERALD));
                        time = i - seconds % i;
                    }
                    armorStand.setCustomName(ChatColor.YELLOW + "Spawns in " + ChatColor.RED + time + ChatColor.YELLOW + " seconds");
                }
                if (level >= 4) {
                    m_board.updateTime(null,0,0);
                    return;
                }
                if (seconds >= 60 * m_timeBetweenUpdatesMinutes[level]) {
                    level++;
                    if (upgrade.equals(Materials.DIAMOND)) {
                        int tempLevel = currentLevel.get(Materials.DIAMOND) + 1;
                        currentLevel.replace(Materials.DIAMOND, tempLevel);
                        for (ArmorStand armorStand : Bukkit.getWorld("world").getEntitiesByClass(ArmorStand.class)) {
                            Set<String> scoreboardTags = armorStand.getScoreboardTags();
                            if (!scoreboardTags.contains("generator") || !scoreboardTags.contains("tier") || !scoreboardTags.contains(Materials.DIAMOND.getName())) continue;
                            armorStand.setCustomName(ChatColor.YELLOW + "Tier " + ChatColor.RED + RomanNumber.toRoman(tempLevel + 1));
                        }
                        upgrade = Materials.EMERALD;
                    } else {
                        int tempLevel = currentLevel.get(Materials.EMERALD) + 1;
                        currentLevel.replace(Materials.EMERALD, tempLevel);
                        for (ArmorStand armorStand : Bukkit.getWorld("world").getEntitiesByClass(ArmorStand.class)) {
                            Set<String> scoreboardTags = armorStand.getScoreboardTags();
                            if (!scoreboardTags.contains("generator") || !scoreboardTags.contains("tier") || !scoreboardTags.contains(Materials.EMERALD.getName())) continue;
                            armorStand.setCustomName(ChatColor.YELLOW + "Tier " + ChatColor.RED + RomanNumber.toRoman(tempLevel + 1));
                        }
                        upgrade = Materials.DIAMOND;
                    }
                    if (level >= 4) {
                        m_board.updateTime(null,0,0);
                        return;
                    }
                }
                updateTime();
            }
        };
        m_spawners.runTaskTimer(m_coolearth, 20, 20);
    }

    public void updateTime() {
        m_board.updateTime(upgrade,(int) (60* m_timeBetweenUpdatesMinutes[level]) - seconds, currentLevel.get(upgrade) + 2);
    }

    public void updateSafe(Player player) {
        m_board.updatePlayersScoreboardSafe(player, upgrade,(int) (60* m_timeBetweenUpdatesMinutes[level]) - seconds, currentLevel.get(upgrade) + 2);
    }

    private void setItem(Materials material) {
        for (Location location: Constants.getGenLocations(material)) {
            spawnItemSmart(location, material.getMaterial());
        }
    }

    private void spawnItemSmart(Location location, Material material){
        Item item = Util.findItemStack(location, material);
        if (item == null) {
            Util.spawnItem(location, material);
            return;
        }
        ItemStack itemStack = item.getItemStack();
        switch (material) {
            case EMERALD:
                if (itemStack.getAmount() >= 4) return;
                break;
            case DIAMOND:
                if (itemStack.getAmount() >= 8) return;
                break;
            default:
                throw new UnsupportedOperationException("Not a real item");
        }
        item.setItemStack(new ItemStack(itemStack.getType(),itemStack.getAmount() + 1));
    }
}
