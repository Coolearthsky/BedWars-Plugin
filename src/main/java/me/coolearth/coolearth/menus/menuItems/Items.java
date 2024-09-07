package me.coolearth.coolearth.menus.menuItems;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public enum Items {
    WOOL("wool", new ItemStack(Material.IRON_INGOT,4)),
    TERRACOTTA("terracotta", new ItemStack(Material.IRON_INGOT,12)),
    BLAST_PROOF_GLASS("glass", new ItemStack(Material.IRON_INGOT,12)),
    END_STONE("endStone", new ItemStack (Material.IRON_INGOT,24)),
    LADDERS("ladder", new ItemStack(Material.IRON_INGOT,4)),
    WOOD("wood", new ItemStack(Material.GOLD_INGOT,4)),
    OBSIDIAN("obsidian", new ItemStack(Material.EMERALD,4)),
    STONE_SWORD("stoneSword", new ItemStack(Material.IRON_INGOT,10)),
    IRON_SWORD("ironSword", new ItemStack(Material.GOLD_INGOT,7)),
    DIAMOND_SWORD("diamondSword",new ItemStack(Material.EMERALD,4)),
    NETHERITE_SWORD("netheriteSword",new ItemStack(Material.EMERALD,6)),
    KNOCKBACK_STICK("knockbackStick",new ItemStack(Material.GOLD_INGOT,5)),
    PERMANENT_CHAINMAIL_ARMOR("chainmailArmor",new ItemStack(Material.IRON_INGOT,40)),
    PERMANENT_IRON_ARMOR("ironArmor",new ItemStack(Material.GOLD_INGOT,12)),
    PERMANENT_DIAMOND_ARMOR("diamondArmor",new ItemStack(Material.EMERALD,6)),
    PERMANENT_NETHERITE_ARMOR("netheriteArmor",new ItemStack(Material.EMERALD,9)),
    PERMANENT_SHEARS("shears",new ItemStack(Material.IRON_INGOT,20)),
    PICKAXE_UPGRADE("pickaxe",new ItemStack(Material.IRON_INGOT, 10), new ItemStack(Material.IRON_INGOT, 10), new ItemStack(Material.GOLD_INGOT, 3), new ItemStack(Material.GOLD_INGOT, 6), new ItemStack(Material.GOLD_INGOT, 9)),
    AXE_UPGRADE ("axe",new ItemStack(Material.IRON_INGOT, 10), new ItemStack(Material.IRON_INGOT, 10), new ItemStack(Material.GOLD_INGOT, 3), new ItemStack(Material.GOLD_INGOT, 6), new ItemStack(Material.GOLD_INGOT, 9)),
    ARROW("arrow",new ItemStack(Material.GOLD_INGOT, 2)),
    REGULAR_BOW("bow",new ItemStack(Material.GOLD_INGOT, 12)),
    POWER_BOW("powerBow",new ItemStack(Material.GOLD_INGOT, 20)),
    PUNCH_BOW("punchBow",new ItemStack(Material.EMERALD, 6)),
    JUMP_BOOST_POTION("jumpPot",new ItemStack(Material.EMERALD, 1)),
    SPEED_POTION("speedPot",new ItemStack(Material.EMERALD, 1)),
    INVISIBILTY_POTION("invisPot",new ItemStack(Material.EMERALD, 2)),
    GOLDEN_APPLE("goldenApple",new ItemStack(Material.GOLD_INGOT, 3)),
    SILVERFISH_SNOWBALL("snowball",new ItemStack(Material.IRON_INGOT, 24)),
    FIREBALL("fireball",new ItemStack(Material.IRON_INGOT, 40)),
    IRON_GOLEM_SPAWN_EGG("ironGolem",new ItemStack(Material.IRON_INGOT, 120)),
    TNT("tnt",new ItemStack(Material.GOLD_INGOT, 4)),
    ENDER_PEARL("enderPearl",new ItemStack(Material.EMERALD, 4)),
    WATER_BUCKET("water",new ItemStack(Material.GOLD_INGOT, 2)),
    BRIDGE_EGG("bridgeEgg",new ItemStack(Material.EMERALD, 1)),
    MAGIC_MILK("magicMilk",new ItemStack(Material.GOLD_INGOT, 4)),
    SPONGE("sponge",new ItemStack(Material.GOLD_INGOT, 2)),
    POP_OUT_BASE("popOutBase", new ItemStack(Material.IRON_INGOT, 24)),
    UNKNOWN(null);

    private final ItemStack[] m_cost;
    private final String m_name;
    private final static Map<String, Items> items = new HashMap<>();
    static {
        for (Items i : Items.values()) {
            items.put(i.m_name, i);
        }
    }

    Items(String name, ItemStack... cost) {
        m_cost = cost;
        m_name = name;
    }

    public ItemStack getFirstCost() {
        return m_cost[0];
    }

    public String getName() {
        return m_name;
    }

    public ItemStack getCost(int num) {
        return m_cost[num];
    }

    public static Items get(String name) {
        if (items.containsKey(name)) {
            return items.get(name);
        }
        return UNKNOWN;
    }
}
