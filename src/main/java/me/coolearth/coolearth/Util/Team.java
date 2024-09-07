package me.coolearth.coolearth.Util;

import org.bukkit.Color;

import java.util.HashMap;
import java.util.Map;

public enum Team {
    RED("red", Color.RED),
    YELLOW("yellow", Color.YELLOW),
    GREEN("green", Color.LIME),
    BLUE("blue", Color.BLUE),
    NONE(null, null);
    private final String m_name;
    private final Color m_color;
    private final static Map<String, Team> teams = new HashMap<>();
    static {
        for (Team i : Team.values()) {
            teams.put(i.m_name, i);
        }
    }
    Team(String name, Color color) {
        m_name = name;
        m_color = color;
    }
    public String getName() {
        return m_name;
    }
    public Color getColor() {
        return m_color;
    }
    public static Team get(String name) {
        if (teams.containsKey(name)) {
            return teams.get(name);
        }
        return NONE;
    }
}
