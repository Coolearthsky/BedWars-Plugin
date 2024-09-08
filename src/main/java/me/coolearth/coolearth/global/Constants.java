package me.coolearth.coolearth.global;

import me.coolearth.coolearth.Util.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final Map<Location, Team> chests = new HashMap<>();
    static {
        chests.put(new Location(Bukkit.getWorld("world"),32,7 ,35), Team.RED);
        chests.put(new Location(Bukkit.getWorld("world"),-32,7 ,90), Team.YELLOW);
        chests.put(new Location(Bukkit.getWorld("world"),-87, 7,27), Team.GREEN);
        chests.put(new Location(Bukkit.getWorld("world"),-24, 7, -29), Team.BLUE);
    }

    public static Team getChestTeam(Location location) {
        return chests.get(location);
    }
}
