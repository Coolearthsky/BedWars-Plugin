package me.coolearth.coolearth.players;

import com.comphenix.protocol.wrappers.Pair;
import me.coolearth.coolearth.PacketManager.ArmorPackets;
import me.coolearth.coolearth.Util.*;
import me.coolearth.coolearth.config.Config;
import me.coolearth.coolearth.global.Constants;
import me.coolearth.coolearth.math.MathUtil;
import me.coolearth.coolearth.menus.menuItems.Items;
import me.coolearth.coolearth.menus.menuItems.MenuUtil;
import me.coolearth.coolearth.menus.menuItems.WoolState;
import org.bukkit.*;
import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.List;

public class PlayerAddons {
    private TeamUtil m_team;
    private boolean m_alive;
    private boolean m_playerAlive;
    private boolean m_hasBed;
    public List<Inventory> m_shop = new ArrayList<>();
    private Optional<Material> m_pickaxeLevel = Optional.empty();
    private Optional<Material> m_axeLevel = Optional.empty();
    private Optional<Integer> m_currentShopMenu = Optional.empty();
    private int m_protectionLevel = 0;
    private boolean m_sharpness;
    private final JavaPlugin m_coolearth;
    private boolean m_shearsUpgrade;
    private boolean m_inUpgrades;
    private boolean m_inTracker;
    private boolean m_inQuickBuyMenu;
    private Material m_armor;
    private final UUID m_player;
    private int kills;
    private int finalKills;
    private int bedsBroken;
    private Optional<BukkitRunnable> m_ignoreTrap;
    private Optional<BukkitRunnable> m_onRespawn;
    private Optional<BukkitRunnable> m_tracker;
    private Set<TeamUtil> m_trackedTeams = new HashSet<>();
    private Inventory m_compassMenu;
    private Inventory m_compassShopMenu;
    private Inventory m_quickBuyMenu;
    private final List<Items> m_quickBuyItems;

    public PlayerAddons(JavaPlugin coolearth, TeamUtil team, UUID player) {
        m_team = team;
        m_coolearth = coolearth;
        m_player = player;
        m_playerAlive = true;
        m_protectionLevel = 0;
        kills = 0;
        bedsBroken = 0;
        m_inQuickBuyMenu = false;
        finalKills = 0;
        m_hasBed = true;
        m_alive = true;
        m_armor = Material.LEATHER_BOOTS;
        m_sharpness = false;
        m_shearsUpgrade = false;
        m_ignoreTrap = Optional.empty();
        m_inUpgrades = false;
        m_tracker = Optional.empty();
        m_quickBuyMenu = Bukkit.createInventory(Bukkit.getPlayer(player), 54, "Adding to Quick Buy...");
        for (int i = 0; i < 9; i++) {
            Inventory shop = Bukkit.createInventory(Bukkit.getPlayer(player), 54, "Shop");
            m_shop.add(shop);
        }
        m_quickBuyItems = Config.loadItemsFromYml(m_player, "quickBuyMenu");
        if (m_quickBuyItems.isEmpty()) {
            for (int i = 0; i < 21; i++) {
                m_quickBuyItems.add(Items.NOTHING);
            }
            saveQuickBuyMenuToDisk();
        }
        m_onRespawn = Optional.empty();
        createStore();
        m_currentShopMenu = Optional.empty();
        m_pickaxeLevel = Optional.empty();
        m_axeLevel = Optional.empty();
        createWoolNoBed();
    }

    private void saveQuickBuyMenuToDisk() {
        Config.saveItemsToYml(m_player, m_quickBuyItems, "quickBuyMenu");
    }

    public void enteredUpgrades() {
        m_inUpgrades = true;
    }

    public void openCompassMenu() {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return;
        player.openInventory(m_compassMenu);
        m_inTracker = true;
    }

    public void openCompassMenuShop() {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return;
        player.openInventory(m_compassShopMenu);
        m_inTracker = true;
    }

    public void updateWoolState() {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return;
        WoolState woolState = WoolState.get(player.getInventory().contains(Material.EMERALD, 2), hasBeds());
        updateWoolState(woolState);
    }

    public boolean hasBeds() {
        for (TeamInfo teamInfo : PlayerInfo.getTeams().values()) {
            if (teamInfo.getTeam().equals(m_team)) continue;
            if (teamInfo.hasBed()) {
                return true;
            }
        }
        return false;
    }

    public void resetTracker() {
        m_trackedTeams.clear();
        m_tracker.ifPresent(BukkitRunnable::cancel);
        m_tracker = Optional.empty();
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return;
        Util.sendActionBarMessage(player, "");
        player.setCompassTarget(Constants.getSpawn());
    }

    public void stopTrackingTeam(TeamUtil team) {
        m_trackedTeams.remove(team);
        if (m_trackedTeams.isEmpty()) {
            resetTracker();
        }
    }

    public boolean trackingTeam(TeamUtil team) {
        return m_trackedTeams.contains(team);
    }

    public boolean trackerPurchasable() {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return false;
        return !hasBeds() && player.getInventory().contains(Material.EMERALD, 2);
    }

    private void updateWoolState(WoolState woolState) {
        int i = 0;
        for (TeamUtil team : TeamUtil.values()) {
            if (team.equals(TeamUtil.NONE)) continue;
            if (!PlayerInfo.getTeamInfo(team).isAnyoneOnTeamAlive()) continue;
            if (team.equals(m_team)) {
                //TODO idk if this is necessary
                m_trackedTeams.remove(team);
                continue;
            }
            WoolState woolState1 = woolState;
            if (m_trackedTeams.contains(team)) {
                woolState1 = WoolState.BOUGHT;
            }
            m_compassMenu.setItem(MenuUtil.getInventoryNum(1, 2 + i * 2), MenuUtil.getWool(team, woolState1));
            m_compassShopMenu.setItem(MenuUtil.getInventoryNum(1, 2 + i * 2), MenuUtil.getWool(team, woolState1));
            i++;
        }
    }

