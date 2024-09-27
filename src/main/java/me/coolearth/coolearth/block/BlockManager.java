package me.coolearth.coolearth.block;

import com.comphenix.protocol.wrappers.Pair;
import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.Constants;
import me.coolearth.coolearth.math.MathUtil;
import me.coolearth.coolearth.math.Rotation2d;
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
        clearPlayerPlacedBlocks();
        resetBeds();
    }

    public void resetBeds() {
        for (TeamUtil team : TeamUtil.values()) {
            if (team.equals(TeamUtil.NONE)) continue;
            resetBed(team);
        }
    }

    private void resetBed(TeamUtil team) {
        Pair<Location,Location> locations = Constants.getBedLocation(team);
        Location dif = locations.getFirst().clone().subtract(locations.getSecond());
        Rotation2d rot = new Rotation2d(dif.getZ(),-1.0 * dif.getX());
        BlockFace dir = Util.getDirectionMinecraft(rot.getDegrees());
        setBed(locations.getFirst(), dir, Bed.Part.HEAD, team.getBed());
        setBed(locations.getSecond(), dir, Bed.Part.FOOT, team.getBed());
    }

    private void setBed(World world, double x, double y, double z, BlockFace face, Bed.Part part, Material material) {
        setBed(new Location(world,x, y, z),face,part,material);
    }

    private void setBed(Location location, BlockFace face, Bed.Part part, Material material) {
        Bed bedBlockData = (Bed) material.createBlockData();
        bedBlockData.setPart(part);
        bedBlockData.setFacing(face);
        location.getWorld().setBlockData(location, bedBlockData);
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
        Pair<Location,Location> placeAble = Constants.getPlaceZone();
        if (!MathUtil.isBetweenTwoLocations(location, placeAble.getFirst(),placeAble.getSecond())) {
            return false;
        }
        for (Pair<Location,Location> loc : Constants.getNoPlaceZones()) {
            if (MathUtil.isBetweenTwoLocations(location, loc.getFirst(), loc.getSecond())) {
                return false;
            }
        }
        return true;
    }
}
