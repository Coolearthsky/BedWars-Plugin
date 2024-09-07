package me.coolearth.coolearth.math;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class MathUtil {

    public static boolean isBetweenTwoLocations(Location blockLocation, Location firstlocation, Location secondlocation) {
        List<Integer> firstxyz = convertLocationToListWithoutRotation(firstlocation);
        List<Integer> secondxyz = convertLocationToListWithoutRotation(secondlocation);
        List<Integer> blockxyz = convertLocationToListWithoutRotation(blockLocation);
        for (int i = 0; i < 3; i++) {
            int firstcord = firstxyz.get(i);
            int secondcord = secondxyz.get(i);
            int blockcord = blockxyz.get(i);
            if (!isBetweenTwoInts(blockcord, firstcord, secondcord)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBetweenTwoInts(int num, int firstNum, int secondNum) {
        if (firstNum<secondNum) {
            return firstNum <= num && num <= secondNum;
        } else {
            return firstNum >= num && num >= secondNum;
        }
    }

    public static Location getRelativeLocation(Location location,Location ofsetLocation, Rotation2d offset) {
        return getRelativeLocation(location.getWorld(), location.getX(), location.getY(), location.getZ(), ofsetLocation.getX(),ofsetLocation.getZ(), offset);
    }

    public static Location getRelativeLocation(World world, double x, double y, double z, double offsetX, double offsetZ, Rotation2d offset) {
        double z1 = z - offsetZ;
        double x1 = x - offsetX;
        Rotation2d newRot = new Rotation2d(z1, x1).rotateBy(offset);
        double hypot = Math.hypot(z1, x1);
        return new Location(world, offsetX + (int) Math.round(newRot.getSin() * hypot), y, offsetZ + (int) Math.round(newRot.getCos()* hypot));
    }

    public static List<Integer> convertLocationToListWithoutRotation(Location location) {
        List<Integer> xyz = new ArrayList<>();
        xyz.add(location.getBlockX());
        xyz.add(location.getBlockY());
        xyz.add(location.getBlockZ());
        return xyz;
    }
}
