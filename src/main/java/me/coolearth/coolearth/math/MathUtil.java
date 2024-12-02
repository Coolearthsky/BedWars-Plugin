package me.coolearth.coolearth.math;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MathUtil {

    public static boolean isBetweenTwoLocations(Location blockLocation, Location firstlocation, Location secondlocation) {
        List<Double> firstxyz = convertLocationToListWithoutRotation(firstlocation);
        List<Double> secondxyz = convertLocationToListWithoutRotation(secondlocation);
        List<Double> blockxyz = convertLocationToListWithoutRotation(blockLocation);
        for (int i = 0; i < 3; i++) {
            double firstcord = firstxyz.get(i);
            double secondcord = secondxyz.get(i);
            double blockcord = blockxyz.get(i);
            if (!isBetweenTwoDoubles(blockcord, firstcord, secondcord)) {
                return false;
            }
        }
        return true;
    }

    public static <T> List<T[]> splitArrayIntoSubarrays(T[] originalArray, int sizesOfNewArrays) {
        // Create a list to store the subarrays
        List<T[]> subarrays = new ArrayList<>();

        // Calculate the number of complete subarrays
        int completeSubarrays = originalArray.length / sizesOfNewArrays;

        // Calculate the number of remaining elements
        int remainingElements = originalArray.length % sizesOfNewArrays;

        // Split the array into subarrays
        for (int i = 0; i < completeSubarrays; i++) {
            // Create a subarray
            T[] subarray = Arrays.copyOfRange(originalArray, i * sizesOfNewArrays, (i + 1) * sizesOfNewArrays);
            subarrays.add(subarray);
        }

        // Handle the remaining elements if any
        if (remainingElements > 0) {
            T[] lastSubarray = Arrays.copyOfRange(originalArray,
                    completeSubarrays * sizesOfNewArrays,
                    completeSubarrays * sizesOfNewArrays + remainingElements
            );
            subarrays.add(lastSubarray);
        }

        return subarrays;
    }

    public static String convertToTime(int seconds) {
        int second = seconds % 60;
        int minutes = (seconds / 60);
        String zero = "";
        if (second < 10) {
            zero = "0";
        }
        return minutes + ":" + zero + second;
    }

    public static boolean isBetweenTwoDoubles(double num, double firstNum, double secondNum) {
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

    public static List<Double> convertLocationToListWithoutRotation(Location location) {
        List<Double> xyz = new ArrayList<>();
        xyz.add(location.getX());
        xyz.add(location.getY());
        xyz.add(location.getZ());
        return xyz;
    }
}