    private void createWoolNoBed() {
        m_compassMenu = Bukkit.createInventory(Bukkit.getPlayer(m_player), 36, "Purchase Enemy Tracker");
        m_compassShopMenu = Bukkit.createInventory(Bukkit.getPlayer(m_player), 36, "Purchase Enemy Tracker");
        m_compassMenu.setItem(MenuUtil.getInventoryNum(3, MenuUtil.getMiddleCol()), MenuUtil.setItemName("tracker", Material.ARROW,
                ChatColor.GREEN + "Go Back",
                ChatColor.GRAY + "To Tracker & Communication"));
        m_compassShopMenu.setItem(MenuUtil.getInventoryNum(3, MenuUtil.getMiddleCol()), MenuUtil.setItemName("shop", Material.ARROW,
                ChatColor.GREEN + "Go Back",
                ChatColor.GRAY + "To Quick Buy"));
    }

    public int getKills() {
        return kills;
    }

    public void killedSomeone() {
        kills++;
    }

    public int getBedsBroken() {
        return bedsBroken;
    }

    public void bedBrokeSomeone() {
        bedsBroken++;
    }

    public int getFinalKills() {
        return finalKills;
    }

    public void finalKilledSomeone() {
        finalKills++;
    }

    public boolean inUpgrades() {
        return m_inUpgrades;
    }

    public boolean isAlive() {
        return m_alive;
    }

    public void bedBreak() {
        m_hasBed = false;
    }

    public void gotShears() {
        m_shearsUpgrade = true;
        menuOne(m_shop.get(0));
        menuFive(m_shop.get(4));
    }

    public void stopAllLoops() {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return;
        m_ignoreTrap.ifPresent(BukkitRunnable::cancel);
        m_tracker.ifPresent(BukkitRunnable::cancel);
        if (m_onRespawn.isPresent()) {
            m_onRespawn.get().cancel();
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(Constants.getTeamGeneratorLocation(m_team));
        }
    }

    public boolean ignoreTrap() {
        return m_ignoreTrap.isPresent();
    }

