package me.coolearth.coolearth.shops;

import me.coolearth.coolearth.Util.InventoryUtil;
import me.coolearth.coolearth.Util.Materials;
import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.math.MathUtil;
import me.coolearth.coolearth.menus.menuItems.Items;
import me.coolearth.coolearth.menus.menuItems.Traps;
import me.coolearth.coolearth.menus.menuItems.Upgrades;
import me.coolearth.coolearth.players.PlayerAddons;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.players.TeamInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ShopManager {
    private final JavaPlugin m_coolearth;
    public ShopManager(JavaPlugin coolearth) {
        m_coolearth = coolearth;
    }

    public void onInventoryPickup(Player player) {
        if (!player.getScoreboardTags().contains("player") || !GlobalVariables.isGameActive()) return;
        PlayerAddons playersInfo = PlayerInfo.getPlayersInfo(player);
        if (playersInfo == null) return;
        if (playersInfo.inMenu()) {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    playersInfo.update();

                }
            };
            runnable.runTaskLater(m_coolearth, 0);
        } else if (playersInfo.inUpgrades()) {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    PlayerInfo.getTeamInfo(playersInfo.getTeam()).createUpgradesForPlayer(player.getUniqueId());
                }
            };
            runnable.runTaskLater(m_coolearth, 0);
        }
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

    public void onShopItemClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        PlayerAddons playersInfo = PlayerInfo.getPlayersInfo(player);
        if (playersInfo == null) return;
        TeamInfo teamInfo = PlayerInfo.getTeamInfo(playersInfo.getTeam());
        if (playersInfo.inMenu()) {
            if (event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP) {
                if (event.getClickedInventory() != player.getInventory()) {
                    event.setCancelled(true);
                    return;
                }
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        playersInfo.update();
                    }
                };
                runnable.runTaskLater(m_coolearth, 0);
            }
        } else if (playersInfo.inUpgrades()) {
            if (event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP) {
                if (event.getClickedInventory() != player.getInventory()) {
                    event.setCancelled(true);
                    return;
                }
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        teamInfo.createUpgradesForPlayer(player.getUniqueId());
                    }
                };
                runnable.runTaskLater(m_coolearth, 0);
            }
        }
        if (event.getClickedInventory() == teamInfo.getUpgradesMenu(player.getUniqueId())) {
            if (event.getSlot() < 0) return;
            event.setCancelled(true);
            player.updateInventory();
            if (event.getAction() == InventoryAction.HOTBAR_SWAP) return;
            upgradeClickHandling(event.getCurrentItem(), player);
        } else if (playersInfo.m_shop.contains(event.getClickedInventory())) {
            int slot = event.getSlot();
            if (slot < 0) return;
            event.setCancelled(true);
            player.updateInventory();
            if (event.getAction() == InventoryAction.HOTBAR_SWAP) return;
            shopClickHandling(event.getCurrentItem(), slot, player);
        } else if ((event.getInventory() == teamInfo.getUpgradesMenu(player.getUniqueId())|| playersInfo.m_shop.contains(event.getInventory())) && event.isShiftClick()) {
            event.setCancelled(true);
            player.updateInventory();
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
                    noMoney(player, cost, inventory);
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
                    noMoney(player, cost, inventory);
                    return;
                }
                takeMoney(player, currentItem, cost, true);
                teamBased.upgrade(upgrades);
            }
        }
    }

    public void shopClickHandling(ItemStack currentItem, int slot, Player player) {
        PlayerAddons e = PlayerInfo.getPlayersInfo(player);
        if (e == null) return;
        if (MathUtil.isBetweenTwoDoubles(slot,0,8)) {
            e.openShopMenu(slot);
        } else if (MathUtil.isBetweenTwoDoubles(slot,9,17)) {
            e.openShopMenu(slot-9);
        } else {
            if (currentItem == null) return;
            Items items = Items.get(currentItem.getItemMeta().getItemName());
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
                noMoney(player, cost, inventory);
                return;
            }
            getRealItem(player, currentItem, items, cost);
        }
    }

    private static void noMoney(Player player, ItemStack cost, PlayerInventory inventory) {
        player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0.5f);
        int amount = cost.getAmount();
        for (ItemStack item : inventory.all(cost.getType()).values()) {
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
     * @param item The item you are buying
     * @param cost The cost of the item in ItemStack terms
     * //TODO MAKE THIS TAKE FROM OFFHAND
     */
    public void takeMoney(Player player, ItemStack item, ItemStack cost, boolean upgrade) {
        purchase(player, item, upgrade);
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

    private boolean isArmor(Items items) {
        return items == Items.PERMANENT_CHAINMAIL_ARMOR || items == Items.PERMANENT_IRON_ARMOR || items == Items.PERMANENT_DIAMOND_ARMOR || items == Items.PERMANENT_NETHERITE_ARMOR;
    }

    private boolean isSword(Items items) {
        return items == Items.STONE_SWORD || items == Items.IRON_SWORD || items == Items.DIAMOND_SWORD || items == Items.NETHERITE_SWORD;
    }

    private void getRealItem(Player player, ItemStack item, Items items, ItemStack realCost) {
        ItemStack itemStack = new ItemStack(item.getType(), item.getAmount());
        ItemMeta itemMeta = item.getItemMeta();
        ItemStack cost = new ItemStack(realCost.getType(), realCost.getAmount());
        ItemMeta newItemMeta = itemStack.getItemMeta();
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
            newPotionMeta.setDisplayName(potionMeta.getDisplayName());
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
            for (Enchantment enchantment: enchants.keySet()) {
                newItemMeta.addEnchant(enchantment, enchants.get(enchantment), true);
            }
        }
        PlayerInventory playerInventory = player.getInventory();
        if (InventoryUtil.checkIfReallyFull(playerInventory,cost,itemStack)) {
            inventoryFull(player);
            return;
        }
        takeMoney(player, item, cost, false);
        itemStack.setItemMeta(newItemMeta);
        player.getInventory().addItem(itemStack);
        PlayerInfo.getPlayersInfo(player).update();
    }

    private void purchase(Player player, ItemStack item, boolean upgrade) {
        player.sendMessage(ChatColor.GREEN + "You purchased " + ChatColor.GOLD + item.getItemMeta().getDisplayName().substring(2));
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
        if (!upgrade) return;
        for (UUID uuid : PlayerInfo.getTeamInfo(Util.getTeam(player)).getPeopleOnTeam().keySet()) {
            Player player2 = Bukkit.getPlayer(uuid);
            if (player2 == null) continue;
            if (player.equals(player2)) continue;
            player2.sendMessage(ChatColor.GREEN + player.getName() + " purchased " + ChatColor.GOLD + item.getItemMeta().getDisplayName().substring(2));
            player2.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
        }
    }

    private static void inventoryFull(Player player) {
        player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0.5f);
        player.sendMessage("Your inventory is full");
    }
}
