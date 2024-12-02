package me.coolearth.coolearth.global;

import me.coolearth.coolearth.menus.menuItems.MenuUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class GlobalVariables {
    private static boolean isOn = false;
    private static Map<String, Inventory> m_compassMenus = new HashMap<>();
    static {
        final Inventory compassMenu = Bukkit.createInventory(null, 27, "Tracker & Communication");
        final Inventory compassMenuMultiplayer = Bukkit.createInventory(null, 27, "Tracker & Communication");
        final Inventory quickCommunications = Bukkit.createInventory(null, 45, "Quick Communications");
        m_compassMenus.put("compassMenu", compassMenu);
        m_compassMenus.put("compassMenuMultiplayer", compassMenuMultiplayer);
        m_compassMenus.put("quickCommunications", quickCommunications);
        ItemStack compass = MenuUtil.setItemName(Material.COMPASS,
                ChatColor.GREEN + "Tracker Shop",
                ChatColor.GRAY + "Purchase tracking upgrade for your",
                ChatColor.GRAY + "compass which will track each player",
                ChatColor.GRAY + "on a specific team until your die",
                "",
                ChatColor.YELLOW + "Click to open!");
        compassMenu.setItem(MenuUtil.getInventoryNum(1, MenuUtil.getMiddleCol()), compass);
        compassMenuMultiplayer.setItem(MenuUtil.getInventoryNum(1, 6), compass);
        compassMenuMultiplayer.setItem(MenuUtil.getInventoryNum(1, 2), MenuUtil.setItemName(Material.EMERALD,
                ChatColor.GREEN + "Quick Communications",
                ChatColor.GRAY + "Send highlighted chat messages to",
                ChatColor.GRAY + "your teammates!",
                "",
                ChatColor.YELLOW + "Click to open!"));
        quickCommunications.setItem(MenuUtil.getInventoryNum(4, MenuUtil.getMiddleCol()), MenuUtil.setItemName(Material.ARROW,
                ChatColor.GREEN + "Go Back",
                ChatColor.GRAY + "To Tracker & Communication"));

    }

    public static Inventory getCompassMenu() {
        return m_compassMenus.get("compassMenu");
    }

    public static Inventory getCompassMenuMult() {
        return m_compassMenus.get("compassMenuMultiplayer");
    }

    public static Inventory getQuickCommunications() {
        return m_compassMenus.get("quickCommunications");
    }

    public static boolean contains(Inventory inventory) {
        return m_compassMenus.containsValue(inventory);
    }

    public static void gameStarted() {
        isOn = true;
    }
    public static void gameEnded() {
        isOn = false;
    }
    public static boolean isGameActive() {
        return isOn;
    }
}
