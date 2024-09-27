package me.coolearth.coolearth.Util;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public enum Materials {
    IRON(Material.IRON_INGOT, "Iron",ChatColor.WHITE.toString(), false),
    GOLD(Material.GOLD_INGOT, "Gold",ChatColor.GOLD.toString(), false),
    DIAMOND(Material.DIAMOND, "Diamond","§b", true),
    EMERALD(Material.EMERALD, "Emerald",ChatColor.DARK_GREEN.toString(), true),
    UNKNOWN(null, null, null, false);
    private final Material m_material;
    private final String m_name;
    private final String m_color;
    private final boolean m_plural;
    private final static Map<Material, Materials> materials = new HashMap<>();
    static {
        for (Materials i : Materials.values()) {
            materials.put(i.m_material, i);
        }
    }

    Materials(Material material, String name, String color, boolean plural) {
        m_material = material;
        m_name = name;
        m_color = color;
        m_plural = plural;
    }

    public Material getMaterial() {
        return m_material;
    }

    public String getName() {
        return m_name;
    }

    public String getColor() {
        return m_color;
    }

    public boolean getPlural() {
        return m_plural;
    }

    public static Materials get(Material material) {
        if (materials.containsKey(material)) {
            return materials.get(material);
        }
        return UNKNOWN;
    }
}
