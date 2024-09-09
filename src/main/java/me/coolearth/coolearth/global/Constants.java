package me.coolearth.coolearth.global;

import com.comphenix.protocol.wrappers.Pair;
import me.coolearth.coolearth.Util.TeamUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.*;

public class Constants {
    private static final Map<Location, TeamUtil> chests = new HashMap<>();
    private static final Map<TeamUtil, Location> teamGenerator = new HashMap<>();
    private static final Map<TeamUtil, Pair<Location,Location>> bedLocations = new HashMap<>();
    private static final Map<Material,Set<Location>> generators = new HashMap<>();
    private static final Set<Pair<Location,Location>> noPlaceZones = new HashSet<>();
    private static final Map<TeamUtil, Pair<Location, Location>> baseLocations = new HashMap<>();
    private static final World world = Bukkit.getWorld("world");
    private static final Location spawn = new Location(world,-27.5,41, 31.5);

    static {
        //Diamond Gens
        generators.put(Material.DIAMOND,new HashSet<>(Arrays.asList(
            new Location(world, -1.5, 6, 5.5),
            new Location(world, -53.5, 6, 57.5),
            new Location(world, -53.5, 6, 5.5),
            new Location(world, -1.5, 6, 57.5))));

        //Emerald Gens
        generators.put(Material.EMERALD,new HashSet<>(Arrays.asList(
            new Location(world, -27.5, 24, 31.5),
            new Location(world, -27.5, 7, 31.5))));

        // Team chest locations
        chests.put(new Location(world,32,7 ,35), TeamUtil.RED);
        chests.put(new Location(world,-32,7 ,90), TeamUtil.YELLOW);
        chests.put(new Location(world,-87, 7,27), TeamUtil.GREEN);
        chests.put(new Location(world,-24, 7, -29), TeamUtil.BLUE);

        // Team generators
        teamGenerator.put(TeamUtil.RED,new Location(world, 43.5, 6.5, 31.5, 90, 0));
        teamGenerator.put(TeamUtil.YELLOW,new Location(world, -27.5, 6.5, 101.5, 180,0));
        teamGenerator.put(TeamUtil.GREEN,new Location(world,-97.5 ,6.5 ,31.5, 270,0));
        teamGenerator.put(TeamUtil.BLUE,new Location(world, -27.5 ,6.5 ,-39.5,0,0));

        // Bed Locations, first location is head of bed, second is the foot
        bedLocations.put(TeamUtil.RED,new Pair<>(new Location(world,25 ,7, 31),new Location(world,26 ,7, 31)));
        bedLocations.put(TeamUtil.YELLOW,new Pair<>(new Location(world,-28,7,83),new Location(world,-28, 7, 84)));
        bedLocations.put(TeamUtil.GREEN,new Pair<>(new Location(world,-80, 7 ,31),new Location(world,-81, 7 ,31)));
        bedLocations.put(TeamUtil.BLUE,new Pair<>(new Location(world,-28,7,-22),new Location(world,-28,7,-23)));

        // No place zones
        noPlaceZones.add(new Pair<>(new Location(world, -86, 7, 36), new Location(world,-99, 11, 26)));
        noPlaceZones.add(new Pair<>(new Location(world, 31, 7, 36), new Location(world,44,11, 26)));
        noPlaceZones.add(new Pair<>(new Location(world, -33,7, 89), new Location(world,-23, 11, 102)));
        noPlaceZones.add(new Pair<>(new Location(world, -33, 11, -41), new Location(world,-23, 7, -28)));

        // Base locations
        baseLocations.put(TeamUtil.RED, new Pair<>(new Location(world, 46, 16, 22), new Location(world, 21, -1, 40)));
        baseLocations.put(TeamUtil.YELLOW, new Pair<>(new Location(world, -37, -1, 79), new Location(world, -19, 16, 104)));
        baseLocations.put(TeamUtil.GREEN, new Pair<>(new Location(world, -76, -1, 22), new Location(world, -101, 16, 40)));
        baseLocations.put(TeamUtil.BLUE, new Pair<>(new Location(world, -37, -1, -18), new Location(world, -19, 16, -43)));
    }

    public static Location getSpawn() {
        return spawn;
    }

    public static Location getTeamGeneratorLocation(TeamUtil team) {
        return teamGenerator.get(team);
    }

    public static Set<Pair<Location, Location>> getNoPlaceZones() {
        return noPlaceZones;
    }

    public static Pair<Location,Location> getBedLocation(TeamUtil team) {
        return bedLocations.get(team);
    }

    public static Pair<Location,Location> getBaseLocations(TeamUtil team) {
        return baseLocations.get(team);
    }

    public static TeamUtil getChestTeam(Location location) {
        return chests.get(location);
    }

    public static Set<Location> getGenLocations(Material material) {
        return generators.get(material);
    }

    public static Map<Location, TeamUtil> getChests() {
        return chests;
    }
}