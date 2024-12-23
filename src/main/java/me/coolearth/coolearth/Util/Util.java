package me.coolearth.coolearth.Util;

import com.comphenix.protocol.wrappers.Pair;
import me.coolearth.coolearth.block.BlockManager;
import me.coolearth.coolearth.global.Constants;
import me.coolearth.coolearth.math.MathUtil;
import me.coolearth.coolearth.math.RomanNumber;
import me.coolearth.coolearth.math.Rotation2d;
import me.coolearth.coolearth.menus.menuItems.MenuUtil;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.players.TeamInfo;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class Util {
    private static final Set<Material> unbreakableMaterials = new HashSet<>(Arrays.asList(Material.RED_STAINED_GLASS, Material.YELLOW_STAINED_GLASS, Material.LIME_STAINED_GLASS, Material.BLUE_STAINED_GLASS));

    public static <T extends Entity> void  killAllEntities(Class<?>... cls) {
            for (Entity e : Bukkit.getWorld("world").getEntitiesByClasses(cls)) {
                e.remove();
            }
    }

    public static void sendActionBarMessage(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    public static Material getWool(TeamUtil team) {
        switch (team) {
            case RED:
                return Material.RED_WOOL;
            case YELLOW:
                return Material.YELLOW_WOOL;
            case GREEN:
                return Material.LIME_WOOL;
            case BLUE:
                return Material.BLUE_WOOL;
            case NONE:
                return Material.WHITE_WOOL;
            default:
                throw new UnsupportedOperationException("Not a real team");
        }
    }

    public static Item findItemStack(Location location, Material material) {
        for (Item entity: location.getWorld().getEntitiesByClass(Item.class)) {
            ItemStack itemStack = entity.getItemStack();
            if (material != itemStack.getType()) continue;
            if (!Util.locationsEqualIgnoringRot(location, entity.getLocation())) {
                continue;
            }
            return entity;
        }
        return null;
    }

    public static void clearEffects(Player player) {
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.removePotionEffect(PotionEffectType.HASTE);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.ABSORPTION);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
    }

    public static void spawnItem(Location location, ItemStack material) {
        location.getWorld().dropItem(location, material, item -> {
            item.setVelocity(new Vector());
            item.setUnlimitedLifetime(true);
            item.setPickupDelay(5);
        });
    }

    public static void spawnVillager(Location location, String tag) {
        location.getWorld().spawn(location, Villager.class, t -> {
            t.setInvulnerable(true);
            t.setGravity(false);
            t.addScoreboardTag(tag);
            t.setAI(false);
            t.setVillagerLevel(5);
            t.setProfession(Villager.Profession.LIBRARIAN);
        });
    }

    public static void spawnShopsAndUpgrades() {
        for (TeamUtil teams : TeamUtil.values()) {
            if (teams.equals(TeamUtil.NONE)) continue;
            setupShop(Constants.getShopLocation(teams));
            setupUpgrades(Constants.getUpgradeLocation(teams));
        }
    }

    public static void setupShop(Location shopLocation) {
        spawnVillager(shopLocation, "shop");
        ArmorStand shopStand = spawnArmorStand(shopLocation.clone().add(0,0.25,0));
        setName(shopStand, Materials.DIAMOND.getColor() + "ITEM SHOP");
        ArmorStand rightClickStand = spawnArmorStand(shopLocation);
        setName(rightClickStand, ChatColor.YELLOW + "" + ChatColor.BOLD + "RIGHT CLICK");
    }

    public static void setupUpgrades(Location upgradeLocation) {
        spawnVillager(upgradeLocation, "upgrades");
        ArmorStand upgradeStand = spawnArmorStand(upgradeLocation.clone().add(0,0.25,0));
        setName(upgradeStand, Materials.DIAMOND.getColor() + "UPGRADES");
        ArmorStand rightClickStand = spawnArmorStand(upgradeLocation);
        setName(rightClickStand, ChatColor.YELLOW + "" + ChatColor.BOLD + "RIGHT CLICK");
    }

    public static void broadcastStartMessage() {
        broadcastMessage(getStartMessage());
    }

    public static String getStartMessage() {
        return ChatColor.GREEN + ChatColor.BOLD.toString() + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n" +
                ChatColor.WHITE + ChatColor.BOLD + "                           Bed Wars\n" +
                " \n" +
                ChatColor.YELLOW + ChatColor.BOLD + "    Protect your bed and destroy the enemy beds.\n" +
                "     Upgrade yourself and your team by collecting\n" +
                "   Iron, Gold, Emerald and Diamond from generators\n" +
                "              to access powerful upgrades.\n \n" +
                ChatColor.GREEN + ChatColor.BOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";
    }

    public static void spawnItem(Location location, Material material) {
        spawnItem(location, new ItemStack(material));
    }

    public static void resetTeams() {
        int i = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getScoreboardTags().contains("player")) continue;
            Util.removeTeams(player);
            switch (i) {
                case 0:
                    player.addScoreboardTag(TeamUtil.RED.getName());
                    break;
                case 1:
                    player.addScoreboardTag(TeamUtil.YELLOW.getName());
                    break;
                case 2:
                    player.addScoreboardTag(TeamUtil.GREEN.getName());
                    break;
                case 3:
                    player.addScoreboardTag(TeamUtil.BLUE.getName());
                    break;
                default: throw new UnsupportedOperationException("Teams not registered correctly");
            }
            if (i < 3) {
                i++;
            } else {
                i = 0;
            }
        }
    }

    public static void removeTeams(Player player) {
        for (TeamUtil team : TeamUtil.values()) {
            if (team == TeamUtil.NONE) continue;
            if (player.getScoreboardTags().contains(team.getName())) {
                player.removeScoreboardTag(team.getName());
            }
        }
    }

    public static void emptyPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getScoreboardTags().contains("player")) continue;
            clearEffects(player);
            removeTeams(player);
            player.getInventory().clear();
            player.getEnderChest().clear();
            player.teleport(Constants.getPregameSpawn());
            player.setFallDistance(0.0f);
            player.setVelocity(new Vector());
            player.setGameMode(GameMode.ADVENTURE);
        }
    }

    public static void clearChests() {
        for (Location location : Constants.getChests().keySet()) {
            Chest blockData = (Chest) Bukkit.getWorld("world").getBlockState(location);
            blockData.getBlockInventory().clear();
        }
    }

    /**
     * Does not check if the player
     * has multiple teams but is faster, this should only be used
     * if you are sure the person's teams are set up correctly
     *
     * @return The team a player has
     */
    public static TeamUtil getTeam(Player player) {
        for (TeamUtil team : TeamUtil.values()) {
            if (team == TeamUtil.NONE) continue;
            if (player.getScoreboardTags().contains(team.getName())) {
                return team;
            }
        }
        return TeamUtil.NONE;
    }

    /**
    * @param yaw DEGREES
     * @return direction in radians
     */
    public static Rotation2d getDirection(double yaw) {
        if((yaw >= -45) && (yaw < 45)) {
            return new Rotation2d();
        } else if((yaw >= -135) && (yaw < -45)) {
            return new Rotation2d(Math.PI/2);
        } else if (yaw >= 45 && yaw < 135) {
            return new Rotation2d(-1.0 * Math.PI/2);
        } else if (((yaw >= 135) || (yaw < -135)) && (yaw <= 180 && yaw >= -180)) {
            return new Rotation2d(Math.PI);
        } else {
            throw new UnsupportedOperationException("No direction found given that yaw");
        }
    }

    /**
     * @param yaw DEGREES
     * @return direction in BlockFace inverted from minecraft
     */
    public static BlockFace getDirectionInverted(double yaw) {
        if((yaw >= -45) && (yaw < 45)) {
            return BlockFace.NORTH;
        } else if((yaw >= -135) && (yaw < -45)) {
            return BlockFace.WEST;
        } else if (yaw >= 45 && yaw < 135) {
            return BlockFace.EAST;
        } else if (((yaw >= 135) || (yaw < -135)) && (yaw <= 180 && yaw >= -180)) {
            return BlockFace.SOUTH;
        } else {
            throw new UnsupportedOperationException("No direction found given that yaw");
        }
    }

    /**
     * @param yaw DEGREES
     * @return direction in BlockFace in minecraft
     */
    public static BlockFace getDirectionMinecraft(double yaw) {
        if((yaw >= -45) && (yaw < 45)) {
            return BlockFace.SOUTH;
        } else if((yaw >= -135) && (yaw < -45)) {
            return BlockFace.EAST;
        } else if (yaw >= 45 && yaw < 135) {
            return BlockFace.WEST;
        } else if (((yaw >= 135) || (yaw < -135)) && (yaw <= 180 && yaw >= -180)) {
            return BlockFace.NORTH;
        } else {
            throw new UnsupportedOperationException("No direction found given that yaw");
        }
    }

    public static void protectedExplotion(List<Block> blockList, Location explosionCenter, BlockManager blockManager) {
        Set<Block> protectedBlocks = new HashSet<>();
        boolean isCheck = checkBreakable(blockList);
        for (Block block : blockList) {
            if (!blockManager.contains(block) || unbreakableMaterials.contains(block.getType())) {
                protectedBlocks.add(block);
            } else if (isCheck) {
                Location blockLocation = block.getLocation();
                if (protectedOrNot(explosionCenter.clone().add(0,0.5,0), blockLocation.clone().add(0.5,0.5,0.5))) {
                    protectedBlocks.add(block);
                }
            }
        }
        blockList.removeAll(protectedBlocks);
    }

    public static boolean protectedOrNot(Location explosionCenter, Location blockLocation) {
        int count = 0;
        //MWAHAHAHAHAHAHHAHAHAHA (this is so overkill, but screw your terrible PC if you are running this plugin)
        for (int x = 0; x < 1000; x++) {
            Location randomizedBlockLocation = explosionCenter.clone().add(Math.random()/2-0.25, Math.random()/2-0.25, Math.random()/2-0.25);
            Location randomizedExplosionCenter = blockLocation.clone().add(Math.random()/2-0.25, Math.random()/2-0.25, Math.random()/2-0.25);
            if (checkPath(randomizedExplosionCenter, randomizedBlockLocation)) {
                count++;
            }
            if (count >= 501) return true;
        }
        return false;
    }

    public static boolean checkPath(Location start, Location end) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        direction.normalize();
        for (double d = 0; d < distance; d += 0.2) {
            Location checkLoc = start.clone().add(direction.clone().multiply(d));
            Block checkBlock = checkLoc.getBlock();
            if (unbreakableMaterials.contains(checkBlock.getType())) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkBreakable(List<Block> blockList) {
        for (Block block : blockList) {
            if (unbreakableMaterials.contains(block.getType())) {
                return true;
            }
        }
        return false;
    }

    public static boolean buildPopUpBase(TeamUtil team, Location location, double yaw, BlockManager blockManager) {
        World world = location.getWorld();
        int blockZ = location.getBlockZ();
        int blockY = location.getBlockY();
        int blockX = location.getBlockX();
        world.setBlockData(blockX,blockY,blockZ,Material.AIR.createBlockData());
        Rotation2d direction = getDirection(yaw);
        Location firstlocation = MathUtil.getRelativeLocation(world, blockX + 3, blockY + 6, blockZ + 2, blockX, blockZ, direction);
        Location secondlocation = MathUtil.getRelativeLocation(world, blockX - 3, blockY, blockZ - 3, blockX, blockZ,direction);
        if (!checkClear(firstlocation, secondlocation)) return false;
        Directional ladder = (Directional) Material.LADDER.createBlockData();
        ladder.setFacing(getDirectionInverted(yaw));

        //Walls
        fill(MathUtil.getRelativeLocation(world, blockX-1, blockY, blockZ+1,blockX, blockZ, direction), MathUtil.getRelativeLocation(world, blockX+1, blockY+3, blockZ+1,blockX, blockZ, direction), getWool(team).createBlockData(),blockManager);
        fill(MathUtil.getRelativeLocation(world, blockX-1, blockY, blockZ-2,blockX, blockZ, direction), MathUtil.getRelativeLocation(world, blockX+1, blockY+3, blockZ-2,blockX, blockZ, direction), getWool(team).createBlockData(),blockManager);
        fill(MathUtil.getRelativeLocation(world, blockX, blockY, blockZ-2,blockX, blockZ, direction), MathUtil.getRelativeLocation(world, blockX, blockY+1, blockZ-2,blockX, blockZ, direction), Material.AIR.createBlockData(),blockManager);
        fill(MathUtil.getRelativeLocation(world, blockX+2, blockY, blockZ,blockX, blockZ, direction), MathUtil.getRelativeLocation(world, blockX+2, blockY+3, blockZ-1,blockX, blockZ, direction), getWool(team).createBlockData(),blockManager);
        fill(MathUtil.getRelativeLocation(world, blockX-2, blockY, blockZ,blockX, blockZ, direction), MathUtil.getRelativeLocation(world, blockX-2, blockY+3, blockZ-1,blockX, blockZ, direction), getWool(team).createBlockData(),blockManager);

        //Top
        fill(MathUtil.getRelativeLocation(world, blockX+2, blockY+4, blockZ+1,blockX, blockZ, direction), MathUtil.getRelativeLocation(world, blockX-2, blockY+4, blockZ-2,blockX, blockZ, direction), getWool(team).createBlockData(),blockManager);
        fill(MathUtil.getRelativeLocation(world, blockX-3, blockY+5, blockZ+1,blockX, blockZ, direction), MathUtil.getRelativeLocation(world, blockX-3, blockY+5, blockZ-2,blockX, blockZ, direction), getWool(team).createBlockData(),blockManager);
        fill(MathUtil.getRelativeLocation(world, blockX+3, blockY+5, blockZ+1,blockX, blockZ, direction), MathUtil.getRelativeLocation(world, blockX+3, blockY+5, blockZ-2,blockX, blockZ, direction), getWool(team).createBlockData(),blockManager);
        fill(MathUtil.getRelativeLocation(world, blockX-2, blockY+5, blockZ+2,blockX, blockZ, direction), MathUtil.getRelativeLocation(world, blockX+2, blockY+5, blockZ+2,blockX, blockZ, direction), getWool(team).createBlockData(),blockManager);
        fill(MathUtil.getRelativeLocation(world, blockX-2, blockY+5, blockZ-3,blockX, blockZ, direction), MathUtil.getRelativeLocation(world, blockX+2, blockY+5, blockZ-3,blockX, blockZ, direction), getWool(team).createBlockData(),blockManager);

        //Crevices
        for (int i = 0; i < 3; i++) {
            setBlock(world, MathUtil.getRelativeLocation(world, blockX+2*i-2, blockY+6,blockZ+2,blockX, blockZ, direction),blockManager,getWool(team).createBlockData());
            setBlock(world, MathUtil.getRelativeLocation(world, blockX+2*i-2, blockY+6,blockZ-3,blockX, blockZ, direction),blockManager,getWool(team).createBlockData());
            setBlock(world, MathUtil.getRelativeLocation(world, blockX+2*i-2, blockY+4,blockZ+2,blockX, blockZ, direction),blockManager,getWool(team).createBlockData());
            setBlock(world, MathUtil.getRelativeLocation(world, blockX+2*i-2, blockY+4,blockZ-3,blockX, blockZ, direction),blockManager,getWool(team).createBlockData());
        }

        for (int i = 0; i < 2;i++) {
            setBlock(world, MathUtil.getRelativeLocation(world, blockX+3, blockY+6,blockZ-2+3*i,blockX, blockZ, direction),blockManager,getWool(team).createBlockData());
            setBlock(world, MathUtil.getRelativeLocation(world, blockX-3, blockY+6,blockZ-2+3*i,blockX, blockZ, direction),blockManager,getWool(team).createBlockData());
            setBlock(world, MathUtil.getRelativeLocation(world, blockX+3, blockY+4,blockZ-2+3*i,blockX, blockZ, direction),blockManager,getWool(team).createBlockData());
            setBlock(world, MathUtil.getRelativeLocation(world, blockX-3, blockY+4,blockZ-2+3*i,blockX, blockZ, direction),blockManager,getWool(team).createBlockData());
        }

        //Ladders
        for (int i = 0; i < 5; i++) {
            Location location1 = new Location(world, blockX, blockY+i,blockZ);
            world.setBlockData(location1, ladder);
            blockManager.add(location1);
        }
        return true;
    }

    public static void setBlock(World world, Location location, BlockManager blockManager, BlockData blockData) {
        world.setBlockData(location,blockData);
        if (blockManager != null) blockManager.add(location);
    }

    public static boolean checkPlaceable(Material material) {
        return material.isAir() || material.equals(Material.WATER) || material.equals(Material.FIRE);
    }

    public static boolean checkClear(Location firstlocation, Location secondlocation) {
        if (firstlocation.getBlockX() > secondlocation.getBlockX()) {
            for (int i = secondlocation.getBlockX(); i<firstlocation.getBlockX(); i++) {
                if (firstlocation.getBlockY() > secondlocation.getBlockY()) {
                    for (int j = secondlocation.getBlockY(); j<firstlocation.getBlockY(); j++) {
                        if (firstlocation.getBlockZ() > secondlocation.getBlockZ()) {
                            for (int f = secondlocation.getBlockZ(); f<firstlocation.getBlockZ(); f++) {
                                if (!checkPlaceable(firstlocation.getWorld().getBlockAt(i, j, f).getType())) {
                                    return false;
                                }
                            }
                        } else {
                            for (int f = firstlocation.getBlockZ(); f<secondlocation.getBlockZ(); f++) {
                                if (!checkPlaceable(firstlocation.getWorld().getBlockAt(i, j, f).getType())) {
                                    return false;
                                }
                            }
                        }
                    }
                } else {
                    for (int j = firstlocation.getBlockY(); j<secondlocation.getBlockY(); j++) {
                        if (firstlocation.getBlockZ() > secondlocation.getBlockZ()) {
                            for (int f = secondlocation.getBlockZ(); f<firstlocation.getBlockZ(); f++) {
                                if (!checkPlaceable(firstlocation.getWorld().getBlockAt(i, j, f).getType())) {
                                    return false;
                                }
                            }
                        } else {
                            for (int f = firstlocation.getBlockZ(); f<secondlocation.getBlockZ(); f++) {
                                if (!checkPlaceable(firstlocation.getWorld().getBlockAt(i, j, f).getType())) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            for (int i = firstlocation.getBlockX(); i<secondlocation.getBlockX(); i++) {
                if (firstlocation.getBlockY() > secondlocation.getBlockY()) {
                    for (int j = secondlocation.getBlockY(); j<firstlocation.getBlockY(); j++) {
                        if (firstlocation.getBlockZ() > secondlocation.getBlockZ()) {
                            for (int f = secondlocation.getBlockZ(); f<firstlocation.getBlockZ(); f++) {
                                if (!checkPlaceable(firstlocation.getWorld().getBlockAt(i, j, f).getType())) {
                                    return false;
                                }
                            }
                        } else {
                            for (int f = firstlocation.getBlockZ(); f<secondlocation.getBlockZ(); f++) {
                                if (!checkPlaceable(firstlocation.getWorld().getBlockAt(i, j, f).getType())) {
                                    return false;
                                }
                            }
                        }
                    }
                } else {
                    for (int j = firstlocation.getBlockY(); j<secondlocation.getBlockY(); j++) {
                        if (firstlocation.getBlockZ() > secondlocation.getBlockZ()) {
                            for (int f = secondlocation.getBlockZ(); f<firstlocation.getBlockZ(); f++) {
                                if (!checkPlaceable(firstlocation.getWorld().getBlockAt(i, j, f).getType())) {
                                    return false;
                                }
                            }
                        } else {
                            for (int f = firstlocation.getBlockZ(); f<secondlocation.getBlockZ(); f++) {
                                if (!checkPlaceable(firstlocation.getWorld().getBlockAt(i, j, f).getType())) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public static TeamUtil getTeamEntity(Entity entity) {
        for (TeamUtil team : TeamUtil.values()) {
            if (team == TeamUtil.NONE) continue;
            if (entity.getScoreboardTags().contains(team.getName())) {
                return team;
            }
        }
        return TeamUtil.NONE;
    }

    public static Location convertToBlockLocation(Location location) {
        return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static void setupPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getScoreboardTags().contains("player")) continue;
            setupPlayerFromStart(player);
            player.getEnderChest().clear();
        }
    }

    public static boolean atBase(Location location, TeamUtil team) {
        Pair<Location, Location> baseBounds = Constants.getBaseLocations(team);
        return MathUtil.isBetweenTwoLocations(location, baseBounds.getFirst(), baseBounds.getSecond());
    }

    public static void setupPlayerFromStart(Player player) {
        TeamUtil team = getTeam(player);
        player.setFallDistance(0.0f);
        player.teleport(Constants.getTeamGeneratorLocation(team));
        player.setVelocity(new Vector());
        PlayerInventory inventory = player.getInventory();
        player.setGameMode(GameMode.SURVIVAL);
        clearEffects(player);
        player.setHealth(20);
        inventory.clear();
        player.getEnderChest().clear();
        inventory.setItem(8, MenuUtil.setItemName(Material.COMPASS, ChatColor.GREEN + "Compass " + ChatColor.GRAY + "(Right Click)"));
        inventory.addItem(createWithUnbreakable(Material.WOODEN_SWORD));
        inventory.setHelmet(setColor(createWithUnbreakable(Material.LEATHER_HELMET), team.getColor()));
        inventory.setChestplate(setColor(createWithUnbreakable(Material.LEATHER_CHESTPLATE), team.getColor()));
        inventory.setLeggings(setColor(createWithUnbreakable(Material.LEATHER_LEGGINGS), team.getColor()));
        inventory.setBoots(setColor(createWithUnbreakable(Material.LEATHER_BOOTS), team.getColor()));
    }

    public static ItemStack setColor(ItemStack itemStack, Color color) {
        LeatherArmorMeta itemMeta = (LeatherArmorMeta) itemStack.getItemMeta();
        itemMeta.setColor(color);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private static ItemMeta createUnbreakableItemMeta(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setUnbreakable(true);
        return itemMeta;
    }

    public static ItemStack createWithUnbreakable(Material material) {
        ItemStack itemStack = new ItemStack(material);
        itemStack.setItemMeta(createUnbreakableItemMeta(itemStack));
        return itemStack;
    }

    public static TeamUtil getMostEmptyTeam() {
        int best = 999999;
        for (TeamInfo team : PlayerInfo.getTeams().values()) {
            int numberOfPeopleOnTeam = team.numberOfPeopleOnTeam();
            if (numberOfPeopleOnTeam < best) {
                best = numberOfPeopleOnTeam;
            }
        }
        for (TeamUtil team : TeamUtil.values()) {
            if (team.equals(TeamUtil.NONE)) continue;
            if (PlayerInfo.getTeamInfo(team).numberOfPeopleOnTeam() == best) {
                return team;
            }
        }
        throw new UnsupportedOperationException("No team found");
    }

    public static void spawnArmorStands() {
        for (Materials material : Constants.getGenMaterials()) {
            for (Location location : Constants.getGenLocations(material)) {
                spawnArmorStandsGenerator(material, location);
            }
        }
    }

    public static void spawnArmorStandsGenerator(Materials materials, Location location) {
        List<ArmorStand> armorStands = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            ArmorStand armorStand = spawnArmorStand(location.clone().add(0,1.25 + i*0.4,0));
            armorStand.addScoreboardTag("generator");
            armorStand.addScoreboardTag(materials.getName());
            armorStands.add(armorStand);
        }
        armorStands.get(0).addScoreboardTag("spinning");
        setName(armorStands.get(1), ChatColor.YELLOW + "Spawns in " + ChatColor.RED + 30 + ChatColor.YELLOW + " seconds");
        armorStands.get(1).addScoreboardTag("timed");
        setName(armorStands.get(2),materials.getColor() + ChatColor.BOLD + materials.getName());
        setName(armorStands.get(3),ChatColor.YELLOW + "Tier " + ChatColor.RED + RomanNumber.toRoman(1));
        armorStands.get(3).addScoreboardTag("tier");
        switch (materials) {
            case DIAMOND:
                armorStands.get(0).getEquipment().setHelmet(new ItemStack(Material.DIAMOND_BLOCK));
                break;
            case EMERALD:
                armorStands.get(0).getEquipment().setHelmet(new ItemStack(Material.EMERALD_BLOCK));
                break;
            default:
                throw new UnsupportedOperationException("Non-Recognized Material");
        }
    }

    private static void setName(ArmorStand armorStand, String name) {
        armorStand.setCustomName(name);
        armorStand.setCustomNameVisible(true);
    }

    public static ArmorStand spawnArmorStand(Location location) {
        ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class);
        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setGravity(false);
        return armorStand;
    }

    public static void broadcastMessage(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getScoreboardTags().contains("player")) {
                player.sendMessage(message);
            }
        }
    }

    public static TeamUtil getMostEmptyAliveTeam() {
        int best = 999999;
        for (TeamInfo team : PlayerInfo.getTeams().values()) {
            if (!team.hasBed()) continue;
            int numberOfPeopleOnTeam = team.numberOfPeopleOnTeam();
            if (numberOfPeopleOnTeam < best) {
                best = numberOfPeopleOnTeam;
            }
        }
        for (TeamUtil team : TeamUtil.values()) {
            if (team.equals(TeamUtil.NONE)) continue;
            if (!PlayerInfo.getTeamInfo(team).hasBed()) continue;
            if (PlayerInfo.getTeamInfo(team).numberOfPeopleOnTeam() == best) {
                return team;
            }
        }
        return null;
    }

    public static boolean locationsEqualIgnoringRot(Location firstlocation, Location secondlocation) {
        return new Location(firstlocation.getWorld(), firstlocation.getX(), firstlocation.getY(), firstlocation.getZ()).equals(new Location(secondlocation.getWorld(), secondlocation.getX(), secondlocation.getY(), secondlocation.getZ()));
    }

    public static ItemStack createPotion(Color color, PotionEffectType potionEffectType, int duration, int amplifier) {
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta itemMeta = (PotionMeta) item.getItemMeta();
        itemMeta.setColor(color);
        itemMeta.addCustomEffect(new PotionEffect(potionEffectType,duration,amplifier, false, false), true);
        item.setItemMeta(itemMeta);
        return item;
    }

    /*
     * @return Item stack of single item with enchantment
     */
    public static ItemStack createWithEnchantmentAndUnbreakable(Enchantment enchantment, int enchantLevel, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = createUnbreakableItemMeta(item);
        itemMeta.addEnchant(enchantment, enchantLevel, true);
        item.setItemMeta(itemMeta);
        return item;
    }

    /*
     * @return Item stack of single item with enchantment
     */
    public static ItemStack createWithEnchantment(Enchantment enchantment, int enchantLevel, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.addEnchant(enchantment, enchantLevel, true);
        item.setItemMeta(itemMeta);
        return item;
    }

    /*
     * @return Item stack of single item with enchantment
     */
    public static ItemStack createWithEnchantment(Enchantment enchantment, Material material) {
        return createWithEnchantment(enchantment, 1, material);
    }

    /*
     * @return Item stack of single item with enchantment at level 1
     */
    public static ItemStack createWithEnchantmentAndUnbreakable(Enchantment enchantment, Material material) {
        return createWithEnchantmentAndUnbreakable(enchantment, 1, material);
    }

    /*
     * @return Item stack of single item with EFFICIENCY
     */
    public static ItemStack createWithEnchantmentAndUnbreakable(int enchantLevel, Material material) {
        return createWithEnchantmentAndUnbreakable(Enchantment.EFFICIENCY, enchantLevel, material);
    }

    /*
    * @return Item stack of single item with EFFICIENCY 1
     */
    public static ItemStack createWithEnchantmentAndUnbreakable(Material material) {
        return createWithEnchantmentAndUnbreakable(Enchantment.EFFICIENCY, 1, material);
    }

    /*
     * @return Item stack now with added enchantment
     */
    public static ItemStack addEnchantment(Enchantment enchantment, int enchantLevel, ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.addEnchant(enchantment, enchantLevel, true);
        item.setItemMeta(itemMeta);
        return item;
    }

    /*
     * @return Item stack now with added enchantment at level 1
     */
    public static ItemStack addEnchantment(Enchantment enchantment, ItemStack item) {
        return addEnchantment(enchantment,1,item);
    }

    /*
     * @return Clears players inventory of wooden swords
     */
    public static void clearOfWoodSwords(PlayerInventory inventory) {
        clear(inventory, Material.WOODEN_SWORD);
    }

    /*
     * @return Clears players inventory of certain materials
     */
    public static void clear(PlayerInventory inventory, Material... materials) {
        for (Material material : materials) {
            inventory.remove(material);
            if (inventory.getItemInOffHand().getType() == material) {
                inventory.setItemInOffHand(new ItemStack(Material.AIR));
            }
        }
    }

    /*
     * @return Checks if a players inventory contains any of the following materials
     */
    public static boolean contains(PlayerInventory inventory, Material... materials) {
        for (Material material : materials) {
            if (inventory.contains(material) || inventory.getItemInOffHand().getType() == material) {
                return true;
            }
        }
        return false;
    }

    /*
     * @return Checks if a players inventory contains any of the following materials
     */
    public static boolean containsIgnoringSlot(PlayerInventory inventory, int slotToIgnore, Material... materials) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i == slotToIgnore) continue;
            ItemStack item = inventory.getItem(i);
            if (item == null) continue;
            for (Material material : materials) {
                if (material == item.getType()) {
                    return true;
                }
            }
        }
        for (Material material : materials) {
            if (inventory.getItemInOffHand().getType() == material) {
                return true;
            }
        }
        return false;
    }

    public static void fill(Location firstLocation, Location secondLocation, BlockData blockData, BlockManager blockManager) {
            List<Location> locations = convertToLowerOptions(firstLocation,secondLocation);
            for (int i = locations.get(0).getBlockX(); i <= locations.get(1).getBlockX(); i++) {
                for (int j = locations.get(0).getBlockY(); j <= locations.get(1).getBlockY(); j++) {
                    for (int k = locations.get(0).getBlockZ(); k <= locations.get(1).getBlockZ(); k++) {
                        Location location = new Location(firstLocation.getWorld(), i, j, k);
                        firstLocation.getWorld().setBlockData(location,blockData);
                        if (blockManager != null) blockManager.add(location);
                    }
                }
            }
    }

    private static List<Location> convertToLowerOptions(Location... locations) {
        if (locations.length < 1) throw new UnsupportedOperationException("No arguments given");
        List<Integer> x = convertToLowerOptions(locations[0].getBlockX(),locations[1].getBlockX());
        List<Integer> y = convertToLowerOptions(locations[0].getBlockY(),locations[1].getBlockY());
        List<Integer> z = convertToLowerOptions(locations[0].getBlockZ(),locations[1].getBlockZ());
        List<Location> Locations = new ArrayList<>();
        Location firstLocation = new Location(locations[0].getWorld(), x.get(0), y.get(0), z.get(0));
        Location secondLocation = new Location(locations[1].getWorld(), x.get(1), y.get(1), z.get(1));
        Locations.add(firstLocation);
        Locations.add(secondLocation);
        return Locations;
    }

    private static List<Integer> convertToLowerOptions(Integer... ints) {
        int first;
        int second;
        if (ints[0] < ints[1]) {
            first = ints[0];
            second = ints[1];
        } else {
            second = ints[0];
            first = ints[1];
        }
        List<Integer> integers = new ArrayList<>();
        integers.add(first);
        integers.add(second);
        return integers;
    }

    /*
     * @return Checks if a players inventory contains any of the following materials
     */
    public static boolean containsIgnoringOffhand(PlayerInventory inventory, Material... materials) {
        for (Material material : materials) {
            if (inventory.contains(material)) return true;
        }
        return false;
    }

    public static String makeFirstCapital(String string) {
        char[] chars = string.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }
}
