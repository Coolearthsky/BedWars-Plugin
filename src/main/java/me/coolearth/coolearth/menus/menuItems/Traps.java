package me.coolearth.coolearth.menus.menuItems;

import java.util.HashMap;
import java.util.Map;

public enum Traps {
    MINING_FATIGUE_TRAP("miningFatigueTrap"),
    BLINDNESS_TRAP("blindnessTrap"),
    COUNTER_OFFENSE_TRAP("counterOffenseTrap"),
    ALARM_TRAP("alarmTrap"),
    NO_TRAP("noTrap"),
    UNKNOWN(null);
    private final String m_name;
    private final static Map<String, Traps> traps = new HashMap<>();
    static {
        for (Traps i : Traps.values()) {
            traps.put(i.m_name, i);
        }
    }

    Traps(String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
    }
    public static Traps get(String name) {
        if (traps.containsKey(name)) {
            return traps.get(name);
        }
        return UNKNOWN;
    }
}
