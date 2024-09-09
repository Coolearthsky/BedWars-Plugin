package me.coolearth.coolearth.listener;

import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.Constants;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.math.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Optional;

public class InventoryManager implements Listener {

    @EventHandler
    public void onInventoryDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!player.getScoreboardTags().contains("player") || !GlobalVariables.isGameActive()) return;
        ItemStack item = event.getItemDrop().getItemStack();
        Material type = item.getType();
        if (getNotDroppable(type)) {
            event.setCancelled(true);
            player.updateInventory();
        } else if (checkIfSpecialSword(type) && test(player, item)) {
            player.getInventory().addItem(getEnchanted(item));
        }
    }

    public ItemStack getEnchanted(ItemStack itemToCompareTo) {
        if (itemToCompareTo.getEnchantments().containsKey(Enchantment.SHARPNESS)) {
            return Util.createWithEnchantmentAndUnbreakable(Enchantment.SHARPNESS, Material.WOODEN_SWORD);
        } else {
            return Util.createWithUnbreakable(Material.WOODEN_SWORD);
        }
    }

    private boolean getNotDroppable(Material type) {
        return type == Material.SHEARS || type == Material.WOODEN_SWORD || type == Material.WOODEN_PICKAXE || type == Material.IRON_PICKAXE || type == Material.GOLDEN_PICKAXE || type == Material.DIAMOND_PICKAXE || type == Material.NETHERITE_PICKAXE || type == Material.WOODEN_AXE || type == Material.STONE_AXE || type == Material.GOLDEN_AXE || type == Material.DIAMOND_AXE || type == Material.NETHERITE_AXE;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        Player player = (Player) event.getWhoClicked();
        if (!player.getScoreboardTags().contains("player") || !GlobalVariables.isGameActive()) return;
        if (player.getInventory().firstEmpty() == -1) {
            if (!checkIfContainsSwordIgnoringSlot(player, Optional.of(event.getSlot())) && (event.isRightClick() || event.isLeftClick()) && (event.getCurrentItem().getType() == Material.WOODEN_SWORD || event.getCurrentItem().getType() == Material.STONE_SWORD || event.getCurrentItem().getType() == Material.DIAMOND_SWORD || event.getCurrentItem().getType() == Material.IRON_SWORD || event.getCurrentItem().getType() == Material.NETHERITE_SWORD) && (event.getCursor().getType() != Material.STONE_SWORD && event.getCursor().getType() != Material.IRON_SWORD && event.getCursor().getType() != Material.NETHERITE_SWORD && event.getCursor().getType() != Material.DIAMOND_SWORD && !event.getCursor().getType().isAir())) {
                event.setCancelled(true);
                player.updateInventory();
            }
        }
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
            player.updateInventory();
            return;
        }
        if (event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP) {
            ItemStack item = event.getCurrentItem();
            Material type = item.getType();
            if (getNotDroppable(type)) {
                event.setCancelled(true);
                player.updateInventory();
            }
        }
        if (event.getClick() == ClickType.SWAP_OFFHAND && event.getClickedInventory() != player.getInventory()) {
            PlayerInventory inventory = player.getInventory();
            Material type1 = inventory.getItemInOffHand().getType();
            boolean b = checkIfSpecialSword(type1);
            boolean b2 = checkIfSpecialSword(event.getCurrentItem().getType());
            if (getNotDroppable(inventory.getItemInOffHand().getType())) {
                event.setCancelled(true);
                player.updateInventory();
                if (b2 && inventory.getItemInOffHand().getType() == Material.WOODEN_SWORD) {
                    player.getInventory().setItemInOffHand(event.getCurrentItem());
                    event.setCurrentItem(new ItemStack(Material.AIR));
                }
                return;
            } else if (b) {
                if (!(b2 || checkIfContainsSwordIgnoringSlot(player, Optional.of(-1)))) {
                    if (player.getInventory().firstEmpty() != -1 || event.getCurrentItem().getType().isAir()) {
                        if (event.getCurrentItem().getType().isAir()) {
                            event.setCancelled(true);
                            player.updateInventory();
                            event.setCurrentItem(inventory.getItemInOffHand());
                            player.getInventory().setItemInOffHand(getEnchanted(inventory.getItemInOffHand()));
                        } else {
                            player.getInventory().addItem(getEnchanted(inventory.getItemInOffHand()));
                        }
                    } else {
                        event.setCancelled(true);
                        player.updateInventory();
                    }
                }
            } else {
                if (b2) {
                    Util.clearOfWoodSwords(player.getInventory());
                }
            }
        }
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && !(event.getView().getTopInventory() instanceof CraftingInventory)) {
            Material material = event.getCurrentItem().getType();
            boolean leavingPlayer =event.getRawSlot() != event.getSlot();
            if (checkIfSpecialSword(material)) {
                if (leavingPlayer) {
                    if (!checkIfContainsSwordIgnoringSlot(player, Optional.of(event.getSlot()))) {
                            event.setCancelled(true);
                            player.updateInventory();
                            event.getInventory().addItem(event.getCurrentItem());
                            player.getInventory().setItem(event.getSlot(),getEnchanted(event.getCurrentItem()));
                    }
                } else {
                    Util.clearOfWoodSwords(player.getInventory());
                }
            } else if (leavingPlayer && getNotDroppable(material)) {
                event.setCancelled(true);
                player.updateInventory();
            }
        }
        checkType(event, event.getCursor(), player, event.getClickedInventory() != player.getInventory());
            if (event.getClick() == ClickType.NUMBER_KEY && event.getClickedInventory() != player.getInventory()) {
                ItemStack item = player.getInventory().getItem(event.getHotbarButton());
                Material type;
                if (item == null) {
                    type = Material.AIR;
                } else {
                    type = item.getType();
                }
                Material type1 = event.getCurrentItem().getType();
                boolean b = checkIfSpecialSword(type1);
                if (getNotDroppable(type)) {
                    event.setCancelled(true);
                    player.updateInventory();
                    if (b && type == Material.WOODEN_SWORD) {
                        player.getInventory().setItem(event.getHotbarButton(), event.getCurrentItem());
                        event.setCurrentItem(new ItemStack(Material.AIR));
                    }
                } else if (item != null && checkIfSpecialSword(type)) {
                        if (!(b || checkIfContainsSwordIgnoringSlot(player, Optional.of(event.getHotbarButton())))) {
                            if (player.getInventory().firstEmpty() != -1 || event.getCurrentItem().getType().isAir()) {
                                if (event.getCurrentItem().getType().isAir()) {
                                    event.setCancelled(true);
                                    player.updateInventory();
                                    player.getInventory().setItem(event.getHotbarButton(), getEnchanted(item));
                                    event.setCurrentItem(item);
                                } else {
                                    player.getInventory().addItem(getEnchanted(item));
                                }
                            } else {
                                event.setCancelled(true);
                                player.updateInventory();
                            }
                        }
                } else {
                    if (b) {
                        Util.clearOfWoodSwords(player.getInventory());
                    }
                }
            }
    }

    private boolean checkIfSpecialSword(Material type1) {
        return type1 == Material.STONE_SWORD || type1 == Material.IRON_SWORD || type1 == Material.NETHERITE_SWORD || type1 == Material.DIAMOND_SWORD;
    }

    private boolean checkIfContainsSwordIgnoringSlot(Player player, Optional<Integer> optSlot) {
        if (optSlot.isPresent()) {
            int slot = optSlot.get();
            if (slot == -1) return Util.containsIgnoringOffhand(player.getInventory(), Material.NETHERITE_SWORD,Material.DIAMOND_SWORD,Material.IRON_SWORD,Material.STONE_SWORD,Material.WOODEN_SWORD);
            return Util.containsIgnoringSlot(player.getInventory(), slot, Material.NETHERITE_SWORD,Material.DIAMOND_SWORD,Material.IRON_SWORD,Material.STONE_SWORD,Material.WOODEN_SWORD);
        } else {
            return Util.contains(player.getInventory(), Material.NETHERITE_SWORD,Material.DIAMOND_SWORD,Material.IRON_SWORD,Material.STONE_SWORD,Material.WOODEN_SWORD);
        }
    }

    private void checkType(InventoryClickEvent event, ItemStack material, Player player, boolean leavingPlayer) {
        if (checkIfSpecialSword(material.getType())) {
            if (leavingPlayer) {
                if (!(checkIfContainsSwordIgnoringSlot(player, Optional.empty()))) {
                    player.getInventory().addItem(getEnchanted(material));
                }
            } else {
                Util.clearOfWoodSwords(player.getInventory());
            }
        } else if (leavingPlayer && getNotDroppable(material.getType())) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!player.getScoreboardTags().contains("player") || !GlobalVariables.isGameActive()) return;
        if (event.getInventory() != player.getInventory()) {
            Material material = event.getOldCursor().getType();
            if (getNotDroppable(material) || checkIfSpecialSword(material)) {
                event.setCancelled(true);
                player.updateInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (!player.getScoreboardTags().contains("player") || !GlobalVariables.isGameActive()) return;
        if (event.getInventory() != player.getInventory()) {
            Material material = player.getItemOnCursor().getType();
            if (checkIfSpecialSword(material)) {
                Util.clearOfWoodSwords(player.getInventory());
            }
        }
    }

    @EventHandler
    public void onInventoryPickup(EntityPickupItemEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) return;
        Player player = (Player) event.getEntity();
        if (!player.getScoreboardTags().contains("player") || !GlobalVariables.isGameActive()) return;
        ItemStack item = event.getItem().getItemStack();
        Material material = item.getType();
        if (checkIfSpecialSword(material)) {
            Util.clearOfWoodSwords(player.getInventory());
            if (player.getItemOnCursor().getType() == Material.WOODEN_SWORD) {
                player.setItemOnCursor(new ItemStack(Material.AIR));
            }
        } else if (material == Material.IRON_INGOT || material == Material.GOLD_INGOT || material == Material.EMERALD) {
            TeamUtil team = inGenerator(player.getLocation());
            if (team== TeamUtil.NONE) return;
            for (Player newPlayer : Bukkit.getOnlinePlayers()) {
                if (newPlayer == player) continue;
                if (inGenerator(newPlayer.getLocation(), team)) {
                    newPlayer.getInventory().addItem(item);
                }
            }
        }
    }

    /**
     * @param playerLocation Location of the player
     * @return The generator the player is in, returns NONE if player is not in a generator
     */
    private TeamUtil inGenerator(Location playerLocation) {
        World world = Bukkit.getWorld("world");
        if (MathUtil.isBetweenTwoLocations(playerLocation, new Location(world,42, 6,32), new Location(world,44, 9, 30))) return TeamUtil.RED;
        if (MathUtil.isBetweenTwoLocations(playerLocation, new Location(world, -27, 6, 102), new Location(world, -29, 9, 100))) return TeamUtil.YELLOW;
        if (MathUtil.isBetweenTwoLocations(playerLocation, new Location(world,-97, 6 ,30), new Location(world,-99 , 9,32))) return TeamUtil.GREEN;
        if (MathUtil.isBetweenTwoLocations(playerLocation, new Location(world,-27, 6, -39), new Location(world,-29 ,9, -41))) return TeamUtil.BLUE;
        return TeamUtil.NONE;
    }

    private boolean inGenerator(Location playerLocation, TeamUtil team) {
        Location spawnerLoc = Constants.getTeamGeneratorLocation(team);
        return MathUtil.isBetweenTwoLocations(playerLocation,spawnerLoc.add(-1.5,0,-1.5),spawnerLoc.add(1.5,3.5,1.5));
    }

    private boolean test(Player player, ItemStack droppedItem) {
        int totalCount = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            Material material = item.getType();
            if (checkIfSpecialSword(material)) {
                totalCount += item.getAmount();
                if (totalCount >= droppedItem.getAmount()) {
                    return false; // Player will still have at least one similar item after the drop
                }
            }
        }
        return true;
    }
}