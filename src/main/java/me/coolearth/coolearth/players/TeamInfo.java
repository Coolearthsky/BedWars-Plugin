package me.coolearth.coolearth.players;

import com.comphenix.protocol.wrappers.Pair;
import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.Constants;
import me.coolearth.coolearth.math.RomanNumber;
import me.coolearth.coolearth.menus.menuItems.MenuUtil;
import me.coolearth.coolearth.menus.menuItems.Traps;
import me.coolearth.coolearth.menus.menuItems.Upgrades;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.Supplier;

public class TeamInfo {
    private final Map<Material, BukkitRunnable> m_spawners;
    public Map<UUID,Inventory> m_upgrades;
    private int m_protectionLevel = 0;
    private boolean m_sharpness;
    private Optional<BukkitRunnable> m_miningManiac;
    private int m_miningManiacLevel = 0;
    private boolean m_hasBed;
    private boolean m_dragonBuff;
    private final List<Traps> m_traps ;
    private final Map<UUID, PlayerAddons> m_playersOnTeam;
    private final Supplier<Map<UUID, PlayerAddons>> m_allPlayers;
    private final ArrayList<UUID> playersInBase;
    private int m_generatorLevel;
    private final TeamUtil m_team;
    private Optional<BukkitRunnable> m_healPool;
    private BukkitRunnable m_trapCheck;
    private final JavaPlugin m_coolearth;

    public TeamInfo(TeamUtil team, JavaPlugin coolearth, Map<UUID, PlayerAddons> playersOnTeam, Supplier<Map<UUID, PlayerAddons>> allPlayers) {
        m_team = team;
        m_spawners = new HashMap<>();
        m_upgrades = new HashMap<>();
        m_generatorLevel = 0;
        m_traps = new ArrayList<>();
        m_playersOnTeam = playersOnTeam;
        m_allPlayers = allPlayers;
        m_miningManiacLevel = 0;
        m_hasBed = true;
        playersInBase = new ArrayList<>();
        m_dragonBuff = false;
        m_sharpness = false;
        m_healPool = Optional.empty();
        m_miningManiac = Optional.empty();
        m_coolearth = coolearth;
        createUpgrades();
        trapCheck();
    }

    public int numberOfPeopleOnTeam() {
        return m_playersOnTeam.size();
    }

    public int numberOfAlivePeopleOnTeam() {
        int i = 0;
        for (PlayerAddons playerAddons : m_playersOnTeam.values()) {
            if (playerAddons.isAlive()) i++;
        }
        return i;
    }

    public boolean hasSharp() {
        return m_sharpness;
    }

    public boolean isAnyoneOnTeamAlive() {
        for (PlayerAddons playerAddons : m_playersOnTeam.values()) {
            if (playerAddons.isAlive()) {
                return true;
            }
        }
        return false;
    }

    public TeamUtil getTeam() {
        return m_team;
    }

