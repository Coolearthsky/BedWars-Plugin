package me.coolearth.coolearth.shops;

import me.coolearth.coolearth.Util.InventoryUtil;
import me.coolearth.coolearth.Util.Materials;
import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.math.MathUtil;
import me.coolearth.coolearth.menus.menuItems.Items;
import me.coolearth.coolearth.menus.menuItems.MenuUtil;
import me.coolearth.coolearth.menus.menuItems.Traps;
import me.coolearth.coolearth.menus.menuItems.Upgrades;
import me.coolearth.coolearth.players.PlayerAddons;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.players.TeamInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ShopManager {
    private final JavaPlugin m_coolearth;
    public ShopManager(JavaPlugin coolearth) {
        m_coolearth = coolearth;
    }

    public void onInventoryPickup(Player player) {
        if (!player.getScoreboardTags().contains("player") || !GlobalVariables.isGameActive()) return;

        PlayerAddons playersInfo = PlayerInfo.getPlayersInfo(player);
        if (playersInfo == null) return;

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (playersInfo.inMenu()) {
                    playersInfo.update();
                } else if (playersInfo.inUpgrades()) {
                    PlayerInfo.getTeamInfo(playersInfo.getTeam()).createUpgradesForPlayer(player.getUniqueId());
                } else if (playersInfo.inTrackerMenu()) {
                    playersInfo.updateWoolState();
                }
            }
        };
        runnable.runTaskLater(m_coolearth, 0);
    }

    public void onShopClick(Set<String> tags, Player player) {
        if (tags.contains("shop")) {
            PlayerAddons addons = PlayerInfo.getPlayersInfo(player);
            if (addons == null) {
                return;
            }
            addons.update();
            addons.openShopMenu(0);
        } else if (tags.contains("upgrades")) {
            TeamInfo teamInfo = PlayerInfo.getTeamInfo(Util.getTeam(player));
            if (teamInfo == null) {
                return;
            }
            teamInfo.createUpgradesForPlayer(player.getUniqueId());
            teamInfo.openMenu(player);
        } else {
            throw new UnsupportedOperationException("Not a real shop");
        }
    }

    public void onShopClose(Player player) {
        PlayerAddons teamRelativeAddons = PlayerInfo.getPlayersInfo(player);
        if (teamRelativeAddons == null) return;
        teamRelativeAddons.closeInventory();
    }
    public void onShopOpen(Player player, Inventory inventory) {
        PlayerAddons teamRelativeAddons = PlayerInfo.getPlayersInfo(player);
        if (teamRelativeAddons == null) return;
        teamRelativeAddons.openInventory(inventory);
    }


    public void onShopItemClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        PlayerAddons playersInfo = PlayerInfo.getPlayersInfo(player);
        if (playersInfo == null) return;

        Runnable cancel = () -> event.setCancelled(true);
        InventoryAction actionType = event.getAction();
        TeamInfo teamInfo = PlayerInfo.getTeamInfo(playersInfo.getTeam());
        ClickType clickType = event.getClick();
        Inventory clickedInv = event.getClickedInventory();
        ItemStack cursorItem = event.getCursor();
        ItemStack currentItem = event.getCurrentItem();
        int slot = event.getSlot();
        Materials materials = null;
        if (cursorItem != null ) {
            materials = Materials.get(cursorItem.getType());
        }
        if (!Materials.UNKNOWN.equals(materials) || isDrop(playersInfo, clickType)) {
            if (clickedInv != player.getInventory() && isDrop(playersInfo, clickType)) {
                cancel.run();
                return;
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (playersInfo.inMenu()) {
                        playersInfo.update();
                    } else if (playersInfo.inUpgrades()) {
                        teamInfo.createUpgradesForPlayer(player.getUniqueId());
                    } else if (playersInfo.inTrackerMenu()) {
                        playersInfo.updateWoolState();
                    }
                }
            }.runTaskLater(m_coolearth, 0);
            return;
        }
        // Common inventory click handling
        if (slot < 0) return;
        // Handle specific menu clicks
        Runnable runnable;

        if (clickedInv == teamInfo.getUpgradesMenu(player.getUniqueId())) {
            runnable = () -> upgradeClickHandling(currentItem, player);
        } else if (playersInfo.m_shop.contains(clickedInv)) {
            runnable = () -> shopClickHandling(currentItem, slot, player, clickType);
        } else if (GlobalVariables.contains(clickedInv) || playersInfo.clickedCompassMenu(clickedInv)) {
            runnable = () -> compassMenu(currentItem, player);
        } else if (playersInfo.clickedQuickBuyMenu(clickedInv)) {
            runnable = () -> quickBuyClick(slot, player);
        } else {
            runnable = null;
        }

        if (runnable != null) {
            handleMenuClick(actionType, cancel, player, runnable);
        }
        // Handle shift-clicking into protected inventories
        if (isProtectedInventory(event.getInventory(), playersInfo, teamInfo, player) && event.isShiftClick()) {
            cancel.run();
            player.updateInventory();
        }
    }

    private void quickBuyClick(int slot, Player player) {
        PlayerAddons playerAddons = PlayerInfo.getPlayersInfo(player);
        if (playerAddons == null) return;
        if (slot == 4) return;
        MenuUtil.playSuccessfulPurchase(player);
        playerAddons.addToQuickBuy(slot);
        playerAddons.update();
        playerAddons.openShopMenu(0);
    }

    private boolean isProtectedInventory(Inventory inv, PlayerAddons playersInfo, TeamInfo teamInfo, Player player) {
        return GlobalVariables.contains(inv) || playersInfo.inTrackerMenu() || playersInfo.inQuickBuyMenu() ||
                inv == teamInfo.getUpgradesMenu(player.getUniqueId()) ||
                playersInfo.m_shop.contains(inv);
    }

    private boolean isDrop(PlayerAddons playersInfo, ClickType clickType) {
        return (playersInfo.inMenu() || playersInfo.inUpgrades() || playersInfo.inTrackerMenu()) &&
                (clickType == ClickType.DROP || clickType == ClickType.CONTROL_DROP);
    }

    // Helper methods
    private void handleMenuClick(InventoryAction actionType, Runnable cancel, Player player, Runnable action) {
        cancel.run();
        player.updateInventory();
        if (actionType != InventoryAction.HOTBAR_SWAP) {
            action.run();
        }
    }

    public void compassMenu(ItemStack currentItem, Player player) {
        if (currentItem == null) return;
        PlayerAddons playersInfo = PlayerInfo.getPlayersInfo(player);
        ItemMeta itemMeta = currentItem.getItemMeta();
        switch (currentItem.getType()) {
            case COMPASS:
                playersInfo.updateWoolState();
                playersInfo.openCompassMenu();
                break;
            case EMERALD:
                player.openInventory(GlobalVariables.getQuickCommunications());
                break;
            case ARROW:
                if (itemMeta == null) {
                    throw new UnsupportedOperationException("No item meta");
                }
                switch (itemMeta.getItemName()) {
                    case "shop":
                        PlayerInfo.getPlayersInfo(player).openShopMenu(0);
                        return;
                    case "tracker":
                        if (PlayerInfo.getTeamInfo(Util.getTeam(player)).numberOfPeopleOnTeam() == 1)  {
                            player.openInventory(GlobalVariables.getCompassMenu());
                        } else {
                            player.openInventory(GlobalVariables.getCompassMenuMult());
                        }
                        return;
                    default:
                        throw new UnsupportedOperationException("Not a known back item");
                }
            case RED_WOOL:
            case YELLOW_WOOL:
            case LIME_WOOL:
            case BLUE_WOOL:
                if (itemMeta == null) {
                    throw new UnsupportedOperationException("No item meta");
                }
                String itemName = itemMeta.getItemName();
                TeamUtil team = TeamUtil.get(itemName);
                if (team.equals(TeamUtil.NONE)) throw new UnsupportedOperationException("Not a team");
                if (playersInfo.trackingTeam(team)) return;
                if (!playersInfo.trackerPurchasable()) {
                    if (playersInfo.hasBeds()) {
                        MenuUtil.playUnsuccessfulPurchase(player);
                        player.sendMessage(ChatColor.RED + "Not all enemy beds are destroyed yet!");
                    } else {
                        noMoney(player, new ItemStack(Material.EMERALD, 2));
                    }
                    return;
                }
                playersInfo.addTeamToTracker(team);
                takeMoney(player, itemName + " Tracking", new ItemStack(Material.EMERALD, 2));
                player.sendMessage(ChatColor.RED + "You will lose ability to track this team when you die!");
                playersInfo.updateWoolState();
                break;
            default:
                throw new UnsupportedOperationException("Not a known item");
        }
    }

    public void upgradeClickHandling(ItemStack currentItem, Player player) {
        if (currentItem == null) return;
        String name = currentItem.getItemMeta().getItemName();
        Traps trap = Traps.get(name);
        TeamUtil team = Util.getTeam(player);
        Upgrades upgrades = Upgrades.get(name);
        TeamInfo teamBased = PlayerInfo.getTeamInfo(team);
        if (teamBased == null) throw new UnsupportedOperationException("No team info");
        if (trap != Traps.UNKNOWN) {
            if (teamBased.canGetTrap()) {
                PlayerInventory inventory = player.getInventory();
                ItemStack cost = teamBased.getItemStackCostTraps();
                if (!inventory.contains(cost.getType(), cost.getAmount())) {
                    //TODO fix this to check offhand
                    noMoney(player, cost);
                    return;
                }
                takeMoney(player, currentItem, cost, true);
                teamBased.getTrap(trap);
            }
        } else
        if (upgrades != Upgrades.UNKNOWN) {
            if (!teamBased.upgradeMaxed(upgrades)) {
                PlayerInventory inventory = player.getInventory();
                ItemStack cost = teamBased.getItemStackCost(upgrades);
                if (!inventory.contains(cost.getType(), cost.getAmount())) {
                    //TODO fix this to check offhand
                    noMoney(player, cost);
                    return;
                }
                takeMoney(player, currentItem, cost, true);
                teamBased.upgrade(upgrades);
            }
        }
    }

    public void shopClickHandling(ItemStack currentItem, int slot, Player player, ClickType clickType) {
        PlayerAddons e = PlayerInfo.getPlayersInfo(player);
        if (e == null || currentItem == null) return;
        if (currentItem.getType().equals(Material.RED_STAINED_GLASS_PANE)) {
            MenuUtil.playUnsuccessfulPurchase(player);
            player.sendMessage(ChatColor.RED + "This is an empty Quick Buy slot!\n" + Materials.DIAMOND.getColor() + "Sneak Click on an item in the rest of the shop to fill it!");
            return;
        }
        if (currentItem.getType().equals(Material.COMPASS)) {
            e.updateWoolState();
            e.openCompassMenuShop();
            return;
        }
        if (MathUtil.isBetweenTwoDoubles(slot,0,8)) {
            e.openShopMenu(slot);
        } else if (MathUtil.isBetweenTwoDoubles(slot,9,17)) {
            e.openShopMenu(slot-9);
        } else {
            Items items = Items.get(currentItem.getItemMeta().getItemName());
            if (clickType.equals(ClickType.SHIFT_LEFT)||clickType.equals(ClickType.SHIFT_RIGHT)) {
                //TODO make this also check if it is in the shop
                if (items.equals(Items.NOTHING) || items.equals(Items.UNKNOWN) || (e.quickBuyContains(Items.get(currentItem.getItemMeta().getItemName())) && e.getMenu().get() != 0)) return;
                e.startAddOrRemoveFromQuickBuy(player, items, slot);
                return;
            }
                if (e.hasShears() && items == Items.PERMANENT_SHEARS) return;
            if (isArmor(items)) {
                if (!e.canSetArmor(items)) return;
            }
            PlayerAddons teamRelativeAddons = PlayerInfo.getPlayersInfo(player);
            ItemStack cost;
            if (items == Items.AXE_UPGRADE) {
                if (teamRelativeAddons.getAxeLevel().isPresent()) {
                    if (teamRelativeAddons.getAxeLevel().get() == Material.NETHERITE_AXE) return;
                }
                cost = teamRelativeAddons.getAxeCost();
            } else
            if (items == Items.PICKAXE_UPGRADE) {
                if (teamRelativeAddons.getPickaxeLevel().isPresent()) {
                    if (teamRelativeAddons.getPickaxeLevel().get() == Material.NETHERITE_PICKAXE) return;
                }
                cost = teamRelativeAddons.getPickaxeCost();
            } else {
                cost = items.getFirstCost();
            }
            PlayerInventory inventory = player.getInventory();
            if (!inventory.contains(cost.getType(), cost.getAmount())) {
                //TODO fix this to check offhand
                noMoney(player, cost);
                return;
            }
            getRealItem(player, currentItem, items, cost);
        }
    }

    private static void noMoney(Player player, ItemStack cost) {
        MenuUtil.playUnsuccessfulPurchase(player);
        int amount = cost.getAmount();
        for (ItemStack item : player.getInventory().all(cost.getType()).values()) {
            amount -= item.getAmount();
        }
        Materials materials = Materials.get(cost.getType());
        String s = "";
        if (materials.getPlural()) {
            s = "s";
        }
        player.sendMessage(ChatColor.RED + "You don't have enough " + materials.getName() + s + "! Need " + amount + " more!");
    }

    /**
     * @param player The player you are checking
     * @param itemName The name of what you are buying
     * @param cost The cost of the item in ItemStack terms
     * @param broadcastToTeam If the purchase should be broadcast to the team
     * //TODO MAKE THIS TAKE FROM OFFHAND
     */
    public void takeMoney(Player player, String itemName, ItemStack cost, boolean broadcastToTeam) {
        purchase(player, itemName, broadcastToTeam);
        PlayerInventory inventory = player.getInventory();
        int amount = cost.getAmount();
        Map<Integer, ? extends ItemStack> itemList = inventory.all(cost.getType());
        for (int slots : itemList.keySet()) {
            if (itemList.get(slots) == null) continue;
            if (itemList.get(slots).getType() != cost.getType()) continue;
            int amount1 = itemList.get(slots).getAmount();
            if (amount1 > amount) {
                ItemStack stack = itemList.get(slots);
                stack.setAmount(amount1-amount);
                inventory.setItem(slots,stack);
                return;
            } else if (amount1 == amount){
                inventory.clear(slots);
                return;
            } else {
                inventory.clear(slots);
                amount = amount-amount1;
            }
        }
    }

    public void takeMoney(Player player, ItemStack item, ItemStack cost, boolean upgrade) {
        takeMoney(player, item.getItemMeta().getDisplayName().substring(2), cost, upgrade);
    }

    public void takeMoney(Player player, String itemName, ItemStack cost) {
        takeMoney(player, itemName, cost, false);
    }

    private boolean isArmor(Items items) {
        return items == Items.PERMANENT_CHAINMAIL_ARMOR || items == Items.PERMANENT_IRON_ARMOR || items == Items.PERMANENT_DIAMOND_ARMOR || items == Items.PERMANENT_NETHERITE_ARMOR;
    }

    private boolean isSword(Items items) {
        return items == Items.STONE_SWORD || items == Items.IRON_SWORD || items == Items.DIAMOND_SWORD || items == Items.NETHERITE_SWORD;
    }

    private void  getRealItem(Player player, ItemStack item, Items items, ItemStack realCost) {
        ItemStack itemStack = item.clone();
        ItemMeta itemMeta = item.getItemMeta();
        ItemStack cost = new ItemStack(realCost.getType(), realCost.getAmount());
        ItemMeta newItemMeta = itemStack.getItemMeta();
        newItemMeta.setLore(new ArrayList<>());
        if (isArmor(items)) {
            takeMoney(player, item, cost, false);
            PlayerInfo.getPlayersInfo(player).setLowerArmor(items);
            return;
        }
        else if (isSword(items)) {
            Util.clearOfWoodSwords(player.getInventory());
        }
        else if (items == Items.AXE_UPGRADE) {
            PlayerAddons playersInfo = PlayerInfo.getPlayersInfo(player);
            if (!playersInfo.isAxeUpgradeAble()) return;
            if (!playersInfo.getAxeLevel().isPresent() && InventoryUtil.checkIfReallyFull(player.getInventory(),cost,null)) {
                inventoryFull(player);
                return;
            }
            if (playersInfo.getAxeLevel().isPresent()) {
                Util.clear(player.getInventory(),playersInfo.getAxeLevel().get());
            }
            newItemMeta.setDisplayName(Materials.DIAMOND.getColor() + itemMeta.getDisplayName().substring(2));
            if (itemMeta.isUnbreakable()) {
                newItemMeta.setUnbreakable(true);
            }
            if (itemMeta.hasEnchants()) {
                Map<Enchantment, Integer> enchants = itemMeta.getEnchants();
                for (Enchantment enchantment: enchants.keySet()) {
                    newItemMeta.addEnchant(enchantment, enchants.get(enchantment), true);
                }
            }
            takeMoney(player, item, cost, false);
            itemStack.setItemMeta(newItemMeta);
            player.getInventory().addItem(itemStack);
            playersInfo.upgradeAxe();
            return;
        }
        else if (items == Items.PERMANENT_SHEARS) {
            if (InventoryUtil.checkIfReallyFull(player.getInventory(),cost,null)) {
                inventoryFull(player);
                return;
            }
            takeMoney(player, item, cost, false);
            PlayerInfo.getPlayersInfo(player).gotShears();
        }
        else if (items == Items.PICKAXE_UPGRADE) {
            PlayerAddons playersInfo = PlayerInfo.getPlayersInfo(player);
            if (!playersInfo.isPickaxeUpgradeAble()) return;
            if (!playersInfo.getPickaxeLevel().isPresent() && InventoryUtil.checkIfReallyFull(player.getInventory(),cost,null)) {
                inventoryFull(player);
                return;
            }
            if (playersInfo.getPickaxeLevel().isPresent()) {
                Util.clear(player.getInventory(),playersInfo.getPickaxeLevel().get());
            }
            newItemMeta.setDisplayName(Materials.DIAMOND.getColor() + itemMeta.getDisplayName().substring(2));
            if (itemMeta.isUnbreakable()) {
                newItemMeta.setUnbreakable(true);
            }
            if (itemMeta.hasEnchants()) {
                Map<Enchantment, Integer> enchants = itemMeta.getEnchants();
                for (Enchantment enchantment: enchants.keySet()) {
                    newItemMeta.addEnchant(enchantment, enchants.get(enchantment), true);
                }
            }
            takeMoney(player, item, cost, false);
            itemStack.setItemMeta(newItemMeta);
            player.getInventory().addItem(itemStack);
            playersInfo.upgradePick();
            return;
        }
        else if (item.getType() == Material.POTION) {
            PotionMeta potionMeta = (PotionMeta) itemMeta;
            PotionMeta newPotionMeta = (PotionMeta) newItemMeta;
            newPotionMeta.setDisplayName(ChatColor.WHITE + potionMeta.getDisplayName().substring(2));
            newPotionMeta.addCustomEffect(potionMeta.getCustomEffects().get(0), true);
            newPotionMeta.setColor(potionMeta.getColor());
            itemStack.setItemMeta(newItemMeta);
            player.getInventory().addItem(itemStack);
            takeMoney(player, item, cost, false);
            PlayerInfo.getPlayersInfo(player).update();
            return;
        }
        if (itemMeta.isUnbreakable()) {
            newItemMeta.setUnbreakable(true);
        }
        if (itemMeta.hasEnchants()) {
            Map<Enchantment, Integer> enchants = itemMeta.getEnchants();
            newItemMeta.setDisplayName(Materials.DIAMOND.getColor() + itemMeta.getDisplayName().substring(2));
            for (Enchantment enchantment: enchants.keySet()) {
                newItemMeta.addEnchant(enchantment, enchants.get(enchantment), true);
            }
        } else if (itemMeta.hasRarity()) {
            newItemMeta.setDisplayName(Materials.DIAMOND.getColor() + itemMeta.getDisplayName().substring(2));
        } else {
            newItemMeta.setDisplayName(ChatColor.WHITE + itemMeta.getDisplayName().substring(2));
        }
        PlayerInventory playerInventory = player.getInventory();
        if (InventoryUtil.checkIfReallyFull(playerInventory,cost,itemStack)) {
            inventoryFull(player);
            return;
        }
        takeMoney(player, item, cost, false);
        if (isBlock(itemStack.getType())) {
            itemStack = new ItemStack(itemStack.getType(), itemStack.getAmount());
        } else {
            itemStack.setItemMeta(newItemMeta);
        }
        player.getInventory().addItem(itemStack);
        PlayerInfo.getPlayersInfo(player).update();
    }

    private boolean isBlock(Material material) {
        return material.isBlock() && !material.equals(Material.CHEST) && !material.equals(Material.RED_STAINED_GLASS) && !material.equals(Material.YELLOW_STAINED_GLASS) && !material.equals(Material.LIME_STAINED_GLASS) && !material.equals(Material.BLUE_STAINED_GLASS);
    }

    private void purchase(Player player, String itemName, boolean broadcastToTeam) {
        player.sendMessage(ChatColor.GREEN + "You purchased " + ChatColor.GOLD + itemName);
        MenuUtil.playSuccessfulPurchase(player);
        if (!broadcastToTeam) return;
        for (UUID uuid : PlayerInfo.getTeamInfo(Util.getTeam(player)).getPeopleOnTeam().keySet()) {
            Player player2 = Bukkit.getPlayer(uuid);
            if (player2 == null) continue;
            if (player.equals(player2)) continue;
            player2.sendMessage(ChatColor.GREEN + player.getName() + " purchased " + ChatColor.GOLD + itemName);
            MenuUtil.playSuccessfulPurchase(player);
        }
    }

    private static void inventoryFull(Player player) {
        MenuUtil.playUnsuccessfulPurchase(player);
        player.sendMessage(ChatColor.RED + "Purchase Failed! Your inventory is full!");
    }
}
