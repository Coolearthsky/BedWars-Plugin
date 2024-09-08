package me.coolearth.coolearth.listener;

import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.math.MathUtil;
import me.coolearth.coolearth.Util.Team;
import me.coolearth.coolearth.menus.menuItems.Items;
import me.coolearth.coolearth.menus.menuItems.Traps;
import me.coolearth.coolearth.menus.menuItems.Upgrades;
import me.coolearth.coolearth.players.PlayerAddons;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.players.TeamInfo;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ShopListener implements Listener {
    private final PlayerInfo m_playerInfo;
    private final JavaPlugin m_coolearth;

    public ShopListener(PlayerInfo playerInfo, JavaPlugin coolearth) {
        m_playerInfo = playerInfo;
        m_coolearth = coolearth;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player1 = event.getPlayer();
        UUID player = player1.getUniqueId();
        Bukkit.getLogger().info(player1.getAddress().getAddress().getAddress().toString());
        Bukkit.getLogger().info(player1.getAddress().toString());
        if (!player1.getScoreboardTags().contains("player") || !GlobalVariables.isGameActive() || m_playerInfo.getPlayers().containsKey(player)) return;
        Team team = Util.getMostEmptyTeam(m_playerInfo);
        PlayerAddons value = new PlayerAddons(m_coolearth, team, player);
        m_playerInfo.getPlayers().put(player, value);
        m_playerInfo.getTeamInfo(team).getMap().put(player, value);
        Util.removeTeams(player1);
        player1.addScoreboardTag(team.getName());
        Util.setupPlayerFromStart(player1);
    }

    @EventHandler
    public void onShopClick(PlayerInteractEntityEvent event) {
        Entity rightClicked = event.getRightClicked();
        if (rightClicked.getType() != EntityType.VILLAGER) return;
        event.setCancelled(true);
        Set<String> tags = rightClicked.getScoreboardTags();
        Player player = event.getPlayer();
        if (tags.contains("shop")) {
            PlayerAddons addons = m_playerInfo.getPlayersInfo(player);
            if (addons == null) {
                return;
            }
            addons.openShopMenu(0);
        } else if (tags.contains("upgrades")) {
            TeamInfo teamInfo = m_playerInfo.getTeamInfo(Util.getTeam(player));
            if (teamInfo == null) {
                return;
            }
            teamInfo.openMenu(player);
        } else {
            throw new UnsupportedOperationException("Not a real shop");
        }
    }

    @EventHandler
    public void onShopClose(InventoryCloseEvent event) {
        PlayerAddons teamRelativeAddons = m_playerInfo.getPlayersInfo((Player) event.getPlayer());
        if (teamRelativeAddons == null) return;
        teamRelativeAddons.closeInventory();
    }

    @EventHandler
    public void onShopItemClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        PlayerAddons teamRelativeAddons = m_playerInfo.getPlayersInfo(player);
        if (teamRelativeAddons == null) return;
        if (event.getClickedInventory() == m_playerInfo.getTeamInfo(teamRelativeAddons.getTeam()).getUpgradesMenu()) {
            upgradeClickHandling(event, player);
        } else if (teamRelativeAddons.m_shop.contains(event.getClickedInventory())) {
            shopClickHandling(event, player);
        } else if ((event.getInventory() == m_playerInfo.getTeamInfo(teamRelativeAddons.getTeam()).getUpgradesMenu()|| teamRelativeAddons.m_shop.contains(event.getInventory())) && event.isShiftClick()) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    private void upgradeClickHandling(InventoryClickEvent event, Player player) {
        int slot = event.getSlot();
        if (slot < 0) return;
        event.setCancelled(true);
        player.updateInventory();
        if (event.getAction() == InventoryAction.HOTBAR_SWAP) return;
        if (event.getCurrentItem() == null) return;
        String name = event.getCurrentItem().getItemMeta().getItemName();
        Traps trap = Traps.get(name);
        Team team = Util.getTeam(player);
        Upgrades upgrades = Upgrades.get(name);
        TeamInfo teamBased = m_playerInfo.getTeamInfo(team);
        if (teamBased == null) throw new UnsupportedOperationException("No team info");
        if (trap != Traps.UNKNOWN) {
            if (teamBased.canGetTrap()) {
                ItemStack cost = teamBased.getItemStackCostTraps();
                if (!player.getInventory().contains(cost.getType(), cost.getAmount())) {
                    //TODO fix this to check offhand
                    player.sendMessage("You don't have the money");
                    return;
                }
                takeMoney(player.getInventory(), cost);
                teamBased.getTrap(trap);
            }
        } else
            if (upgrades != Upgrades.UNKNOWN) {
                if (!teamBased.upgradeMaxed(upgrades)) {
                    ItemStack cost = teamBased.getItemStackCost(upgrades);
                    if (!player.getInventory().contains(cost.getType(), cost.getAmount())) {
                        //TODO fix this to check offhand
                        player.sendMessage("You don't have the money");
                        return;
                    }
                    takeMoney(player.getInventory(), cost);
                    teamBased.upgrade(upgrades);
                }
            }
        }

    private void shopClickHandling(InventoryClickEvent event, Player player) {
        int slot = event.getSlot();
        if (slot < 0) return;
        event.setCancelled(true);
        player.updateInventory();
        if (event.getAction() == InventoryAction.HOTBAR_SWAP) return;
        PlayerAddons e = m_playerInfo.getPlayersInfo(player);
        if (e == null) return;
        if (MathUtil.isBetweenTwoInts(slot,0,8)) {
            e.openShopMenu(slot);
        } else if (MathUtil.isBetweenTwoInts(slot,9,17)) {
            e.openShopMenu(slot-9);
        } else {
            if (event.getCurrentItem() == null) return;
            Items items = Items.get(event.getCurrentItem().getItemMeta().getItemName());
            if (e.hasShears() && items == Items.PERMANENT_SHEARS) return;
            if (isArmor(items)) {
                if (!e.canSetArmor(items)) return;
            }
            PlayerAddons teamRelativeAddons = m_playerInfo.getPlayersInfo(player);
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
                player.sendMessage("You don't have the money");
                return;
            }
            getRealItem(player, event.getCurrentItem(), items, cost);
        }
    }

    /**
     *
     * @param inventory The player's inventory you are checking
     * @param cost The cost of the item in ItemStack terms
     * //TODO MAKE THIS TAKE FROM OFFHAND
     */
    public void takeMoney(PlayerInventory inventory, ItemStack cost) {
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
            m_playerInfo.getPlayersInfo(player).setLowerArmor(items);
            takeMoney(player.getInventory(), cost);
            return;
        }
        else if (isSword(items)) {
            Util.clearOfWoodSwords(player.getInventory());
        }
        else if (items == Items.AXE_UPGRADE) {
            if (!m_playerInfo.getPlayersInfo(player).getAxeLevel().isPresent() && checkIfReallyFull(player.getInventory(),cost,null)) {
                player.sendMessage("Your inventory is full");
                return;
            }
            if (!m_playerInfo.getPlayersInfo(player).upgradeAxe()) {
                takeMoney(player.getInventory(), cost);
                return;
            }
        }
        else if (items == Items.PERMANENT_SHEARS) {
            if (checkIfReallyFull(player.getInventory(),cost,null)) {
                player.sendMessage("Your inventory is full");
                return;
            }
            m_playerInfo.getPlayersInfo(player).gotShears();
        }
        else if (items == Items.PICKAXE_UPGRADE) {
            if (!m_playerInfo.getPlayersInfo(player).getPickaxeLevel().isPresent() && checkIfReallyFull(player.getInventory(),cost,null)) {
                player.sendMessage("Your inventory is full");
                return;
            }
            if (!m_playerInfo.getPlayersInfo(player).upgradePick())  {
                takeMoney(player.getInventory(), cost);
                return;
            }
        }
        else if (item.getType() == Material.POTION) {
            PotionMeta potionMeta = (PotionMeta) itemMeta;
            PotionMeta newPotionMeta = (PotionMeta) newItemMeta;
            newPotionMeta.setDisplayName(potionMeta.getDisplayName());
            newPotionMeta.addCustomEffect(potionMeta.getCustomEffects().get(0), true);
            newPotionMeta.setColor(potionMeta.getColor());
            itemStack.setItemMeta(newItemMeta);
            player.getInventory().addItem(itemStack);
            takeMoney(player.getInventory(), cost);
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
        if (checkIfReallyFull(playerInventory,cost,itemStack)) {
            player.sendMessage("Your inventory is full");
            return;
        }
        takeMoney(player.getInventory(), cost);
        itemStack.setItemMeta(newItemMeta);
        player.getInventory().addItem(itemStack);
    }

    public boolean checkIfReallyFull(PlayerInventory playerInventory, ItemStack cost, ItemStack realItem) {
        if (playerInventory.firstEmpty() > -1) {
            return false;
        }
        Material costType = cost.getType();
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
        HashMap<Integer, ? extends ItemStack> all = playerInventory.all(costType);
        for (ItemStack item : all.values()) {
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
}
