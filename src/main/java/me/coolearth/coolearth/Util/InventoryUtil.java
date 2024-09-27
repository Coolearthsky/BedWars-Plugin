package me.coolearth.coolearth.Util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventoryUtil {

    public static boolean checkIfReallyFull(PlayerInventory playerInventory, ItemStack cost, ItemStack realItem) {
        if (!checkIfReallyFull(playerInventory, realItem)) return false;
        for (ItemStack item : playerInventory.all(cost.getType()).values()) {
            int amount = item.getAmount();
            int costAmount = cost.getAmount();
            if (amount <= costAmount) {
                playerInventory.clear(playerInventory.first(item));
                cost.setAmount(costAmount - amount);
                return false;
            }
        }
        return true;
    }

    public static boolean checkIfReallyFull(PlayerInventory playerInventory, ItemStack realItem) {
        if (playerInventory.firstEmpty() > -1) {
            return false;
        }
        if (realItem != null) {
            Material type = realItem.getType();
            if (playerInventory.contains(type)) {
                int initialAmount = realItem.getMaxStackSize();
                for (ItemStack item : playerInventory.all(type).values()) {
                    if (item.getAmount() <= initialAmount-realItem.getAmount()) {
                        return false;
                    } else {
                        initialAmount = item.getAmount();
                    }
                }
            }
        }
        return true;
    }
}
