package me.coolearth.coolearth.global;

import com.comphenix.protocol.wrappers.Pair;
import me.coolearth.coolearth.Util.Materials;
import me.coolearth.coolearth.Util.TeamUtil;
import org.bukkit.*;

import java.util.*;

public class Constants {
    private static final Map<Location, TeamUtil> chests = new HashMap<>();
    private static final Map<TeamUtil, Location> teamGenerator = new HashMap<>();
    private static final Map<TeamUtil, Location> shopLocations = new HashMap<>();
    private static final Map<TeamUtil, Location> upgradesLocations = new HashMap<>();
    private static final Map<TeamUtil, Pair<Location,Location>> bedLocations = new HashMap<>();
    private static final Map<Materials,Set<Location>> generators = new HashMap<>();
    private static final Set<Pair<Location,Location>> noPlaceZones = new HashSet<>();
    private static final Map<TeamUtil, Pair<Location, Location>> baseLocations = new HashMap<>();
    private static final World world = Bukkit.getWorld("world");
    private static final Pair<Location,Location> placeZone = new Pair<>(new Location(world, 46, -8, -43),new Location(world, -101,40, 104));
    private static final Location spawn = new Location(world,-27.5,41, 31.5);

    static {
        //Shops
        shopLocations.put(TeamUtil.RED,new Location(world, 37, 7 , 37,180,0));
        shopLocations.put(TeamUtil.YELLOW,new Location(world, -34, 7, 95,-90,0));
        shopLocations.put(TeamUtil.GREEN,new Location(world, -92,7, 25,0,0));
        shopLocations.put(TeamUtil.BLUE,new Location(world, -22, 7, -34,90,0));
        for (Location location : shopLocations.values()) {
            location.add(0.5, 0, 0.5);
        }

        //Upgrades
        upgradesLocations.put(TeamUtil.RED,new Location(world, 37, 7 , 25,0,0));
        upgradesLocations.put(TeamUtil.YELLOW,new Location(world, -22, 7, 95,90,0));
        upgradesLocations.put(TeamUtil.GREEN,new Location(world, -92,7, 37,180,0));
        upgradesLocations.put(TeamUtil.BLUE,new Location(world, -34, 7, -34,-90,0));
        for (Location location : upgradesLocations.values()) {
            location.add(0.5, 0, 0.5);
        }

        //Diamond Gens
        generators.put(Materials.DIAMOND,new HashSet<>(Arrays.asList(
            new Location(world, -1.5, 6, 5.5),
            new Location(world, -53.5, 6, 57.5),
            new Location(world, -53.5, 6, 5.5),
            new Location(world, -1.5, 6, 57.5))));

        //Emerald Gens
        generators.put(Materials.EMERALD,new HashSet<>(Arrays.asList(
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
        for (Set<Location> locations : generators.values()) {
            for (Location location : locations) {
                noPlaceZones.add(new Pair<>(location.clone().add(2.5,1,2.5), location.clone().add(-3.5,4,-3.5)));
            }
        }

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

    public static Location getShopLocation(TeamUtil team) {
        return shopLocations.get(team);
    }

    public static Location getUpgradeLocation(TeamUtil team) {
        return upgradesLocations.get(team);
    }

    public static Set<Pair<Location, Location>> getNoPlaceZones() {
        return noPlaceZones;
    }

    public static Pair<Location, Location> getPlaceZone() {
        return placeZone;
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

    public static Set<Location> getGenLocations(Materials material) {
        return generators.get(material);
    }

    public static Set<Materials> getGenMaterials() {
        return generators.keySet();
    }

    public static Map<Location, TeamUtil> getChests() {
        return chests;
    }
}