package me.coolearth.coolearth.menus.menuItems;

import java.util.HashMap;
import java.util.Map;

public enum Upgrades {
    IRON_FORGE("upgradeForge", 4, 8, 16, 32),
    SHARPENED_SWORDS("sharpness", 8),
    REINFORCED_ARMOR("protection", 5, 8, 20, 32),
    MANIAC_MINER("maniacMiner", 4, 8),
    DRAGON_BUFF("dragonBuff", 8),
    HEAL_POOL("healPool", 3),
    UNKNOWN(null);
    String m_name;
    int[] m_costs;
    private final static Map<String, Upgrades> upgrades = new HashMap<>();
    static {
        for (Upgrades i : Upgrades.values()) {
            upgrades.put(i.m_name, i);
        }
    }
    Upgrades(String name, int... costs) {
        m_costs = costs;
        m_name = name;
    }
    public static Upgrades get(String name) {
        if (upgrades.containsKey(name)) {
            return upgrades.get(name);
        }
        return UNKNOWN;
    }

    public int getFirstCost() {
        return m_costs[0];
    }

    public int[] getCost() {
        return m_costs;
    }

    public String getName() {
        return m_name;
    }
}
