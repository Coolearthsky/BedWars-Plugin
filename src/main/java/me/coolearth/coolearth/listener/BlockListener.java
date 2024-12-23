package me.coolearth.coolearth.listener;

import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.block.BlockManager;
import me.coolearth.coolearth.global.Constants;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.players.PlayerAddons;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.players.TeamInfo;
import me.coolearth.coolearth.scoreboard.Board;
import me.coolearth.coolearth.timed.SpongeManager;
import org.bukkit.*;

import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener {
    private final BlockManager m_blockManager;
    private final SpongeManager m_spongeManager;

    public BlockListener(BlockManager blockManager, SpongeManager spongeManager) {
        m_blockManager = blockManager;
        m_spongeManager = spongeManager;
    }

    @EventHandler
    public void onChestClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.getScoreboardTags().contains("player") || !GlobalVariables.isGameActive() || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        if (clickedBlock.getType().equals(Material.CHEST)) {
            TeamUtil team = Constants.getChestTeam(clickedBlock.getLocation());
            TeamInfo teamInfo = PlayerInfo.getTeamInfo(team);
            if (team != Util.getTeam(player) && (teamInfo.isAnyoneOnTeamAlive() || teamInfo.hasBed())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot open this Chest as the " + team.getChatColor() + team.getName() + " Team " + ChatColor.RED + "has not been eliminated!");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!player.getScoreboardTags().contains("player")) return;
        if (!m_blockManager.checkIfPlacable(event.getBlockReplacedState())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can't place blocks here!");
            return;
        }
        Block block = event.getBlock();
        if (block.getType() == Material.TNT) {
            World world = block.getWorld();
            Location loc = block.getLocation();
            world.setBlockData(loc, Material.AIR.createBlockData());
            world.spawn(new Location(world, loc.getBlockX() + 0.5, loc.getBlockY(), loc.getBlockZ() + 0.5), TNTPrimed.class);
            return;
        }
        if (block.getType() == Material.SPONGE) {
            m_spongeManager.placeSponge(block);
            return;
        }
        if (block.getType() == Material.CHEST) {
            if (Util.buildPopUpBase(Util.getTeam(player), event.getBlockPlaced().getLocation(), player.getLocation().getYaw(),m_blockManager)) return;
            event.setCancelled(true);
            return;
        }
        m_blockManager.add(block);
    }

    @EventHandler
    public void onFlow(BlockFromToEvent event) {
        Block toBlock = event.getToBlock();
        if (!m_blockManager.checkIfPlacable(toBlock)){
            event.setCancelled(true);
        } else {
            m_blockManager.add(toBlock.getLocation());
        }
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;
        Block clickedBlock = event.getClickedBlock();
        if (item.getType().equals(Material.WATER_BUCKET) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (clickedBlock != null) {
                if (clickedBlock.getType().equals(Material.CHEST) && !event.getPlayer().isSneaking()) return;
            }
            Block block = clickedBlock.getRelative(event.getBlockFace());
            event.setCancelled(true);
            if (!m_blockManager.checkIfPlacable(block)) return;
            m_blockManager.add(block);
            block.getWorld().setBlockData(block.getLocation(), Material.WATER.createBlockData());
            Player player = event.getPlayer();
            if (!player.getGameMode().equals(GameMode.CREATIVE)) player.getInventory().setItem(event.getHand(),new ItemStack(Material.AIR));
        } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            if (item.getType().equals(Material.COMPASS)) {
                if (PlayerInfo.getTeamInfo(Util.getTeam(event.getPlayer())).numberOfPeopleOnTeam() == 1) {
                    event.getPlayer().openInventory(GlobalVariables.getCompassMenu());
                } else {
                    event.getPlayer().openInventory(GlobalVariables.getCompassMenuMult());
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Util.protectedExplotion(event.blockList(), event.getLocation(), m_blockManager);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Util.protectedExplotion(event.blockList(), event.getBlock().getLocation().clone().add(.5,0,.5), m_blockManager);
    }

    @EventHandler
    public void noBedEnter(PlayerBedEnterEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!player.getScoreboardTags().contains("player")) return;
        Material type = event.getBlock().getType();
        if (isBed(type)) {
            event.setDropItems(false);
            if (type == Material.RED_BED ) {
                TeamUtil red = TeamUtil.RED;
                if (player.getScoreboardTags().contains(red.getName())) {
                    player.sendMessage(ChatColor.RED + "You can't break your own bed!");
                    event.setCancelled(true);
                } else {
                    TeamInfo teamInfo = PlayerInfo.getTeamInfo(red);
                    if (teamInfo == null) {
                        player.sendMessage(ChatColor.RED + "You can't break blocks here!");
                        event.setCancelled(true);
                        return;
                    }
                    bedBreak(teamInfo, player);
                }
            }
            else if (type == Material.BLUE_BED ) {
                TeamUtil blue = TeamUtil.BLUE;
                if (player.getScoreboardTags().contains(blue.getName())) {
                    player.sendMessage(ChatColor.RED + "You can't break your own bed!");
                    event.setCancelled(true);
                } else {
                    TeamInfo teamInfo = PlayerInfo.getTeamInfo(blue);
                    if (teamInfo == null) {
                        player.sendMessage(ChatColor.RED + "You can't break blocks here!");
                        event.setCancelled(true);
                        return;
                    }
                    bedBreak(teamInfo, player);
                }
            }
            else if (type == Material.YELLOW_BED ) {
                TeamUtil yellow = TeamUtil.YELLOW;
                if (player.getScoreboardTags().contains(yellow.getName())) {
                    player.sendMessage(ChatColor.RED + "You can't break your own bed!");
                    event.setCancelled(true);
                } else {
                    TeamInfo teamInfo = PlayerInfo.getTeamInfo(yellow);
                    if (teamInfo == null) {
                        player.sendMessage(ChatColor.RED + "You can't break blocks here!");
                        event.setCancelled(true);
                        return;
                    }
                    bedBreak(teamInfo, player);
                }
            }
            else if (type == Material.LIME_BED ) {
                TeamUtil green = TeamUtil.GREEN;
                if (player.getScoreboardTags().contains(green.getName())) {
                    player.sendMessage(ChatColor.RED + "You can't break your own bed!");
                    event.setCancelled(true);
                } else {
                    TeamInfo teamInfo = PlayerInfo.getTeamInfo(green);
                    if (teamInfo == null) {
                        player.sendMessage(ChatColor.RED + "You can't break blocks here!");
                        event.setCancelled(true);
                        return;
                    }
                    bedBreak(teamInfo, player);
                }
            }
            for (PlayerAddons playerAddons : PlayerInfo.getPlayers().values()) {
                playerAddons.updateWoolState();
            }
        } else if (m_blockManager.contains(event.getBlock())){
            m_blockManager.remove(event.getBlock());
        } else {
            if (type == Material.FIRE) return;
            player.sendMessage(ChatColor.RED + "You can't break blocks here!");
            event.setCancelled(true);
        }
    }

    private void bedBreak(TeamInfo teamInfo, Player player) {
        teamInfo.bedBreak(player);
        Board.updatePlayersScoreboardBreakBed(player);
        Board.updateAllTeamsScoreboardsOfSpecificTeamsBed(teamInfo.getTeam());
    }

    private static boolean isBed(Material type) {
        return type == Material.LIME_BED || type == Material.RED_BED || type == Material.BLUE_BED || type == Material.YELLOW_BED;
    }
 }
