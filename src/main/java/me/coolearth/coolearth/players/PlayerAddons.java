package me.coolearth.coolearth.players;

import me.coolearth.coolearth.Util.Trio;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.Util.Team;
import me.coolearth.coolearth.menus.menuItems.Items;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerAddons {
    private Team m_team;
    private boolean m_alive;
    private boolean m_hasBed;
    public List<Inventory> m_shop = new ArrayList<>();
    private Optional<Material> m_pickaxeLevel = Optional.empty();
    private Optional<Material> m_axeLevel = Optional.empty();
    private Optional<Integer> m_currentShopMenu = Optional.empty();
    private int m_protectionLevel = 0;
    private boolean m_sharpness;
    private final JavaPlugin m_coolearth;
    private boolean m_shearsUpgrade;
    private Material m_armor;
    private final UUID m_player;
    private Optional<BukkitRunnable> m_ignoreTrap;
    private Optional<BukkitRunnable> m_deathCheck;
    private Optional<BukkitRunnable> m_onRespawn;

    public PlayerAddons(JavaPlugin coolearth, Team team, UUID player) {
        m_team = team;
        m_coolearth = coolearth;
        m_player = player;
        m_protectionLevel = 0;
        m_hasBed = true;
        m_alive = true;
        m_armor = Material.LEATHER_BOOTS;
        m_sharpness = false;
        m_shearsUpgrade = false;
        m_ignoreTrap = Optional.empty();
        for (int i = 0; i < 9; i++) {
            Inventory shop = Bukkit.createInventory(Bukkit.getPlayer(player), 54, "Shop");
            m_shop.add(shop);
        }
        m_onRespawn = Optional.empty();
        createStore(m_shop);
        m_currentShopMenu = Optional.empty();
        m_pickaxeLevel = Optional.empty();
        m_axeLevel = Optional.empty();
        deathCheck();
    }

    public boolean isAlive() {
        return m_alive;
    }

    private void deathCheck() {
        m_deathCheck= Optional.of(new BukkitRunnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayer(m_player);
                if (player == null) return;
                if (!player.getScoreboardTags().contains("player")) return;
                if (player.getLocation().getY() < -40 && player.getGameMode() != GameMode.SPECTATOR) {
                    player.teleport(Util.getSpawn(player.getLocation().getWorld()));
                    onDeath();
                }
            }
        });
        m_deathCheck.get().runTaskTimer(m_coolearth, 0,0);
    }

    public void bedBreak() {
        Bukkit.getLogger().info("BRO");
        m_hasBed = false;
    }

    public void gotShears() {
        m_shearsUpgrade = true;
    }

    public void stopAllLoops(){
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return;
        m_ignoreTrap.ifPresent(BukkitRunnable::cancel);
        m_deathCheck.ifPresent(BukkitRunnable::cancel);
        if (m_onRespawn.isPresent()) {
            m_onRespawn.get().cancel();
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(Util.getSpawnerLocation(m_team));
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
        m_ignoreTrap.get().runTaskLater(m_coolearth,20*60);
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
    }

    private void createStore(List<Inventory> storeInventories) {
        for (int i = 0; i < 9; i++) {
            Player player = Bukkit.getPlayer(m_player);
            if (player == null) return;
            Inventory shop = Bukkit.createInventory(player, 54, "Shop");
            createShopMenu(shop, i);
            createShop(shop, i);
            storeInventories.set(i,shop);
        }
    }

    public void setTeam(Team team, boolean hasBed) {
        m_team = team;
        m_hasBed = hasBed;
        createStore(m_shop);
    }

    public Team getTeam() {
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
        m_armor = getItem(items).getType();
        setLowerArmor(m_armor);
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
            player.getInventory().setBoots(Util.setColor(getProt(material),m_team.getColor()));
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
        Util.addToShop(inventory, 0, Material.NETHER_STAR);
        switch (m_team){
            case RED:
                Util.addToShop(inventory,1, Material.RED_TERRACOTTA);
                break;
            case YELLOW:
                Util.addToShop(inventory,1, Material.YELLOW_TERRACOTTA);
                break;
            case GREEN:
                Util.addToShop(inventory,1, Material.LIME_TERRACOTTA);
                break;
            case BLUE:
                Util.addToShop(inventory,1, Material.BLUE_TERRACOTTA);
                break;
            case NONE:
                Util.addToShop(inventory,1, Material.TERRACOTTA);
                break;
            default:
                throw new UnsupportedOperationException("Invalid team");
        }
        Util.addToShop(inventory, 2,
                Material.GOLDEN_SWORD,
                Material.CHAINMAIL_BOOTS,
                Material.STONE_AXE,
                Material.BOW,
                Material.BREWING_STAND,
                Material.TNT,
                Material.BEDROCK);
        for (int i = 9; i < 18; i++) {
            Util.addToShop(inventory, i, Material.GRAY_STAINED_GLASS_PANE);
        }
        Util.addToShop(inventory, greenPlacement + 9, Material.LIME_STAINED_GLASS_PANE);
    }

    public ItemStack getItem(Items material) {
        switch (material) {
            case WOOL:
                String goodForBridging = "Cheap blocks, good for bridging.";
                int amount = 16;
                switch (m_team) {
                    case RED:
                        return Util.addNamesShopStyle(new ItemStack(Material.RED_WOOL, amount), "Red Wool", material.getName() , material.getFirstCost(), goodForBridging);
                    case YELLOW:
                        return Util.addNamesShopStyle(new ItemStack(Material.YELLOW_WOOL, amount), "Yellow Wool", material.getName(), material.getFirstCost(),goodForBridging);
                    case GREEN:
                        return Util.addNamesShopStyle(new ItemStack(Material.LIME_WOOL, amount), "Green Wool", material.getName(), material.getFirstCost(),goodForBridging);
                    case BLUE:
                        return Util.addNamesShopStyle(new ItemStack(Material.BLUE_WOOL, amount), "Blue Wool", material.getName(), material.getFirstCost(),goodForBridging);
                    case NONE:
                        return Util.addNamesShopStyle(new ItemStack(Material.WHITE_WOOL, amount), "White Wool", material.getName(), material.getFirstCost(),goodForBridging);
                    default:
                        throw new UnsupportedOperationException("Not a real team");
                }
            case WOOD:
                return Util.addNamesShopStyle(new ItemStack(Material.OAK_PLANKS,16), "Wood", material.getName(), material.getFirstCost(), "Stronger bed defense then wool,", "But still pretty weak");
            case END_STONE:
                return Util.addNamesShopStyle(new ItemStack(Material.END_STONE,12), "End Stone", material.getName(), material.getFirstCost(), "Strong bed defense, renders fireballs useless");
            case OBSIDIAN:
                return Util.addNamesShopStyle(new ItemStack(Material.OBSIDIAN,4), "Obsidian", material.getName(), material.getFirstCost(), "The best bed defense, very expensive");
            case BLAST_PROOF_GLASS:
                switch (m_team) {
                    case RED:
                        return Util.addNamesShopStyle(new ItemStack(Material.RED_STAINED_GLASS,4), "Blast Proof Glass", material.getName(), material.getFirstCost(), "Renders fireballs and tnt useless," ,"but is very fragile to a player.");
                    case YELLOW:
                        return Util.addNamesShopStyle(new ItemStack(Material.YELLOW_STAINED_GLASS,4), "Blast Proof Glass", material.getName(), material.getFirstCost(), "Renders fireballs and tnt useless," ,"but is very fragile to a player.");
                    case GREEN:
                        return Util.addNamesShopStyle(new ItemStack(Material.LIME_STAINED_GLASS,4), "Blast Proof Glass", material.getName(), material.getFirstCost(), "Renders fireballs and tnt useless," ,"but is very fragile to a player.");
                    case BLUE:
                        return Util.addNamesShopStyle(new ItemStack(Material.BLUE_STAINED_GLASS,4), "Blast Proof Glass", material.getName(), material.getFirstCost(), "Renders fireballs and tnt useless," ,"but is very fragile to a player.");
                    case NONE:
                        return Util.addNamesShopStyle(new ItemStack(Material.GLASS,4), "Blast Proof Glass", material.getName(), material.getFirstCost(), "Renders fireballs and tnt useless," ,"but is very fragile to a player.");
                    default:
                        throw new UnsupportedOperationException("Not a real team");
                }
            case LADDERS:
                return Util.addNamesShopStyle(new ItemStack(Material.LADDER,8), "Ladders", material.getName(), material.getFirstCost(), "Ladders, they help climb.");
            case TERRACOTTA:
                switch (m_team) {
                    case RED:
                        return Util.addNamesShopStyle(new ItemStack(Material.RED_TERRACOTTA,16), "Terracotta", material.getName(), material.getFirstCost(), "Strong yet quite cheap bed defense.");
                    case YELLOW:
                        return Util.addNamesShopStyle(new ItemStack(Material.YELLOW_TERRACOTTA,16), "Terracotta", material.getName(), material.getFirstCost(), "Strong yet quite cheap bed defense.");
                    case GREEN:
                        return Util.addNamesShopStyle(new ItemStack(Material.LIME_TERRACOTTA,16), "Terracotta", material.getName(), material.getFirstCost(), "Strong yet quite cheap bed defense.");
                    case BLUE:
                        return Util.addNamesShopStyle(new ItemStack(Material.BLUE_TERRACOTTA,16), "Terracotta", material.getName(), material.getFirstCost(), "Strong yet quite cheap bed defense.");
                    case NONE:
                        return Util.addNamesShopStyle(new ItemStack(Material.TERRACOTTA,16), "Terracotta", material.getName(), material.getFirstCost(), "Strong yet quite cheap bed defense.");
                    default:
                        throw new UnsupportedOperationException("Not a real team");
                }
            case STONE_SWORD:
                return Util.addNamesShopStyle(getSharp(Material.STONE_SWORD), "Stone Sword", material.getName(), material.getFirstCost(), "Good value cheap sword.");
            case IRON_SWORD:
                return Util.addNamesShopStyle(getSharp(Material.IRON_SWORD), "Iron Sword", material.getName(), material.getFirstCost(), "Slightly more expense but more powerful sword.");
            case DIAMOND_SWORD:
                return Util.addNamesShopStyle(getSharp(Material.DIAMOND_SWORD), "Diamond Sword", material.getName(), material.getFirstCost(), "Very powerful endgame sword.");
            case NETHERITE_SWORD:
                return Util.addNamesShopStyle(getSharp(Material.NETHERITE_SWORD), "Netherite Sword", material.getName(), material.getFirstCost(), "The last sword you will ever need.");
            case KNOCKBACK_STICK:
                return Util.addNamesShopStyle(Util.createWithEnchantment(Enchantment.KNOCKBACK, Material.STICK), "Knockback Stick", material.getName(), material.getFirstCost(), "Stick blessed with the power of knockback," , "use it to bully your enemies on bridges.");
            case PERMANENT_CHAINMAIL_ARMOR:
                return Util.addNamesShopStyle(getProt(Material.CHAINMAIL_BOOTS), "Permanent Chainmail Armor", material.getName(), material.getFirstCost(), "Permanent chainmail armor,","quite the bargain.");
            case PERMANENT_IRON_ARMOR:
                return Util.addNamesShopStyle(getProt(Material.IRON_BOOTS), "Permanent Iron Armor", material.getName(), material.getFirstCost(), "Permanent iron armor, also","quite the bargain.");
            case PERMANENT_DIAMOND_ARMOR:
                return Util.addNamesShopStyle(getProt(Material.DIAMOND_BOOTS), "Permanent Diamond Armor", material.getName(), material.getFirstCost(), "Permanent diamond armor, very","powerful endgame armor.");
            case PERMANENT_NETHERITE_ARMOR:
                return Util.addNamesShopStyle(getProt(Material.NETHERITE_BOOTS), "Permanent Netherite Armor", material.getName(), material.getFirstCost(), "Permanent netherite armor,", "the final endgame armor.");
            case PERMANENT_SHEARS:
                return Util.addNamesShopStyle(Util.createWithUnbreakable(Material.SHEARS), "Permanent Shears", material.getName(), material.getFirstCost(), "Permanent shears allowing you", "to always break wool", "faster than your enemies.");
            case PICKAXE_UPGRADE:
                Trio<ItemStack, ItemStack, String> pickaxe = getPickaxeInfo();
                return Util.addNamesShopStyle(pickaxe.getFirst(), pickaxe.getThird(), material.getName(), pickaxe.getSecond(), "Pickaxe upgrades, necessary for breaking","into opponents beds, the more you spend ","on pickaxes the faster your heists will be.");
            case AXE_UPGRADE:
                Trio<ItemStack, ItemStack, String> axe = getAxeInfo();
                return Util.addNamesShopStyle(getSharp(axe.getFirst()), axe.getThird(), material.getName(), axe.getSecond(), "Axe upgrades, necessary are","sometimes necessary for","breaking into opponents beds,","they are useful mainly for", "wood defenses and damage.");
            case ARROW:
                return Util.addNamesShopStyle(new ItemStack(Material.ARROW,6), "Arrows", material.getName(), material.getFirstCost(), "Arrows are a necessity if you want to buy bows", "as they are literally your ammo.");
            case REGULAR_BOW:
                return Util.addNamesShopStyle(Util.createWithUnbreakable(Material.BOW), "Bow", material.getName(), material.getFirstCost(), "Base bow, still a powerful weapon for knocking your", "opponents off their bridges for a good price.");
            case POWER_BOW:
                return Util.addNamesShopStyle(Util.createWithEnchantmentAndUnbreakable(Enchantment.POWER,Material.BOW), "Power Bow", material.getName(), material.getFirstCost(), "The more powerful bow, a good late game purchase.");
            case PUNCH_BOW:
                return Util.addNamesShopStyle(Util.addEnchantment(Enchantment.PUNCH,Util.createWithEnchantmentAndUnbreakable(Enchantment.POWER,Material.BOW)), "Punch Bow", material.getName(), material.getFirstCost(), "The endgame bow, watch your opponents fly from a", "single hit with this bad boy.");
            case JUMP_BOOST_POTION:
                return Util.addNamesShopStyle(Util.createPotion(Color.LIME, PotionEffectType.JUMP_BOOST, 60*20, 4), "Jump Boost Potion", material.getName(), material.getFirstCost(), "This potion will make you jump","over all your opponents defenses...", "for 60 seconds.");
            case SPEED_POTION:
                return Util.addNamesShopStyle(Util.createPotion(Color.YELLOW, PotionEffectType.SPEED, 60*20, 1), "Speed Potion", material.getName(), material.getFirstCost(), "This potion will make you speed","right past all your opponents...", "for 60 seconds.");
            case INVISIBILTY_POTION:
                return Util.addNamesShopStyle(Util.createPotion(Color.WHITE, PotionEffectType.INVISIBILITY, 30*20, 0), "Invisibility Potion", material.getName(), material.getFirstCost(), "This potion will make you sneak","right past all your opponents...", "for 30 seconds.");
            case GOLDEN_APPLE:
                return Util.addNamesShopStyle(new ItemStack(Material.GOLDEN_APPLE), "Golden Apple", material.getName(), material.getFirstCost(), "Fantastic regen to get you out of a tough spot");
            case SILVERFISH_SNOWBALL:
                return Util.addNamesShopStyle(new ItemStack(Material.SNOWBALL), "Silverfish Snowball", material.getName(), material.getFirstCost(), "A way to swarm your opponents with annoying critters");
            case IRON_GOLEM_SPAWN_EGG:
                return Util.addNamesShopStyle(new ItemStack(Material.IRON_GOLEM_SPAWN_EGG), "Iron Golem", material.getName(), material.getFirstCost(), "Spawns a literal iron golem to defend you,","it does not get much better than that");
            case FIREBALL:
                return Util.addNamesShopStyle(new ItemStack(Material.FIRE_CHARGE), "Fireball", material.getName(), material.getFirstCost(), "Shoots a ball which creates", "a large explosion on impact");
            case TNT:
                return Util.addNamesShopStyle(new ItemStack(Material.TNT), "TNT", material.getName(), material.getFirstCost(), "Spawns an activated tnt on place");
            case ENDER_PEARL:
                return Util.addNamesShopStyle(new ItemStack(Material.ENDER_PEARL), "Ender Pearl", material.getName(), material.getFirstCost(), "It's an ender pearl, it lets you","teleport where you throw it");
            case WATER_BUCKET:
                return Util.addNamesShopStyle(new ItemStack(Material.WATER_BUCKET), "Water Bucket", material.getName(), material.getFirstCost(), "Lets you add water somewhere");
            case BRIDGE_EGG:
                return Util.addNamesShopStyle(new ItemStack(Material.EGG), "Bridge Egg", material.getName(), material.getFirstCost(), "Creates a bridge where you throw it");
            case MAGIC_MILK:
                return Util.addNamesShopStyle(new ItemStack(Material.MILK_BUCKET), "Magic Milk", material.getName(), material.getFirstCost(), "Lets you avoid enemy traps for 60 seconds");
            case SPONGE:
                return Util.addNamesShopStyle(new ItemStack(Material.SPONGE, 4), "Sponges", material.getName(), material.getFirstCost(), "Soaks up water");
            case POP_OUT_BASE:
                return Util.addNamesShopStyle(new ItemStack(Material.CHEST), "Pop Out Base", material.getName(), material.getFirstCost(), "Creates a mini-base where you place it");
            default: throw new UnsupportedOperationException("Not a bedwars item");
        }
    }

    public boolean upgradePick() {
        return setPickLevel(getPickaxeToShow());
    }

    public Optional<Material> getPickaxeLevel() {
        return m_pickaxeLevel;
    }

    public Optional<Material> getAxeLevel() {
        return m_axeLevel;
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

    private boolean setPickLevel(Material material) {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) throw new UnsupportedOperationException("No player");
        if (m_pickaxeLevel.isPresent()) {
            if (m_pickaxeLevel.get() == material) return false;
            Util.clear(player.getInventory(), m_pickaxeLevel.get());
        }
        m_pickaxeLevel = Optional.of(material);
        menuOne(m_shop.get(0));
        menuFive(m_shop.get(4));
        player.updateInventory();
        return true;
    }

    public void onRespawn() {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return;
        m_onRespawn.get().cancel();
        m_onRespawn = Optional.empty();
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        setFullArmorSet();
        player.teleport(Util.getSpawnerLocation(m_team));
        PlayerInventory inventory = player.getInventory();
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

    private void addItemAxe(PlayerInventory inventory, Material material) {
        inventory.addItem(getAxe(material));
        m_axeLevel = Optional.of(material);
        menuOne(m_shop.get(0));
        menuFive(m_shop.get(4));
    }

    public void onDeath() {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return;
        player.getInventory().clear();
        Util.clearEffects(player);
        player.setGameMode(GameMode.SPECTATOR);
        m_onRespawn.ifPresent(BukkitRunnable::cancel);
        if (!m_hasBed) {
            player.sendMessage("You are out of the game");
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

    private void setFullArmorSet() {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) return;
        player.getInventory().setHelmet(Util.setColor(getProt(Material.LEATHER_HELMET), m_team.getColor()));
        player.getInventory().setChestplate(Util.setColor(getProt(Material.LEATHER_CHESTPLATE), m_team.getColor()));
        setLowerArmor(m_armor);
    }



    private boolean setAxeLevel(Material material) {
        Player player = Bukkit.getPlayer(m_player);
        if (player == null) throw new UnsupportedOperationException("No player");
        if (m_axeLevel.isPresent()) {
            if (m_axeLevel.get() == material) return false;
            Util.clear(player.getInventory(), m_axeLevel.get());
        }
        m_axeLevel = Optional.of(material);
        menuOne(m_shop.get(0));
        menuFive(m_shop.get(4));
        player.updateInventory();
        return true;
    }

    public boolean upgradeAxe() {
        return setAxeLevel(getAxeToShow());
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

    private Material getPickaxeToShow() {
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
        player.openInventory(m_shop.get(menuNum));
        m_currentShopMenu = Optional.of(menuNum);
    }

    private void createShop(Inventory inventory, int i) {
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

    private void upgradeMenu(Inventory inventory) {

    }

    private void menuOne(Inventory inventory) {
        addToShop(inventory,
                Items.WOOL,
                Items.DIAMOND_SWORD,
                Items.KNOCKBACK_STICK,
                Items.AXE_UPGRADE,
                Items.ENDER_PEARL,
                Items.INVISIBILTY_POTION,
                Items.OBSIDIAN);
        addToShop(inventory,28,
                Items.WOOD,
                Items.IRON_SWORD,
                Items.PERMANENT_IRON_ARMOR,
                Items.PICKAXE_UPGRADE,
                Items.FIREBALL,
                Items.SPEED_POTION,
                Items.IRON_GOLEM_SPAWN_EGG);
        addToShop(inventory,37,
                Items.END_STONE,
                Items.STONE_SWORD,
                Items.PERMANENT_DIAMOND_ARMOR,
                Items.PERMANENT_SHEARS,
                Items.MAGIC_MILK,
                Items.JUMP_BOOST_POTION,
                Items.GOLDEN_APPLE);
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
                Items.WATER_BUCKET);
        addToShop(inventory,28,
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

    private void addToShop(Inventory inventory, Items... items) {
        addToShop(inventory, 19, items);
    }

    private void addToShop(Inventory inventory, int startpoint, Items... items) {
        ItemStack[] itemStacks = new ItemStack[items.length];
        for (int i = 0; i < (items.length); i++) {
            itemStacks[i] = getItem(items[i]);
        }
        Util.addToShop(inventory, startpoint, itemStacks);
    }
}