    public void drankMilk() {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return;
        player.sendMessage("You now have 60 seconds of trap immunity");
        m_ignoreTrap.ifPresent(BukkitRunnable::cancel);
        m_ignoreTrap = Optional.of(new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage("Your magic milk has worn off");
                deleteRunnable();
            }
        });
        m_ignoreTrap.get().runTaskLater(m_coolearth, 20 * 60);
    }

    private void deleteRunnable() {
        m_ignoreTrap.get().cancel();
        m_ignoreTrap = Optional.empty();
    }

    public boolean hasShears() {
        return m_shearsUpgrade;
    }

    public UUID getPlayer() {
        return m_player;
    }

    public void closeInventory() {
        m_currentShopMenu = Optional.empty();
        m_inTracker = false;
        m_inUpgrades = false;
        m_inQuickBuyMenu = false;
    }

    public void openInventory(Inventory inv) {
        if (inv.equals(m_compassMenu) || inv.equals(m_compassShopMenu)) {
            m_inTracker = true;
        } else if (inv.equals(m_quickBuyMenu)) {
            m_inQuickBuyMenu = true;
        } else if (m_shop.contains(inv)) {
            for (int i = 0; i < m_shop.size(); i++) {
                if (m_shop.get(i).equals(inv)) {
                    m_currentShopMenu = Optional.of(i);
                    return;
                }
            }
        }
    }

    public boolean clickedCompassMenu(Inventory inventory) {
        return m_compassMenu.equals(inventory) || m_compassShopMenu.equals(inventory);
    }

    public boolean clickedQuickBuyMenu(Inventory inventory) {
        return m_quickBuyMenu.equals(inventory);
    }

    public boolean inTrackerMenu() {
        return m_inTracker;
    }

    public boolean inQuickBuyMenu() {
        return m_inQuickBuyMenu;
    }

    public void addTeamToTracker(TeamUtil team) {
        m_trackedTeams.add(team);
        if (m_tracker.isPresent()) return;
        m_tracker = Optional.of(new BukkitRunnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayer(m_player);
                if (player == null) return;
                Pair<Player, Integer> nearest = getNearest();
                if (nearest == null) {
                    Util.sendActionBarMessage(player, ChatColor.RED + ChatColor.BOLD.toString() + "No players alive!");
                    return;
                }
                Player trackingPlayer = nearest.getFirst();
                double distance = nearest.getSecond();
                Util.sendActionBarMessage(player, ChatColor.WHITE + "Tracking: " + Util.getTeam(trackingPlayer).getChatColor() + ChatColor.BOLD + trackingPlayer.getName() + ChatColor.WHITE + " - Distance: " + ChatColor.GREEN + ChatColor.BOLD + (int) distance + "m");
                player.setCompassTarget(trackingPlayer.getLocation());
            }
        });
        m_tracker.get().runTaskTimer(m_coolearth, 0, 1);
    }

    public Pair<Player, Integer> getNearest() {
        double distance = Double.POSITIVE_INFINITY; // To make sure the first
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return null;
        Player target = null;
        for (Player e : Bukkit.getOnlinePlayers()) {
            if (e == player) continue; //Added this check so you don't target yourself.
            if (!m_trackedTeams.contains(Util.getTeam(e))) continue;
            double distanceto = player.getLocation().distance(e.getLocation());
            if (distanceto > distance)
                continue;
            distance = distanceto;
            target = e;
        }
        if (target == null) return null;
        return new Pair<>(target, (int) distance);
    }

    private void createStore() {
        for (int i = 0; i < 9; i++) {
            Player player = Bukkit.getPlayer(m_player);
            if (player == null) return;
            Inventory shop = Bukkit.createInventory(player, 54, "Shop");
            createShopMenu(shop, i);
            updateShop(i);
            m_shop.set(i, shop);
        }
    }

    public boolean inMenu() {
        return m_currentShopMenu.isPresent();
    }

    public Optional<Integer> getMenu() {
        return m_currentShopMenu;
    }

    public void setTeam(TeamUtil team, boolean hasBed) {
        m_team = team;
        m_hasBed = hasBed;
        createStore();
    }

    public TeamUtil getTeam() {
        return m_team;
    }

    public boolean canSetArmor(Items material) {
        switch (m_armor) {
            case LEATHER_BOOTS:
                if (material == Items.PERMANENT_CHAINMAIL_ARMOR) return true;
            case CHAINMAIL_BOOTS:
                if (material == Items.PERMANENT_IRON_ARMOR) return true;
            case IRON_BOOTS:
                if (material == Items.PERMANENT_DIAMOND_ARMOR) return true;
            case DIAMOND_BOOTS:
                if (material == Items.PERMANENT_NETHERITE_ARMOR) return true;
            case NETHERITE_BOOTS:
                return false;
            default:
                throw new UnsupportedOperationException("Not real armor set");
        }
    }

    public void setLowerArmor(Items items) {
        m_armor = getDisplayItem(items).getType();
        setLowerArmor(m_armor);
        menuOne(m_shop.get(0));
        menuFour(m_shop.get(3));
    }

    private void setLowerArmor(Material material) {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return;
        switch (material) {
            case LEATHER_BOOTS:
                player.getInventory().setLeggings(Util.setColor(getProt(Material.LEATHER_LEGGINGS), m_team.getColor()));
                break;
            case CHAINMAIL_BOOTS:
                player.getInventory().setLeggings(getProt(Material.CHAINMAIL_LEGGINGS));
                break;
            case IRON_BOOTS:
                player.getInventory().setLeggings(getProt(Material.IRON_LEGGINGS));
                break;
            case DIAMOND_BOOTS:
                player.getInventory().setLeggings(getProt(Material.DIAMOND_LEGGINGS));
                break;
            case NETHERITE_BOOTS:
                player.getInventory().setLeggings(getProt(Material.NETHERITE_LEGGINGS));
                break;
            default:
                throw new UnsupportedOperationException("Not real settable armor set");
        }
        if (material == Material.LEATHER_BOOTS) {
            player.getInventory().setBoots(Util.setColor(getProt(material), m_team.getColor()));
        } else {
            player.getInventory().setBoots(getProt(material));
        }
    }

    public ItemStack getAxeCost() {
        return getAxeInfo().getSecond();
    }

    public ItemStack getPickaxeCost() {
        return getPickaxeInfo().getSecond();
    }

    public void createShopMenu(Inventory inventory, int greenPlacement) {
        MenuUtil.addToShop(inventory, 0, MenuUtil.getCatagory(Material.NETHER_STAR, greenPlacement));
        switch (m_team) {
            case RED:
                MenuUtil.addToShop(inventory, 1, MenuUtil.getCatagory(Material.RED_TERRACOTTA, greenPlacement));
                break;
            case YELLOW:
                MenuUtil.addToShop(inventory, 1, MenuUtil.getCatagory(Material.YELLOW_TERRACOTTA, greenPlacement));
                break;
            case GREEN:
                MenuUtil.addToShop(inventory, 1, MenuUtil.getCatagory(Material.LIME_TERRACOTTA, greenPlacement));
                break;
            case BLUE:
                MenuUtil.addToShop(inventory, 1, MenuUtil.getCatagory(Material.BLUE_TERRACOTTA, greenPlacement));
                break;
            case NONE:
                MenuUtil.addToShop(inventory, 1, MenuUtil.getCatagory(Material.TERRACOTTA, greenPlacement));
                break;
            default:
                throw new UnsupportedOperationException("Invalid team");
        }
        MenuUtil.addToShop(inventory, 2,
                MenuUtil.getCatagory(Material.GOLDEN_SWORD, greenPlacement),
                MenuUtil.getCatagory(Material.CHAINMAIL_BOOTS, greenPlacement),
                MenuUtil.getCatagory(Material.STONE_PICKAXE, greenPlacement),
                MenuUtil.getCatagory(Material.BOW, greenPlacement),
                MenuUtil.getCatagory(Material.BREWING_STAND, greenPlacement),
                MenuUtil.getCatagory(Material.TNT, greenPlacement),
                MenuUtil.getCatagory(Material.BEDROCK, greenPlacement));
        for (int i = 9; i < 18; i++) {
            addToShop(inventory, i, false, Items.GRAY_PANE);
        }
        addToShop(inventory, greenPlacement + 9, false, Items.LIME_PANE);
    }
    public ItemStack getDisplayItem(Items material) {
        return getDisplayItem(material, Optional.empty());
    }


    public ItemStack getDisplayItem(Items material, Optional<Inventory> shopMenu) {
        Player player = Bukkit.getPlayer(m_player);
        ItemStack firstCost = null;
        if (material.getCosts().length > 0) {
            firstCost = material.getFirstCost();
        }
        PlayerInventory inventory = player.getInventory();
        boolean quickBuyContains = quickBuyContains(material);
        boolean inMenu = false;
        if (shopMenu.isPresent()) {
            inMenu = shopMenu.get().equals(m_shop.get(0));
        }
        switch (material) {
            case WOOL:
                String goodForBridging = "Cheap blocks, good for bridging.";
                int amount = 16;
                switch (m_team) {
                    case RED:
                        return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.RED_WOOL, amount), "Red Wool", material.getName() , firstCost, goodForBridging);
                    case YELLOW:
                        return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.YELLOW_WOOL, amount), "Yellow Wool", material.getName(), firstCost, goodForBridging);
                    case GREEN:
                        return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.LIME_WOOL, amount), "Green Wool", material.getName(), firstCost, goodForBridging);
                    case BLUE:
                        return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.BLUE_WOOL, amount), "Blue Wool", material.getName(), firstCost, goodForBridging);
                    case NONE:
                        return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.WHITE_WOOL, amount), "White Wool", material.getName(), firstCost, goodForBridging);
                    default:
                        throw new UnsupportedOperationException("Not a real team");
                }
            case WOOD:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.OAK_PLANKS,16), "Wood", material.getName(), firstCost, "Stronger bed defense then wool,", "But still pretty weak");
            case END_STONE:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.END_STONE,12), "End Stone", material.getName(), firstCost, "Strong bed defense, renders fireballs useless");
            case OBSIDIAN:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.OBSIDIAN,4), "Obsidian", material.getName(), firstCost, "The best bed defense, very expensive");
            case BLAST_PROOF_GLASS:
                switch (m_team) {
                    case RED:
                        return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.RED_STAINED_GLASS,4), "Blast Proof Glass", material.getName(), firstCost, "Renders fireballs and tnt useless," ,"but is very fragile to a player.");
                    case YELLOW:
                        return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.YELLOW_STAINED_GLASS,4), "Blast Proof Glass", material.getName(), firstCost, "Renders fireballs and tnt useless," ,"but is very fragile to a player.");
                    case GREEN:
                        return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.LIME_STAINED_GLASS,4), "Blast Proof Glass", material.getName(), firstCost, "Renders fireballs and tnt useless," ,"but is very fragile to a player.");
                    case BLUE:
                        return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.BLUE_STAINED_GLASS,4), "Blast Proof Glass", material.getName(), firstCost, "Renders fireballs and tnt useless," ,"but is very fragile to a player.");
                    case NONE:
                        return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.GLASS,4), "Blast Proof Glass", material.getName(), firstCost, "Renders fireballs and tnt useless," ,"but is very fragile to a player.");
                    default:
                        throw new UnsupportedOperationException("Not a real team");
                }
            case LADDERS:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.LADDER,8), "Ladders", material.getName(), firstCost, "Ladders, they help climb.");
            case TERRACOTTA:
                switch (m_team) {
                    case RED:
                        return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.RED_TERRACOTTA,16), "Terracotta", material.getName(), firstCost, "Strong yet quite cheap bed defense.");
                    case YELLOW:
                        return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.YELLOW_TERRACOTTA,16), "Terracotta", material.getName(), firstCost, "Strong yet quite cheap bed defense.");
                    case GREEN:
                        return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.LIME_TERRACOTTA,16), "Terracotta", material.getName(), firstCost, "Strong yet quite cheap bed defense.");
                    case BLUE:
                        return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.BLUE_TERRACOTTA,16), "Terracotta", material.getName(), firstCost, "Strong yet quite cheap bed defense.");
                    case NONE:
                        return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.TERRACOTTA,16), "Terracotta", material.getName(), firstCost, "Strong yet quite cheap bed defense.");
                    default:
                        throw new UnsupportedOperationException("Not a real team");
                }
            case STONE_SWORD:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),getSharp(Material.STONE_SWORD), "Stone Sword", material.getName(), firstCost, "Good value cheap sword.");
            case IRON_SWORD:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),getSharp(Material.IRON_SWORD), "Iron Sword", material.getName(), firstCost, "Slightly more expense but more powerful sword.");
            case DIAMOND_SWORD:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),getSharp(Material.DIAMOND_SWORD), "Diamond Sword", material.getName(), firstCost, "Very powerful endgame sword.");
            case NETHERITE_SWORD:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),getSharp(Material.NETHERITE_SWORD), "Netherite Sword", material.getName(), firstCost, "The last sword you will ever need.");
            case KNOCKBACK_STICK:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()), Util.createWithEnchantment(Enchantment.KNOCKBACK, Material.STICK), "Knockback Stick", material.getName(), firstCost, "Stick blessed with the power of knockback," , "use it to bully your enemies on bridges.");
            case PERMANENT_CHAINMAIL_ARMOR:
                Boolean s = null;
                if (canSetArmor(material)) s = inventory.contains(firstCost.getType(), firstCost.getAmount());
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,s,getProt(Material.CHAINMAIL_BOOTS), "Permanent Chainmail Armor", material.getName(), firstCost, "Permanent chainmail armor,","quite the bargain.");
            case PERMANENT_IRON_ARMOR:
                Boolean contains = null;
                if (canSetArmor(material)) contains = inventory.contains(firstCost.getType(), firstCost.getAmount());
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,contains,getProt(Material.IRON_BOOTS), "Permanent Iron Armor", material.getName(), firstCost, "Permanent iron armor, also","quite the bargain.");
            case PERMANENT_DIAMOND_ARMOR:
                Boolean d = null;
                if (canSetArmor(material)) d = inventory.contains(firstCost.getType(), firstCost.getAmount());
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,d,getProt(Material.DIAMOND_BOOTS), "Permanent Diamond Armor", material.getName(), firstCost, "Permanent diamond armor, very","powerful endgame armor.");
            case PERMANENT_NETHERITE_ARMOR:
                Boolean c = null;
                if (canSetArmor(material)) c = inventory.contains(firstCost.getType(), firstCost.getAmount());
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,c,getProt(Material.NETHERITE_BOOTS), "Permanent Netherite Armor", material.getName(), firstCost, "Permanent netherite armor,", "the final endgame armor.");
            case PERMANENT_SHEARS:
                Boolean a = null;
                if (!hasShears()) a = inventory.contains(firstCost.getType(), firstCost.getAmount());
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,a,Util.createWithUnbreakable(Material.SHEARS), "Permanent Shears", material.getName(), firstCost, "Permanent shears allowing you", "to always break wool", "faster than your enemies.");
            case PICKAXE_UPGRADE:
                Trio<ItemStack, ItemStack, String> pickaxe = getPickaxeInfo();
                Boolean containse1 = null;
                if (!m_pickaxeLevel.isPresent() || !m_pickaxeLevel.get().equals(Material.NETHERITE_PICKAXE)) containse1 = inventory.contains(pickaxe.getSecond().getType(), pickaxe.getSecond().getAmount());
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,containse1,pickaxe.getFirst(), pickaxe.getThird(), material.getName(), pickaxe.getSecond(), "Pickaxe upgrades, necessary for breaking","into opponents beds, the more you spend ","on pickaxes the faster your heists will be.");
            case AXE_UPGRADE:
                Trio<ItemStack, ItemStack, String> axe = getAxeInfo();
                Boolean contains1 = null;
                if (!m_axeLevel.isPresent() || !m_axeLevel.get().equals(Material.NETHERITE_AXE)) contains1 = inventory.contains(axe.getSecond().getType(), axe.getSecond().getAmount());
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,contains1,getSharp(axe.getFirst()), axe.getThird(), material.getName(), axe.getSecond(), "Axe upgrades, necessary are","sometimes necessary for","breaking into opponents beds,","they are useful mainly for", "wood defenses and damage.");
            case ARROW:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.ARROW,6), "Arrows", material.getName(), firstCost, "Arrows are a necessity if you want to buy bows", "as they are literally your ammo.");
            case REGULAR_BOW:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),Util.createWithUnbreakable(Material.BOW), "Bow", material.getName(), firstCost, "Base bow, still a powerful weapon for knocking your", "opponents off their bridges for a good price.");
            case POWER_BOW:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),Util.createWithEnchantmentAndUnbreakable(Enchantment.POWER,Material.BOW), "Power Bow", material.getName(), firstCost, "The more powerful bow, a good late game purchase.");
            case PUNCH_BOW:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),Util.addEnchantment(Enchantment.PUNCH,Util.createWithEnchantmentAndUnbreakable(Enchantment.POWER,Material.BOW)), "Punch Bow", material.getName(), firstCost, "The endgame bow, watch your opponents fly from a", "single hit with this bad boy.");
            case JUMP_BOOST_POTION:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),Util.createPotion(Color.LIME, PotionEffectType.JUMP_BOOST, 60*20, 4), "Jump Boost Potion", material.getName(), firstCost, "This potion will make you jump","over all your opponents defenses...", "for 60 seconds.");
            case SPEED_POTION:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),Util.createPotion(Color.YELLOW, PotionEffectType.SPEED, 60*20, 1), "Speed Potion", material.getName(), firstCost, "This potion will make you speed","right past all your opponents...", "for 60 seconds.");
            case INVISIBILTY_POTION:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),Util.createPotion(Color.WHITE, PotionEffectType.INVISIBILITY, 30*20, 0), "Invisibility Potion", material.getName(), firstCost, "This potion will make you sneak","right past all your opponents...", "for 30 seconds.");
            case GOLDEN_APPLE:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.GOLDEN_APPLE), "Golden Apple", material.getName(), firstCost, "Fantastic regen to get you out of a tough spot");
            case SILVERFISH_SNOWBALL:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.SNOWBALL), "Silverfish Snowball", material.getName(), firstCost, "A way to swarm your opponents with annoying critters");
            case IRON_GOLEM_SPAWN_EGG:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.IRON_GOLEM_SPAWN_EGG), "Iron Golem", material.getName(), firstCost, "Spawns a literal iron golem to defend you,","it does not get much better than that");
            case FIREBALL:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.FIRE_CHARGE), "Fireball", material.getName(), firstCost, "Shoots a ball which creates", "a large explosion on impact");
            case TNT:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.TNT), "TNT", material.getName(), firstCost, "Spawns an activated tnt on place");
            case ENDER_PEARL:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.ENDER_PEARL), "Ender Pearl", material.getName(), firstCost, "It's an ender pearl, it lets you","teleport where you throw it");
            case WATER_BUCKET:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.WATER_BUCKET), "Water Bucket", material.getName(), firstCost, "Lets you add water somewhere");
            case BRIDGE_EGG:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.EGG), "Bridge Egg", material.getName(), firstCost, "Creates a bridge where you throw it");
            case MAGIC_MILK:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.MILK_BUCKET), "Magic Milk", material.getName(), firstCost, "Lets you avoid enemy traps for 60 seconds");
            case SPONGE:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.SPONGE, 4), "Sponges", material.getName(), firstCost, "Soaks up water");
            case POP_OUT_BASE:
                return MenuUtil.addNamesShopStyle(quickBuyContains, inMenu,inventory.contains(firstCost.getType(), firstCost.getAmount()),new ItemStack(Material.CHEST), "Pop Out Base", material.getName(), firstCost, "Creates a mini-base where you place it");
            case LIME_PANE:
                return MenuUtil.getSeparator(true, "Categories","Items");
            case GRAY_PANE:
                return MenuUtil.getSeparator(false, "Categories","Items");
            case NOTHING:
                return MenuUtil.setItemName(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Empty Slot!", ChatColor.GRAY + "This is a Quick Buy Slot! " + Materials.DIAMOND.getColor() + "Sneak Click",ChatColor.GRAY +  "any item in the shop to add it here.");
            default:
                throw new UnsupportedOperationException("Not a bedwars item");
        }
    }

    public boolean quickBuyContains(Items material) {
        return m_quickBuyItems.contains(material);
    }

    public void upgradePick() {
        setPickLevel(getPickaxeToShow());
    }

    public Optional<Material> getPickaxeLevel() {
        return m_pickaxeLevel;
    }

    public Optional<Material> getAxeLevel() {
        return m_axeLevel;
    }

    public boolean isAxeUpgradeAble() {
        return m_axeLevel.map(material -> !material.equals(Material.NETHERITE_AXE)).orElse(true);
    }

    public boolean isPickaxeUpgradeAble() {
        return m_pickaxeLevel.map(material -> !material.equals(Material.NETHERITE_PICKAXE)).orElse(true);
    }

    public void upgradeProt() {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return;
        m_protectionLevel++;
        menuOne(m_shop.get(0));
        menuFour(m_shop.get(3));
        player.updateInventory();
        setFullArmorSet();
    }

    private void setPickLevel(Material material) {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) throw new UnsupportedOperationException("No player");
        m_pickaxeLevel = Optional.of(material);
        menuOne(m_shop.get(0));
        menuFive(m_shop.get(4));
        player.updateInventory();
    }

    public void onRespawn() {
        m_playerAlive = true;
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return;
        m_onRespawn.get().cancel();
        player.sendMessage(ChatColor.YELLOW + "You have respawned!");
        player.sendTitle(ChatColor.GREEN + "RESPAWNED!","",10,20*1,10);
        m_onRespawn = Optional.empty();
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        setFullArmorSet();
        player.teleport(Constants.getTeamGeneratorLocation(m_team));
        PlayerInventory inventory = player.getInventory();
        inventory.setItem(8, MenuUtil.setItemName(Material.COMPASS, ChatColor.GREEN + "Compass " + ChatColor.GRAY + "(Right Click)"));
        inventory.addItem(getSharp(Material.WOODEN_SWORD));
        getPickaxeLowerLevel().ifPresent(material -> addItemPickaxe(inventory, material));
        getAxeLowerLevel().ifPresent(material -> addItemAxe(inventory, material));
        if (m_shearsUpgrade) inventory.addItem(Util.createWithUnbreakable(Material.SHEARS));
    }

    private void addItemPickaxe(PlayerInventory inventory, Material material) {
        inventory.addItem(getPickaxe(material));
        m_pickaxeLevel = Optional.of(material);
        menuOne(m_shop.get(0));
        menuFive(m_shop.get(4));
    }

    public void startAddOrRemoveFromQuickBuy(Player player,Items item, int slot) {
        if (m_currentShopMenu.isPresent() && m_currentShopMenu.get() == 0) {
            m_quickBuyItems.set(convertMenuSlotToQuickBuy(slot),Items.NOTHING);
            saveQuickBuyMenuToDisk();
            MenuUtil.playSuccessfulPurchase(player);
            updateShop(0);
        } else {
            startAddToQuickBuy(item);
        }
    }

    public void startAddToQuickBuy(Items item) {
        updateQuickBuyMenu(item);
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return;
        m_inQuickBuyMenu = true;
        player.openInventory(m_quickBuyMenu);
    }

    public void addToQuickBuy(int slot) {
        m_quickBuyItems.set(convertMenuSlotToQuickBuy(slot), Items.get(m_quickBuyMenu.getItem(4).getItemMeta().getItemName()));
        saveQuickBuyMenuToDisk();
    }

    public int convertMenuSlotToQuickBuy(int slot) {
        int index = slot - 19;
        int div = index / 9;
        index -= (2 * div);
        return index;
    }

    public void updateQuickBuyMenu(Items item) {
        addToShopQuickBuy(m_quickBuyMenu, m_quickBuyItems);
        ItemStack displayItem = getDisplayItem(item);
        ItemMeta itemMeta = displayItem.getItemMeta();
        List<String> lore = itemMeta.getLore();
        if (lore.get(lore.size()-3).equals("")) {
            lore.remove(lore.size() - 1);
        }
        lore.set(lore.size()-1, ChatColor.YELLOW + "Adding item to Quick Slot!");
        itemMeta.setLore(lore);
        displayItem.setItemMeta(itemMeta);
        m_quickBuyMenu.setItem(MenuUtil.getInventoryNum(0, MenuUtil.getMiddleCol()), displayItem);
    }

    private void addItemAxe(PlayerInventory inventory, Material material) {
        inventory.addItem(getAxe(material));
        m_axeLevel = Optional.of(material);
        menuOne(m_shop.get(0));
        menuFive(m_shop.get(4));
    }

    public boolean getAlive() {
        return m_playerAlive;
    }

    public void onDeath() {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return;
        ArmorPackets.stopLoop(player);
        giveKillerItems(player);
        m_playerAlive = false;
        resetTracker();
        player.getInventory().clear();
        Util.clearEffects(player);
        player.setGameMode(GameMode.SPECTATOR);
        m_onRespawn.ifPresent(BukkitRunnable::cancel);
        if (!m_hasBed) {
            m_alive = false;
            return;
        }
        m_onRespawn = Optional.of(new BukkitRunnable() {
            @Override
            public void run() {
                onRespawn();
            }
        });
        m_onRespawn.get().runTaskLater(m_coolearth, 20*5);
    }

    private void giveKillerItems(Player player) {
        Player killer = player.getKiller();
        if (killer == null) return;
        if (killer.equals(player)) return;
        for (Materials material : Materials.values()) {
            if (material.equals(Materials.UNKNOWN)) continue;
            int amount = 0;
            for (ItemStack item : player.getInventory().all(material.getMaterial()).values()) {
                amount += item.getAmount();
                if (InventoryUtil.checkIfReallyFull(killer.getInventory(), item)) {
                    Util.spawnItem(killer.getLocation(), item);
                } else {
                    killer.getInventory().addItem(item);
                }
            }
            if (amount == 0) continue;
            if (amount == 1 || !material.getPlural()) {
                killer.sendMessage(ChatColor.GREEN + "+ " + material.getColor() + amount + " " + material.getName());
            } else {
                killer.sendMessage(ChatColor.GREEN + "+ " + material.getColor() + amount + " " + material.getName() + "s");
            }
        }
    }
    private void setFullArmorSet() {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return;
        player.getInventory().setHelmet(Util.setColor(getProt(Material.LEATHER_HELMET), m_team.getColor()));
        player.getInventory().setChestplate(Util.setColor(getProt(Material.LEATHER_CHESTPLATE), m_team.getColor()));
        setLowerArmor(m_armor);
    }



    private void setAxeLevel(Material material) {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) throw new UnsupportedOperationException("No player");
        m_axeLevel = Optional.of(material);
        menuOne(m_shop.get(0));
        menuFive(m_shop.get(4));
        player.updateInventory();
    }

    public void upgradeAxe() {
        setAxeLevel(getAxeToShow());
    }

    private Material getAxeToShow() {
        if (!m_axeLevel.isPresent()) return Material.WOODEN_AXE;
        switch (m_axeLevel.get()) {
            case WOODEN_AXE:
                return Material.STONE_AXE;
            case STONE_AXE:
                return Material.GOLDEN_AXE;
            case GOLDEN_AXE:
                return Material.DIAMOND_AXE;
            case DIAMOND_AXE:
            case NETHERITE_AXE:
                return Material.NETHERITE_AXE;
            default:
                throw new UnsupportedOperationException("Axe level is not real level");
        }
    }

    private Material    getPickaxeToShow() {
        if (!m_pickaxeLevel.isPresent()) return Material.WOODEN_PICKAXE;
        switch (m_pickaxeLevel.get()) {
            case WOODEN_PICKAXE:
                return Material.IRON_PICKAXE;
            case IRON_PICKAXE:
                return Material.GOLDEN_PICKAXE;
            case GOLDEN_PICKAXE:
                return Material.DIAMOND_PICKAXE;
            case DIAMOND_PICKAXE:
            case NETHERITE_PICKAXE:
                return Material.NETHERITE_PICKAXE;
            default:
                throw new UnsupportedOperationException("Axe level is not real level");
        }
    }

    public void openShopMenu(int menuNum) {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return;
        Optional<Integer> pastPane = m_currentShopMenu;
        if (pastPane.isPresent()) {
            if (pastPane.get() == menuNum) return;
        }
        m_currentShopMenu = Optional.of(menuNum);
        player.openInventory(m_shop.get(menuNum));
    }

    public void updateShop(int i) {
        Inventory inventory = m_shop.get(i);
        switch (i) {
            case 0:
                menuOne(inventory);
                break;
            case 1:
                menuTwo(inventory);
                break;
            case 2:
                menuThree(inventory);
                break;
            case 3:
                menuFour(inventory);
                break;
            case 4:
                menuFive(inventory);
                break;
            case 5:
                menuSix(inventory);
                break;
            case 6:
                menuSeven(inventory);
                break;
            case 7:
                menuEight(inventory);
                break;
            case 8:
                menuNine(inventory);
                break;
            default: throw new UnsupportedOperationException("Not a real menu");
        }
    }

    public void update() {
        for (int i = 0; i < 9; i++) {
            updateShop(i);
        }
    }

    private void menuOne(Inventory inventory) {
        addToShop(inventory, m_quickBuyItems);
        MenuUtil.addToShop(inventory, 45, MenuUtil.setItemName(Material.COMPASS,
                ChatColor.GREEN + "Tracker Shop",
                ChatColor.GRAY + "Purchase tracking upgrade for your",
                ChatColor.GRAY + "compass which will track each player",
                ChatColor.GRAY + "on a specific team until your die"));
    }

    private void menuTwo(Inventory inventory) {
        addToShop(inventory,
                Items.WOOL,
                Items.TERRACOTTA,
                Items.BLAST_PROOF_GLASS,
                Items.END_STONE,
                Items.LADDERS,
                Items.WOOD,
                Items.OBSIDIAN);
    }

    private void menuThree(Inventory inventory) {
        addToShop(inventory,
                Items.STONE_SWORD,
                Items.IRON_SWORD,
                Items.DIAMOND_SWORD,
                Items.NETHERITE_SWORD,
                Items.KNOCKBACK_STICK);
    }

    private void menuFour(Inventory inventory) {
        addToShop(inventory,
                Items.PERMANENT_CHAINMAIL_ARMOR,
                Items.PERMANENT_IRON_ARMOR,
                Items.PERMANENT_DIAMOND_ARMOR,
                Items.PERMANENT_NETHERITE_ARMOR);
    }

    private void menuFive(Inventory inventory) {
        addToShop(inventory,
                Items.PERMANENT_SHEARS,
                Items.PICKAXE_UPGRADE,
                Items.AXE_UPGRADE);
    }
    private void menuSix(Inventory inventory) {
        addToShop(inventory,
                Items.ARROW,
                Items.REGULAR_BOW,
                Items.POWER_BOW,
                Items.PUNCH_BOW);
    }
    private void menuSeven(Inventory inventory) {
        addToShop(inventory,
                Items.JUMP_BOOST_POTION,
                Items.SPEED_POTION,
                Items.INVISIBILTY_POTION);
    }
    private void menuEight(Inventory inventory) {
        addToShop(inventory,
                Items.GOLDEN_APPLE,
                Items.SILVERFISH_SNOWBALL,
                Items.IRON_GOLEM_SPAWN_EGG,
                Items.FIREBALL,
                Items.TNT,
                Items.ENDER_PEARL,
                Items.WATER_BUCKET,
                Items.BRIDGE_EGG,
                Items.MAGIC_MILK,
                Items.SPONGE,
                Items.POP_OUT_BASE);
    }
    private void menuNine(Inventory inventory) {}

    private ItemStack getSharp(Material material) {
        if (m_sharpness) {
            return Util.createWithEnchantmentAndUnbreakable(Enchantment.SHARPNESS,material);
        } else {
            return Util.createWithUnbreakable(material);
        }
    }

    private ItemStack getSharp(ItemStack material) {
        if (m_sharpness) {
            return Util.addEnchantment(Enchantment.SHARPNESS,material);
        } else {
            return material;
        }
    }

    public void gotSharpness() {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return;
        m_sharpness = true;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !canGetSharp(item.getType())) continue;
            Util.addEnchantment(Enchantment.SHARPNESS,item);
        }
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (canGetSharp(offhand.getType())){
            Util.addEnchantment(Enchantment.SHARPNESS,offhand);
        }
        for (ItemStack item : player.getEnderChest().getContents()) {
            if (item == null || !canGetSharp(item.getType())) continue;
            Util.addEnchantment(Enchantment.SHARPNESS,item);
        }
        menuOne(m_shop.get(0));
        menuThree(m_shop.get(2));
        menuFive(m_shop.get(4));
        player.updateInventory();
    }

    private boolean canGetSharp(Material material) {
        return material == Material.WOODEN_SWORD
                || material == Material.STONE_SWORD
                || material == Material.IRON_SWORD
                || material == Material.DIAMOND_SWORD
                || material == Material.NETHERITE_SWORD
                || material == Material.WOODEN_AXE
                || material == Material.STONE_AXE
                || material == Material.GOLDEN_AXE
                || material == Material.DIAMOND_AXE
                || material == Material.NETHERITE_AXE;
    }

    private ItemStack getProt(Material material) {
        if (m_protectionLevel == 0) {
            return Util.createWithUnbreakable(material);
        } else {
            return Util.createWithEnchantmentAndUnbreakable(Enchantment.PROTECTION, m_protectionLevel, material);
        }
    }

    private ItemStack getPickaxe(Material material) {
        switch (material) {
            case WOODEN_PICKAXE:
                return Util.createWithEnchantmentAndUnbreakable(material);
            case IRON_PICKAXE:
            case GOLDEN_PICKAXE:
                return Util.createWithEnchantmentAndUnbreakable(2,material);
            case DIAMOND_PICKAXE:
                return Util.createWithEnchantmentAndUnbreakable(3,material);
            case NETHERITE_PICKAXE:
                return Util.createWithEnchantmentAndUnbreakable(4,material);
            default: throw new UnsupportedOperationException("Not supported level");
        }
    }

    private Trio<ItemStack, ItemStack, String> getPickaxeInfo() {
        Material pickaxeToShow = getPickaxeToShow();
        switch (pickaxeToShow) {
            case WOODEN_PICKAXE:
                return new Trio<>(getPickaxe(pickaxeToShow), Items.PICKAXE_UPGRADE.getCost(0), "Wooden Pickaxe");
            case IRON_PICKAXE:
                return new Trio<>(getPickaxe(pickaxeToShow), Items.PICKAXE_UPGRADE.getCost(1), "Iron Pickaxe");
            case GOLDEN_PICKAXE:
                return new Trio<>(getPickaxe(pickaxeToShow), Items.PICKAXE_UPGRADE.getCost(2), "Golden Pickaxe");
            case DIAMOND_PICKAXE:
                return new Trio<>(getPickaxe(pickaxeToShow), Items.PICKAXE_UPGRADE.getCost(3), "Diamond Pickaxe");
            case NETHERITE_PICKAXE:
                return new Trio<>(getPickaxe(pickaxeToShow), Items.PICKAXE_UPGRADE.getCost(4), "Netherite Pickaxe");
            default: throw new UnsupportedOperationException("Not supported level");
        }
    }

    private ItemStack getAxe(Material material) {
        switch (material) {
            case WOODEN_AXE:
            case STONE_AXE:
                return getSharp(Util.createWithEnchantmentAndUnbreakable(material));
            case GOLDEN_AXE:
                return getSharp(Util.createWithEnchantmentAndUnbreakable(2,material));
            case DIAMOND_AXE:
                return getSharp(Util.createWithEnchantmentAndUnbreakable(3,material));
            case NETHERITE_AXE:
                return getSharp(Util.createWithEnchantmentAndUnbreakable(4,material));
            default: throw new UnsupportedOperationException("Not supported level");
        }
    }

    private Optional<Material> getAxeLowerLevel() {
        if (!m_axeLevel.isPresent()) return Optional.empty();
        switch (m_axeLevel.get()) {
            case WOODEN_AXE:
            case STONE_AXE:
                return Optional.of(Material.WOODEN_AXE);
            case GOLDEN_AXE:
                return Optional.of(Material.STONE_AXE);
            case DIAMOND_AXE:
                return Optional.of(Material.GOLDEN_AXE);
            case NETHERITE_AXE:
                return Optional.of(Material.DIAMOND_AXE);
            default:
                throw new UnsupportedOperationException("not a valid axe");
        }
    }
    private Optional<Material> getPickaxeLowerLevel() {
        if (!m_pickaxeLevel.isPresent()) return Optional.empty();
        switch (m_pickaxeLevel.get()) {
            case WOODEN_PICKAXE:
            case IRON_PICKAXE:
                return Optional.of(Material.WOODEN_PICKAXE);
            case GOLDEN_PICKAXE:
                return Optional.of(Material.IRON_PICKAXE);
            case DIAMOND_PICKAXE:
                return Optional.of(Material.GOLDEN_PICKAXE);
            case NETHERITE_PICKAXE:
                return Optional.of(Material.DIAMOND_PICKAXE);
            default:
                throw new UnsupportedOperationException("not a valid axe");
        }
    }


    private Trio<ItemStack, ItemStack, String> getAxeInfo() {
        Material axeToShow = getAxeToShow();
        switch (axeToShow) {
            case WOODEN_AXE:
                return new Trio<>(getAxe(axeToShow),Items.AXE_UPGRADE.getCost(0), "Wooden Axe");
            case STONE_AXE:
                return new Trio<>(getAxe(axeToShow),Items.AXE_UPGRADE.getCost(1), "Stone Axe");
            case GOLDEN_AXE:
                return new Trio<>(getAxe(axeToShow),Items.AXE_UPGRADE.getCost(2), "Golden Axe");
            case DIAMOND_AXE:
                return new Trio<>(getAxe(axeToShow),Items.AXE_UPGRADE.getCost(3), "Diamond Axe");
            case NETHERITE_AXE:
                return new Trio<>(getAxe(axeToShow),Items.AXE_UPGRADE.getCost(4), "Netherite Axe");
            default: throw new UnsupportedOperationException("Not supported level");
        }
    }

    private void addToShop(Inventory inventory, boolean quickBuy, Items... items) {
        List<Items[]> itemsListSplit = MathUtil.splitArrayIntoSubarrays(items, 7);
        int i = 0;
        for (Items[] itemsArray : itemsListSplit) {
            addToShop(inventory, 19+i*9,quickBuy, itemsArray);
            i++;
        }
    }

    private void addToShop(Inventory inventory, Items... items) {
        addToShop(inventory, false,items);
    }

    private void addToShop(Inventory inventory, List<Items> items) {
        addToShop(inventory, false, items.toArray(new Items[0]));
    }

    private void addToShopQuickBuy(Inventory inventory, List<Items> items) {
        addToShop(inventory, true, items.toArray(new Items[0]));
    }

    private void addToShop(Inventory inventory, int startpoint, boolean quickBuy, Items... items) {
        ItemStack[] itemStacks = new ItemStack[items.length];
        for (int i = 0; i < (items.length); i++) {
            itemStacks[i] = getDisplayItem(items[i], Optional.of(inventory));
            if (quickBuy) {
                MenuUtil.setItemName(itemStacks[i], itemStacks[i].getItemMeta().getDisplayName(), ChatColor.YELLOW + "Click to replace!");
            }
        }
        MenuUtil.addToShop(inventory, startpoint, itemStacks);
    }
}