package me.coolearth.coolearth.block;

import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.math.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Bed;

import java.util.ArrayList;

public class BlockManager {

    private final ArrayList<Location> playerPlacedBlockList =new ArrayList<>();
    private final ArrayList<ArrayList<Location>> noPlaceZones = new ArrayList<>();

    public BlockManager() {
        setNoPlaceZone(new Location(Bukkit.getWorld("world"), -86, 7, 36), new Location(Bukkit.getWorld("world"),-99, 11, 26));
        setNoPlaceZone(new Location(Bukkit.getWorld("world"), 31, 7, 36), new Location(Bukkit.getWorld("world"),44,11, 26));
        setNoPlaceZone(new Location(Bukkit.getWorld("world"), -33,7, 89), new Location(Bukkit.getWorld("world"),-23, 11, 102));
        setNoPlaceZone(new Location(Bukkit.getWorld("world"), -33, 11, -41), new Location(Bukkit.getWorld("world"),-23, 7, -28));
    }

    private void setNoPlaceZone(Location firstplace, Location secondplace) {
        ArrayList<Location> noPlaceZone= new ArrayList<>();
        noPlaceZone.add(firstplace);
        noPlaceZone.add(secondplace);
        noPlaceZones.add(noPlaceZone);
    }

    public void add(Block block) {
        add(block.getLocation());
    }

    public void add(int x, int y, int z) {
        playerPlacedBlockList.add(new Location(Bukkit.getWorld("world"),x,y,z));
    }

    public void add(Location location) {
        playerPlacedBlockList.add(location);
    }

    public void remove(Block block) {
        remove(block.getLocation());
    }

    public void remove(Location location) {
        playerPlacedBlockList.remove(location);
    }

    private void clearPlayerPlacedBlocks() {
        for (Location block : playerPlacedBlockList) {
            block.getWorld().setBlockData(block, Material.AIR.createBlockData());
        }
        playerPlacedBlockList.clear();
    }

    public void resetMap() {
        World world = Bukkit.getWorld("world");
        clearPlayerPlacedBlocks();
        setBed(world,-28, 7, 83, BlockFace.NORTH, Bed.Part.HEAD,Material.YELLOW_BED);
        setBed(world,-28, 7, 84, BlockFace.NORTH, Bed.Part.FOOT,Material.YELLOW_BED);
        setBed(world,25 ,7, 31, BlockFace.WEST, Bed.Part.HEAD,Material.RED_BED);
        setBed(world,26 ,7, 31, BlockFace.WEST, Bed.Part.FOOT,Material.RED_BED);
        setBed(world,-28, 7, -22, BlockFace.SOUTH, Bed.Part.HEAD,Material.BLUE_BED);
        setBed(world,-28, 7, -23, BlockFace.SOUTH, Bed.Part.FOOT,Material.BLUE_BED);
        setBed(world,-80, 7 ,31, BlockFace.EAST, Bed.Part.HEAD,Material.LIME_BED);
        setBed(world,-81, 7 ,31, BlockFace.EAST, Bed.Part.FOOT, Material.LIME_BED);
    }


    private void setBed(World world, double x, double y, double z, BlockFace face, Bed.Part part, Material material) {
        Bed bedBlockData = (Bed) material.createBlockData();
        bedBlockData.setPart(part);
        bedBlockData.setFacing(face);
        world.setBlockData(new Location(world,x, y, z), bedBlockData);
    }

    public boolean contains(Block block) {
        return contains(block.getLocation());
    }

    public boolean contains(Location location) {
        return playerPlacedBlockList.contains(location);
    }

    public boolean checkIfPlacable(Block block) {
        if (!Util.checkPlaceable(block.getType())) return false;
        return checkNoPlaceZones(block.getLocation());
    }

    public boolean checkIfPlacable(BlockState block) {
        if (!Util.checkPlaceable(block.getType())) return false;
        return checkNoPlaceZones(block.getLocation());
    }
    /**
     *
     * @param location location of placeableBlock
     * @return If you can place a block here
     */
    public boolean checkIfPlacable(Location location) {
        return checkIfPlacable(location.getBlock());
    }

    private boolean checkNoPlaceZones(Location location) {
        for (ArrayList<Location> loc : noPlaceZones) {
            if (MathUtil.isBetweenTwoLocations(location, loc.get(0), loc.get(1))) {
                return false;
            }
        }
        return true;
    }
}
