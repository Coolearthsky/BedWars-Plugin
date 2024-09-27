package me.coolearth.coolearth.Util;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public enum TeamUtil {
    RED("Red", Color.RED, ChatColor.RED, Material.RED_BED),
    YELLOW("Yellow", Color.YELLOW, ChatColor.YELLOW, Material.YELLOW_BED),
    GREEN("Green", Color.LIME, ChatColor.GREEN, Material.LIME_BED),
    BLUE("Blue", Color.BLUE, ChatColor.BLUE, Material.BLUE_BED),
    NONE(null, null, null, null);
    private final String m_name;
    private final Color m_color;
    private final ChatColor m_chatColor;
    private final Material m_bed;
    private final static Map<String, TeamUtil> teams = new HashMap<>();
    static {
        for (TeamUtil i : TeamUtil.values()) {
            teams.put(i.m_name, i);
        }
    }
    TeamUtil(String name, Color color, ChatColor chatColor, Material bed) {
        m_name = name;
        m_color = color;
        m_bed = bed;
        m_chatColor = chatColor;
    }

    public Material getBed() {
        return m_bed;
    }

    public String getName() {
        return m_name;
    }

    public Color getColor() {
        return m_color;
    }

    public ChatColor getChatColor() {
        return m_chatColor;
    }

    public static TeamUtil get(String name) {
        if (teams.containsKey(name)) {
            return teams.get(name);
        }
        return NONE;
    }
}