    private void trapCheck() {
        m_trapCheck = new BukkitRunnable() {
            @Override
            public void run() {
                if (m_traps.isEmpty()) return;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerUUID = player.getUniqueId();
                    if (player.getGameMode() == GameMode.SPECTATOR) continue;
                    if (Util.getTeam(player) == m_team) continue;
                    if (!Util.atBase(player.getLocation(), m_team)) {
                        playersInBase.remove(playerUUID);
                        continue;
                    }
                    if (playersInBase.contains(playerUUID)) continue;
                    if (m_allPlayers.get().get(playerUUID).ignoreTrap()) continue;
                    triggerTrap(player);
                }
            }
        };
        m_trapCheck.runTaskTimer(m_coolearth, 0, 0);
    }

    public void upgrade(Upgrades upgrades) {
        switch (upgrades) {
            case HEAL_POOL:
                gotHealPool();
                return;
            case IRON_FORGE:
                upgradeToNextLevel();
                return;
            case MANIAC_MINER:
                upgradeManiacMiner();
                return;
            case REINFORCED_ARMOR:
                upgradeProt();
                return;
            case SHARPENED_SWORDS:
                upgradeToSharpness();
                return;
            case DRAGON_BUFF:
                getDragonBuff();
                return;
            default:
                throw new UnsupportedOperationException("Not an accepted upgrade");
        }
    }

    private void getDragonBuff() {
        createUpgrades();
    }

    public boolean upgradeMaxed(Upgrades upgrades){
        switch (upgrades) {
            case HEAL_POOL:
                return m_healPool.isPresent();
            case DRAGON_BUFF:
                return true;
            case IRON_FORGE:
                return m_generatorLevel >= 4;
            case MANIAC_MINER:
                return m_miningManiacLevel >=2;
            case REINFORCED_ARMOR:
                return m_protectionLevel >= 4;
            case SHARPENED_SWORDS:
                return m_sharpness;
            default:
                throw new UnsupportedOperationException("Not an accepted upgrade");
        }
    }

    private void gotHealPool() {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : m_playersOnTeam.keySet()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) return;
                    if (Util.atBase(player.getLocation(), m_team)) player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 2, 0, false, false));
                }
            }
        };
        runnable.runTaskTimer(m_coolearth, 0 ,0);
        m_healPool = Optional.of(runnable);
        createUpgrades();
    }


    public boolean canGetTrap() {
        return m_traps.size() < 3;
    }

    public void getTrap(Traps trap) {
        m_traps.add(trap);
        createUpgrades();
    }

    public void stopAllLoops() {
        stopSpawners();
        m_trapCheck.cancel();
        m_healPool.ifPresent(BukkitRunnable::cancel);
        m_miningManiac.ifPresent(BukkitRunnable::cancel);
    }

    public void stopSpawners() {
        for (BukkitRunnable runnable : m_spawners.values()) {
            runnable.cancel();
        }
        m_spawners.clear();
    }

    public void createUpgrades() {
        for (UUID uuid : m_playersOnTeam.keySet()) {
            createUpgradesForPlayer(uuid);
        }
    }

    public void createUpgradesForPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (!m_upgrades.containsKey(uuid)) {
            Inventory upgrades = Bukkit.createInventory(player, 54, "Upgrades");
            m_upgrades.put(uuid, upgrades);
        }
        Inventory upgrades = m_upgrades.get(uuid);
        addToShop(player,upgrades, 10, Upgrades.SHARPENED_SWORDS, Upgrades.REINFORCED_ARMOR, Upgrades.MANIAC_MINER);
        addToShop(player,upgrades, 19, Upgrades.IRON_FORGE, Upgrades.HEAL_POOL, Upgrades.DRAGON_BUFF);
        addToShop(player,upgrades, 14, Traps.BLINDNESS_TRAP, Traps.COUNTER_OFFENSE_TRAP, Traps.ALARM_TRAP);
        addToShop(player,upgrades, 23, Traps.MINING_FATIGUE_TRAP);
        MenuUtil.addToShop(upgrades, 39, getTraps(player));
        for (int i = 27; i < 36; i++) {
            addToShop(player,upgrades, i, Upgrades.SEPARATOR);
        }
    }

    private ItemStack[] getTraps(Player player) {
        int size = m_traps.size();
        List<ItemStack> array = new ArrayList<>();
        int count = 1;
        for (Traps trap : m_traps) {
            ItemStack item = getItem(trap,player);
            ItemMeta itemMeta = item.getItemMeta();
            List<String> e = itemMeta.getLore();
            itemMeta.setDisplayName(ChatColor.GREEN + itemMeta.getDisplayName().substring(2));
            int g = 4;
            if (size == 3) {
                g = 2;
            }
            for (int i =0; i < g;i++) {
                e.remove(e.size() - 1);
            }
            itemMeta.setMaxStackSize(3);
            itemMeta.setItemName("");
            itemMeta.setLore(e);
            item.setAmount(count);
            item.setItemMeta(itemMeta);
            array.add(item);
            count++;
        }
        for (int i = 0; i < 3-size; i++) {
            int amount = i + size + 1;
            int cost = (int) Math.pow(2, size);
            array.add(MenuUtil.noTrapStyle(Material.LIGHT_GRAY_STAINED_GLASS, amount, cost));
        }
        ItemStack[] intarray = new ItemStack[array.size()];
        for(int i = 0; i < array.size(); i++) intarray[i] = array.get(i);
        return intarray;
    }

    private ItemStack getItem(Upgrades upgrades, Player player) {
        Integer cost = null;
        Boolean bool = null;
        if (upgrades.getCost().length > 0) {
            cost = upgrades.getFirstCost();
            bool = player.getInventory().contains(Material.DIAMOND, cost);
        }
        switch (upgrades) {
            case SHARPENED_SWORDS:
                return MenuUtil.addNamesUpgradeStyle(!m_sharpness,bool,new ItemStack(Material.IRON_SWORD), "Sharpened Swords", upgrades.getName(), cost, "Gives all your team's swords sharpness 1.");
            case REINFORCED_ARMOR:
                String num = RomanNumber.toRoman(m_protectionLevel+1);
                if (m_protectionLevel == 4) num = "IV";
                return MenuUtil.addNamesUpgradeStyle(bool,new ItemStack(Material.IRON_CHESTPLATE), "Reinforced Armor " + num, upgrades.getName(), getPairCost(upgrades), "Gives your armor protection.");
            case HEAL_POOL:
                return MenuUtil.addNamesUpgradeStyle(!m_healPool.isPresent(),bool,new ItemStack(Material.BEACON), "Heal Pool", upgrades.getName(), cost, "Creates a range around your base where you get regen.");
            case IRON_FORGE:
                String ironForge;
                switch (m_generatorLevel) {
                    case 0:
                        ironForge = "Iron Forge";
                        break;
                    case 1:
                        ironForge = "Golden Forge";
                        break;
                    case 2:
                        ironForge = "Emerald Forge";
                        break;
                    case 3:
                    case 4:
                        ironForge = "Molten Forge";
                        break;
                    default:
                        throw new UnsupportedOperationException("Impossible gen level");
                }
                return MenuUtil.addNamesUpgradeStyle(bool,new ItemStack(Material.FURNACE), ironForge, upgrades.getName(), getPairCost(upgrades), "Speeds up your generator, can even give you emeralds.");
            case MANIAC_MINER:
                String num12 = RomanNumber.toRoman(m_miningManiacLevel+1);
                if (m_miningManiacLevel == 2) num12 = "II";
                return MenuUtil.addNamesUpgradeStyle(bool,new ItemStack(Material.GOLDEN_PICKAXE), "Maniac Miner "+ num12, upgrades.getName(), getPairCost(upgrades), "Gives you haste.");
            case DRAGON_BUFF:
                return MenuUtil.addNamesUpgradeStyle(false,bool,new ItemStack(Material.DRAGON_EGG), "Dragon Buff", upgrades.getName(), cost, "Gives you an extra dragon in the end game.");
            case SEPARATOR:
                return MenuUtil.getSeparator(false, "Purchasable","Traps Queue");
            default:
                throw new UnsupportedOperationException("Not allowed upgrade");
        }
    }

    private Pair<Integer, int[]> getPairCost(Upgrades upgrades){
        switch (upgrades) {
            case REINFORCED_ARMOR:
                return new Pair<>(m_protectionLevel, upgrades.getCost());
            case IRON_FORGE:
                return new Pair<>(m_generatorLevel, upgrades.getCost());
            case MANIAC_MINER:
                return new Pair<>(m_miningManiacLevel, upgrades.getCost());
            default:
                throw new UnsupportedOperationException("Does not have multiple costs");
        }
    }

    public ItemStack getItemStackCost(Upgrades upgrades){
        return new ItemStack(Material.DIAMOND, getIntCost(upgrades));
    }

    public ItemStack getItemStackCostTraps(){
        return new ItemStack(Material.DIAMOND, getTrapCost().get());
    }

    private int getIntCost(Upgrades upgrades){
        switch (upgrades) {
            case REINFORCED_ARMOR:
                return upgrades.getCost()[m_protectionLevel];
            case IRON_FORGE:
                return upgrades.getCost()[m_generatorLevel];
            case MANIAC_MINER:
                return upgrades.getCost()[m_miningManiacLevel];
            case SHARPENED_SWORDS:
            case HEAL_POOL:
            case DRAGON_BUFF:
                return upgrades.getFirstCost();
            default:
                throw new UnsupportedOperationException("Does not have multiple costs");
        }
    }

    private ItemStack getItem(Traps traps, Player player) {
        Optional<Integer> trapCost = getTrapCost();
        boolean bool = false;
        if (trapCost.isPresent()) {
            bool = player.getInventory().contains(Material.DIAMOND, trapCost.get());
        }
        switch (traps) {
            case BLINDNESS_TRAP:
                return MenuUtil.addNamesTrapStyle(bool,new ItemStack(Material.TRIPWIRE_HOOK), "It's a trap!", traps.getName(), trapCost, "Inflicts Blindness and Slowness for","8 seconds.");
            case ALARM_TRAP:
                return MenuUtil.addNamesTrapStyle(bool,new ItemStack(Material.REDSTONE_TORCH), "Alarm Trap", traps.getName(), trapCost, "Reveals invisible players as well as", "their name and team.");
            case COUNTER_OFFENSE_TRAP:
                return MenuUtil.addNamesTrapStyle(bool,new ItemStack(Material.FEATHER), "Counter Offense Trap", traps.getName(), trapCost, "Grants Speed II, and Jump Boost II", "for 15 seconds to allied players", "near your base.");
            case MINING_FATIGUE_TRAP:
                return MenuUtil.addNamesTrapStyle(bool,new ItemStack(Material.IRON_PICKAXE), "Mining Fatigue Trap", traps.getName(), trapCost, "Inflict Mining Fatigue for 10 seconds.");
            default:
                throw new UnsupportedOperationException("Not allowed trap");
        }
    }

    public void upgradeToSharpness() {
        m_sharpness = true;
        for (PlayerAddons addon : m_playersOnTeam.values()) {
            addon.gotSharpness();
        }
        createUpgrades();
    }

    public void upgradeProt() {
        m_protectionLevel++;
        for (PlayerAddons addon : m_playersOnTeam.values()) {
            addon.upgradeProt();
        }
        createUpgrades();
    }

    public void bedBreak(Player bedBreaker) {
        m_hasBed = false;
        for (PlayerAddons player : m_allPlayers.get().values()) {
            if (player.getTeam().equals(m_team)) continue;
            Player realPlayer = Bukkit.getPlayer(player.getPlayer());
            if (realPlayer == null) continue;
            realPlayer.sendMessage("\n" + ChatColor.BOLD + "BED DESTRUCTION > " + m_team.getChatColor() + m_team.getName() + " Bed " + ChatColor.GRAY + "was destroyed by " + Util.getTeam(bedBreaker).getChatColor() + bedBreaker.getName() + ChatColor.GRAY + "!\n ");
            realPlayer.playSound(realPlayer, Sound.ENTITY_ENDER_DRAGON_GROWL,1,1);
            realPlayer.playSound(realPlayer, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);
        }
        for (PlayerAddons player : m_playersOnTeam.values()) {
            player.bedBreak();
            Player player1 = Bukkit.getPlayer(player.getPlayer());
            if (!player1.isOnline()) continue;
            player1.sendMessage("\n" + ChatColor.BOLD + "BED DESTRUCTION > " + ChatColor.GRAY + "Your bed was destroyed by " + Util.getTeam(bedBreaker).getChatColor() + bedBreaker.getName() + ChatColor.GRAY + "!\n ");
            player1.playSound(player1, Sound.ENTITY_WITHER_DEATH,1,1);
            player1.sendTitle(ChatColor.RED + "BED DESTROYED!",ChatColor.GRAY + "You will no longer respawn!",(int) (20*0.5),20*2,(int) (20*0.5));
        }
    }

    public boolean hasBed() {
        return m_hasBed;
    }

    private void upgradeManiacMiner() {
        m_miningManiacLevel++;
        createUpgrades();
        addMiningManiac(m_miningManiacLevel);
    }

    public void addMiningManiac(int value) {
        m_miningManiac.ifPresent(BukkitRunnable::cancel);
        m_miningManiac = Optional.of(new BukkitRunnable() {
            @Override
            public void run() {
                for (PlayerAddons playerAddons : m_playersOnTeam.values()) {
                    Player player = Bukkit.getPlayer(playerAddons.getPlayer());
                    if (player != null) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 2, value-1,true, false));
                    }
                }
            }
        });
        m_miningManiac.get().runTaskTimer(m_coolearth, 0,0);
    }

    private void triggerTrap(Player player) {
        UUID playerUUID = player.getUniqueId();
        playersInBase.add(playerUUID);
        Traps traps = m_traps.get(0);
        m_traps.remove(0);
        createUpgrades();
        switch (traps) {
            case BLINDNESS_TRAP:
                for (UUID offenseUUID : m_playersOnTeam.keySet()) {
                    Player offense = Bukkit.getPlayer(offenseUUID);
                    if (offense != null) {
                        offense.sendMessage("Your blindness trap was triggered!");
                    }
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20*8, 0, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20*8, 0, false, false));
                return;
            case COUNTER_OFFENSE_TRAP:
                for (UUID offenseUUID : m_playersOnTeam.keySet()) {
                    Player offense = Bukkit.getPlayer(offenseUUID);
                    if (offense != null) {
                        offense.sendMessage("Your counter offense trap was triggered!");
                        if (!Util.atBase(offense.getLocation(), m_team)) continue;
                        offense.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20*15, 1, false, false));
                        offense.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 20*15,1, false, false));
                    }
                }
                return;
            case MINING_FATIGUE_TRAP:
                for (UUID offenseUUID : m_playersOnTeam.keySet()) {
                    Player offense = Bukkit.getPlayer(offenseUUID);
                    if (offense != null) {
                        offense.sendMessage("Your mining fatigue trap was triggered!");
                    }
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 20*10, 0, false, false));
                return;
            case ALARM_TRAP:
                for (UUID offenseUUID : m_playersOnTeam.keySet()) {
                    Player offense = Bukkit.getPlayer(offenseUUID);
                    if (offense != null) {
                        offense.sendMessage(player.getName() + " from the " + Util.getTeam(player).getName() +" team triggered your alarm trap!");
                    }
                }
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                return;
            default:
                throw new UnsupportedOperationException("Not an accepted trap");
        }
    }

    private Optional<Integer> getTrapCost() {
        int size = m_traps.size();
        switch (size) {
            case 0:
            case 1:
            case 2:
                return Optional.of((int) Math.pow(2, size));
            case 3:
                return Optional.empty();
            default:
                throw new UnsupportedOperationException("Higher than max numbers of traps");
        }
    }

    public Inventory getUpgradesMenu(UUID player) {
        return m_upgrades.get(player);
    }

    public void openMenu(Player player) {
        m_playersOnTeam.get(player.getUniqueId()).enteredUpgrades();
        player.openInventory(m_upgrades.get(player.getUniqueId()));
    }

    private void addToShop(Player player, Inventory inventory, int startNum, Upgrades... upgrades) {
        ItemStack[] itemStacks = new ItemStack[upgrades.length];
        for (int i = 0; i < (upgrades.length); i++) {
            itemStacks[i] = getItem(upgrades[i], player);
        }
        MenuUtil.addToShop(inventory, startNum, itemStacks);
    }

    private void addToShop(Player player, Inventory inventory, int startNum, Traps... traps) {
        ItemStack[] itemStacks = new ItemStack[traps.length];
        for (int i = 0; i < (traps.length); i++) {
            itemStacks[i] = getItem(traps[i], player);
        }
        MenuUtil.addToShop(inventory, startNum, itemStacks);
    }

    public Map<UUID, PlayerAddons> getPeopleOnTeam() {
        return m_playersOnTeam;
    }

    public void upgradeToNextLevel() {
        setUpgradeLevel(m_generatorLevel+1);
    }

    public void startSpawning() {
        setUpgradeLevel(m_generatorLevel);
    }

    public void setUpgradeLevel(int level) {
        m_generatorLevel = level;
        createUpgrades();
        if (level == 0) {
            closeRunnable(Material.EMERALD);
            setInfinite((double) 2/3, Material.IRON_INGOT);
            setInfinite(4, Material.GOLD_INGOT);
        }
        else if (level == 1) {
            closeRunnable(Material.EMERALD);
            setInfinite((double) 4/9, Material.IRON_INGOT);
            setInfinite((double) 8/3, Material.GOLD_INGOT);
        }
        else if (level == 2) {
            closeRunnable(Material.EMERALD);
            setInfinite((double) 1/3, Material.IRON_INGOT);
            setInfinite(2, Material.GOLD_INGOT);
        }
        else if (level == 3) {
            setInfinite((double) 1 / 3, Material.IRON_INGOT);
            setInfinite(2, Material.GOLD_INGOT);
            setInfinite(15, Material.EMERALD);
        }
        else if (level == 4) {
            setInfinite((double) 2/9, Material.IRON_INGOT);
            setInfinite((double) 4/3, Material.GOLD_INGOT);
            setInfinite(10, Material.EMERALD);
        } else {
            throw new UnsupportedOperationException("Critical error, Unknown level");
        }
    }

    private void setInfinite(double seconds, Material material)  {
        closeRunnable(material);
        m_spawners.put(material, new BukkitRunnable() {
            public void run()
            {
                if (Bukkit.getOnlinePlayers().isEmpty()) return;
                Item item = Util.findItemStack(Constants.getTeamGeneratorLocation(m_team), material);
                if (item == null) {
                    setItem(material);
                    return;
                }
                ItemStack itemStack = item.getItemStack();
                switch (material) {
                    case GOLD_INGOT:
                        if (itemStack.getAmount() >= 8) return;
                        break;
                    case IRON_INGOT:
                        if (itemStack.getAmount() >= 48) return;
                        break;
                    case EMERALD:
                        if (itemStack.getAmount() >= 4) return;
                        break;
                    default:
                        throw new UnsupportedOperationException("Not a working item");
                }
                item.setItemStack(new ItemStack(itemStack.getType(),itemStack.getAmount() + 1));
            }
        });
        m_spawners.get(material).runTaskTimer(m_coolearth, (long) (seconds*20), (long) (seconds*20));
    }

    public void closeRunnable(Material pair) {
        BukkitRunnable runnable = m_spawners.get(pair);
        if (runnable != null) {
            runnable.cancel();
            m_spawners.remove(pair);
        }
    }

    private void setItem(Material material) {
        Util.spawnItem(Constants.getTeamGeneratorLocation(m_team), material);
    }
}
