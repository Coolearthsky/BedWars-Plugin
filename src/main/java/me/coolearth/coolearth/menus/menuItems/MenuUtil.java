package me.coolearth.coolearth.menus.menuItems;

import com.comphenix.protocol.wrappers.Pair;
import me.coolearth.coolearth.Util.Materials;
import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MenuUtil {
    public static ItemStack getSeparator(boolean inMenu, String above, String lower) {
        if (inMenu) {
            return create2NamedThing(Material.LIME_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + "⬆ " + ChatColor.GRAY +above, ChatColor.DARK_GRAY + "⬇ " + ChatColor.GRAY + lower);
        }
        return create2NamedThing(Material.GRAY_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + "⬆ " + ChatColor.GRAY +above, ChatColor.DARK_GRAY + "⬇ " + ChatColor.GRAY + lower);
    }

    public static ItemStack create2NamedThing(ItemStack item, String above, String lower) {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(above);
        itemMeta.setLore(Collections.singletonList(lower));
        item.setItemMeta(itemMeta);
        return item;
    }

    public static void addToShop(Inventory inventory, int startpoint, ItemStack... items) {
        for (int i = startpoint; i < (startpoint + items.length); i++) {
            inventory.setItem(i, items[i-startpoint]);
        }
    }

    public static void playSuccessfulPurchase(Player player) {
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
    }

    public static void playUnsuccessfulPurchase(Player player) {
        player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0.5f);
    }

    public static ItemStack getWool(TeamUtil team, WoolState woolState) {
        Material material = Util.getWool(team);
        ChatColor color;
        switch (woolState) {
            case BEDS:
            case NOT_PURCHASABLE:
                color = ChatColor.RED;
                break;
            case BOUGHT:
            case PURCHASABLE:
                color = ChatColor.GREEN;
                break;
            default:
                throw new UnsupportedOperationException("Not a wool state");
        }
        String name = team.getName();
        if (woolState.equals(WoolState.BOUGHT)) {
            return setItemName(name, material,
                    color + "Track Team " + name,
                    ChatColor.GRAY + "Purchase tracking upgrade for your",
                    ChatColor.GRAY + "compass which will track each player",
                    ChatColor.GRAY + "on a specific team until you die.",
                    "",
                    ChatColor.GREEN + "UNLOCKED");
        }
        String message;
        switch (woolState) {
            case BEDS:
                message = ChatColor.RED + "Unlocks when all enemy beds are destroyed!";
                break;
            case NOT_PURCHASABLE:
                message = ChatColor.RED + "You don't have enough emeralds!";
                break;
            case PURCHASABLE:
                message = ChatColor.YELLOW + "Click to purchase!";
                break;
            default:
                throw new UnsupportedOperationException("Not a wool state");
        }
        return setItemName(name, material,
                color + "Track Team " + name,
                ChatColor.GRAY + "Purchase tracking upgrade for your",
                ChatColor.GRAY + "compass which will track each player",
                ChatColor.GRAY + "on a specific team until you die.",
                "",
                ChatColor.GRAY + "Cost: " + ChatColor.DARK_GREEN + "2 Emeralds",
                "",
                message);
    }

    public static int getMiddleCol() {
        return 4;
    }

    /**
     *
     * @param row The row in the inventory starting from 0
     * @param col The column in the inventory starting from 0
     * @return the int num for the Bukkit inventory api
     */
    public static int getInventoryNum(int row, int col) {
        return row * 9 + col;
    }

    public static ItemStack setItemName(String hiddenName, ItemStack itemStack, String... name) {
        if (name.length == 0) {
            throw new UnsupportedOperationException("No name given");
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name[0]);
        itemMeta.setItemName(hiddenName);
        if (name.length > 1) {
            List<String> lores = new ArrayList<>(Arrays.asList(name));
            lores.remove(0);
            itemMeta.setLore(lores);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack setItemName(Material material, String... name) {
        return setItemName(new ItemStack(material), name);
    }

    public static ItemStack setItemName(ItemStack item, String... name) {
        return setItemName("",item, name);
    }

    public static ItemStack setItemName(String hiddenName, Material material, String... name) {
        return setItemName(hiddenName, new ItemStack(material), name);
    }

    public String lastFourChars(String inputString) {
        if (inputString == null) {
            return "";
        }

        return inputString.length() <= 4
                ? inputString
                : inputString.substring(inputString.length() - 4);
    }

    public static void addToShop(Inventory inventory, int startpoint, Material... materials) {
        ItemStack[] itemStacks = new ItemStack[materials.length];
        for (int i = 0; i < (materials.length); i++) {
            itemStacks[i] = new ItemStack(materials[i]);
        }
        addToShop(inventory, startpoint, itemStacks);
    }

    public static ItemStack addNameAndLore(ItemStack item, String displayName, String realName, String... lores) {
        List<String> loreList = new ArrayList<>(Arrays.asList(lores));
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setItemName(realName);
        itemMeta.setDisplayName(displayName);
        itemMeta.setLore(loreList);
        item.setItemMeta(itemMeta);
        return item;
    }

    public static ItemStack create2NamedThing(Material material, String above, String lower) {
        return create2NamedThing(new ItemStack(material), above, lower);
    }

    private static ItemStack addNameAndLore(ItemStack item, String displayName, String realName, String firstLore, String secondLore, String... lores) {
        String[] loresArray = new String[lores.length + 2];
        loresArray[0] = firstLore;
        loresArray[1] = secondLore;
        System.arraycopy(lores, 0, loresArray, 2, lores.length);
        return addNameAndLore(item, displayName,realName, loresArray);
    }

    private static ItemStack addNameAndLore(ItemStack item, String displayName, String realName, String firstLore, String... lores) {
        String[] loresArray = new String[lores.length + 1];
        loresArray[0] = firstLore;
        System.arraycopy(lores, 0, loresArray, 1, lores.length);
        return addNameAndLore(item, displayName,realName, loresArray);
    }

    private static ItemStack addNameAndLoreUpgrade(ItemStack item, String displayName, String realName, String lastLore, String secondToLastLore, String... lores) {
        String[] loresArray = new String[lores.length + 2];
        loresArray[loresArray.length-1] = lastLore;
        loresArray[loresArray.length-2] = secondToLastLore;
        System.arraycopy(lores, 0, loresArray, 0, lores.length);
        return addNameAndLore(item, displayName,realName, loresArray);
    }

    private static ItemStack addNameAndLoreUpgrade(ItemStack item, String displayName, String realName, String[] lastLore, String secondToLastLore, String... lores) {
        String[] loresArray = new String[lores.length + lastLore.length + 1];
        System.arraycopy(lores, 0, loresArray, 0, lores.length);
        loresArray[lores.length] = secondToLastLore;
        for (int i = 0; i < lastLore.length; i++) {
            loresArray[lores.length+1+i] = lastLore[i];
        }
        return addNameAndLore(item, displayName, realName, loresArray);
    }

    public static ItemStack addNamesShopStyle(boolean itemInQuickBuy, boolean playerInQuickBuy, Boolean hasMoney, ItemStack item, String displayName, String realName, ItemStack cost, String... lores) {
        String s = "s";
        Material type = cost.getType();
        Materials materials = Materials.get(type);
        if (cost.getAmount() == 1 || !materials.getPlural()) s = "";
        int i = 0;
        for (String lore : lores) {
            lores[i] = ChatColor.GRAY + lore;
            i++;
        }
        int quickBuy = 1;
        if (itemInQuickBuy && !playerInQuickBuy) {
            quickBuy--;
        }
        String[] newLores = new String[lores.length + 2 + quickBuy];
        System.arraycopy(lores, 0, newLores, 0, lores.length);
        newLores[newLores.length - (2 + quickBuy)] = "";
        if (quickBuy == 1) {
            if (playerInQuickBuy) {
                newLores[newLores.length - 2] = Materials.DIAMOND.getColor() + "Sneak click to remove from Quick Buy";
            } else {
                newLores[newLores.length - 2] = Materials.DIAMOND.getColor() + "Sneak click to add to Quick Buy";
            }
        }
        if (hasMoney != null) {
            ChatColor color;
            if (hasMoney) {
                color = ChatColor.GREEN;
                newLores[newLores.length - 1] = ChatColor.YELLOW + "Click to purchase!";
            } else {
                color = ChatColor.RED;
                newLores[newLores.length - 1] = ChatColor.RED + "You don't have enough " + materials.getName() + s + "!";
            }
            return addNameAndLore(item, color + displayName, realName, ("§7Cost: " + materials.getColor() + cost.getAmount() + " " + materials.getName()) + s, "", newLores);
        } else {
            newLores[newLores.length - 1] = ChatColor.GREEN + "UNLOCKED";
            return addNameAndLore(item,  ChatColor.RED + displayName, realName, "", newLores);
        }
    }

    public static ItemStack addNamesUpgradeStyle(boolean g, boolean hasMoney, ItemStack item, String displayName, String realName, int cost, String... lores) {
        int i = 0;
        for (String lore : lores) {
            lores[i] = "§7" + lore;
            i++;
        }
        String s = "s";
        if (cost == 1) s = "";
        String[] lastLore = new String[3];
        lastLore[0] = "§7Cost: §b" + cost + " Diamond" + s;
        lastLore[1] = "";
        ChatColor color;
        if (g) {
            if (hasMoney) {
                color = ChatColor.YELLOW;
                lastLore[lastLore.length - 1] = ChatColor.YELLOW + "Click to purchase!";
            } else {
                color = ChatColor.RED;
                lastLore[lastLore.length - 1] = ChatColor.RED + "You don't have enough Diamonds!";
            }
        } else {
            color = ChatColor.GREEN;
            lastLore[lastLore.length - 1] = ChatColor.GREEN + "UNLOCKED";
        }
        return addNameAndLoreUpgrade(item, color + displayName, realName, lastLore, "" , lores);
    }

    public static ItemStack noTrapStyle(Material material, int num, int cost) {
        ItemStack item = new ItemStack(material, num);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RED + "Trap #" + num + ": No Trap!");
        String s = "s";
        if (cost == 1) s = "";
        itemMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "The " + getName(num) + " enemy to walk into your",
                ChatColor.GRAY + "base will trigger this trap!",
                "",
                ChatColor.GRAY + "Purchasing a trap will queue it here.",
                ChatColor.GRAY + "Its cost will scale based on the",
                ChatColor.GRAY + "number of traps queued.",
                "",
                ChatColor.GRAY + "Next trap: " + Materials.DIAMOND.getColor() + cost + " Diamond" + s));
        item.setItemMeta(itemMeta);
        return item;
    }

    public static ItemStack getCatagory(Material material, int placement) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        boolean in;
        switch(material) {
            case NETHER_STAR:
                in = placement == 0;
                itemMeta.setDisplayName("Quick Buy");
                break;
            case RED_TERRACOTTA:
            case YELLOW_TERRACOTTA:
            case LIME_TERRACOTTA:
            case BLUE_TERRACOTTA:
            case TERRACOTTA:
                in = placement == 1;
                itemMeta.setDisplayName("Blocks");
                break;
            case GOLDEN_SWORD:
                in = placement == 2;
                itemMeta.setDisplayName("Melee");
                break;
            case CHAINMAIL_BOOTS:
                in = placement == 3;
                itemMeta.setDisplayName("Armor");
                break;
            case STONE_PICKAXE:
                in = placement == 4;
                itemMeta.setDisplayName("Tools");
                break;
            case BOW:
                in = placement == 5;
                itemMeta.setDisplayName("Ranged");
                break;
            case BREWING_STAND:
                in = placement == 6;
                itemMeta.setDisplayName("Potions");
                break;
            case TNT:
                in = placement == 7;
                itemMeta.setDisplayName("Utility");
                break;
            case BEDROCK:
                in = (placement == 8);
                itemMeta.setDisplayName("Rotating Items");
                break;
            default:
                throw new UnsupportedOperationException("Not a material");
        }
        if (in) {
            itemMeta.setDisplayName(Materials.DIAMOND.getColor() + itemMeta.getDisplayName());
        } else {
            itemMeta.setDisplayName(ChatColor.GREEN + itemMeta.getDisplayName());
            itemMeta.setLore(Collections.singletonList(ChatColor.YELLOW + "Click to view!"));
        }
        item.setItemMeta(itemMeta);
        return item;
    }

    public static String getName(int num) {
        switch (num) {
            case 1:
                return "first";
            case 2:
                return "second";
            case 3:
                return "third";
            default:
                throw new UnsupportedOperationException("Not a name");
        }
    }

    public static ItemStack addNamesTrapStyle(boolean hasMoney, ItemStack item, String displayName, String realName, Optional<Integer> cost, String... lores) {
        int i = 0;
        for (String lore : lores) {
            lores[i] = "§7" + lore;
            i++;
        }
        if (!cost.isPresent()) {
            return addNameAndLoreUpgrade(item, ChatColor.RED + displayName, realName, ChatColor.RED + "Traps queue full!", "", lores);
        }
        String s = "s";
        if (cost.get() == 1) s = "";
        String[] lastLore = new String[3];
        lastLore[0] = "§7Cost: §b" + cost.get() + " Diamond" + s;
        lastLore[1] = "";
        ChatColor color;
        if (hasMoney) {
            color = ChatColor.YELLOW;
            lastLore[lastLore.length - 1] = ChatColor.YELLOW + "Click to purchase!";
        } else {
            color = ChatColor.RED;
            lastLore[lastLore.length - 1] = ChatColor.RED + "You don't have enough Diamonds!";
        }
        return addNameAndLoreUpgrade(item, color + displayName, realName, lastLore, "" , lores);
    }

    public static ItemStack addNamesUpgradeStyle(boolean hasMoney, ItemStack item, String displayName, String realName, Pair<Integer, int[]> cost, String... lores) {
        String[] costArray = new String[cost.getSecond().length + 2];
        for (int i = 0; i < cost.getSecond().length; i++) {
            String s = "s";
            if (cost.getSecond()[i] == 1) s = "";
            int view = i+1;
            String chatColor;
            if (cost.getFirst() <= i) {
                chatColor = "§7";
            } else {
                chatColor = ChatColor.GREEN.toString();
            }
            costArray[i] = (chatColor + "Tier " + view + ": §b" + cost.getSecond()[i] + " Diamond" + s);
        }
        int i = 0;
        for (String lore : lores) {
            lores[i] = "§7" + lore;
            i++;
        }
        costArray[costArray.length-2] = "";
        ChatColor color;
        if (cost.getFirst() != cost.getSecond().length) {
            if (hasMoney) {
                color = ChatColor.YELLOW;
                costArray[costArray.length - 1] = ChatColor.YELLOW + "Click to purchase!";
            } else {
                color = ChatColor.RED;
                costArray[costArray.length - 1] = ChatColor.RED + "You don't have enough Diamonds!";
            }
        } else {
            color = ChatColor.GREEN;
            costArray[costArray.length - 1] = ChatColor.GREEN + "UNLOCKED";
        }
        return addNameAndLoreUpgrade(item, color + displayName, realName, costArray, "" , lores);
    }
}
