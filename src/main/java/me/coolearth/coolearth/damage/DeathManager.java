package me.coolearth.coolearth.damage;

import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.Constants;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.players.PlayerAddons;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.players.TeamInfo;
import me.coolearth.coolearth.scoreboard.Board;
import me.coolearth.coolearth.startstop.StopGame;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


public class DeathManager {

    private final PlayerInfo m_playerInfo;
    private final Board m_board;
    private final StopGame m_stopGame;
    private final JavaPlugin m_coolearth;
    public DeathManager(PlayerInfo playerInfo, Board board, StopGame stopGame, JavaPlugin coolearth) {
        m_playerInfo = playerInfo;
        m_board = board;
        m_stopGame = stopGame;
        m_coolearth = coolearth;
    }

    public void onDamage(EntityDamageEvent event) {
        if (!event.getEntityType().equals(EntityType.PLAYER)) return;
        DamageType damageType = event.getDamageSource().getDamageType();
        if (damageType.equals(DamageType.PLAYER_EXPLOSION)) {
            event.setDamage(0);
        }
        else if (damageType.equals(DamageType.EXPLOSION)) {
            event.setDamage(2);
        }
    }

    public void onPlayerDeath(Player player) {
        if (!player.getScoreboardTags().contains("player") || !GlobalVariables.isGameActive()) return;
        PlayerAddons playersInfo = m_playerInfo.getPlayersInfo(player);
        if (playersInfo == null) return;
        playersInfo.onDeath();
        if (!playersInfo.isAlive()) {
            TeamUtil team1 = playersInfo.getTeam();
            Util.broadcastMessage(team1.getChatColor() + player.getName() + ChatColor.GRAY + " died " + "§b" + ChatColor.BOLD + "FINAL KILL!");
            player.sendMessage(ChatColor.RED + "You have been eliminated!");
            Location teamGeneratorLocation = Constants.getTeamGeneratorLocation(team1);
            boolean sendmessage = false;
            for (ItemStack item : player.getEnderChest()) {
                if (item == null) continue;
                sendmessage = true;
                Util.spawnItem(teamGeneratorLocation, item);
            }
            m_board.updateSpecificTeamsScoreboards(team1);
            if (!m_playerInfo.getTeamInfo(team1).isAnyoneOnTeamAlive()) {
                Util.broadcastMessage("\n" + ChatColor.BOLD + "TEAM ELIMINATED > " + team1.getChatColor() + team1.getName() + " Team " + ChatColor.RED + "has been eliminated!"+ "\n");
                Util.broadcastMessage("");
            }
            TeamUtil aliveTeam = null;
            for (TeamInfo team : m_playerInfo.getTeams().values()) {
                if (team.isAnyoneOnTeamAlive()) {
                    if (aliveTeam != null) {
                        if (sendmessage) {
                            Player killer = player.getKiller();
                            if (killer != null && !player.equals(killer)) killer.sendMessage(ChatColor.GREEN + "Contents of " + player.getName() + "'s Ender Chest have been dropped into their fountain.");
                        }
                        return;
                    }
                    aliveTeam = team.getTeam();
                }
            }
            if (aliveTeam != null) {
                Util.broadcastMessage(aliveTeam.getChatColor() + aliveTeam.getName() + ChatColor.GRAY + " team won!");
            } else {
                Util.broadcastMessage("Game end");
            }
            m_stopGame.stop();
        } else {
            int totalTime = 5;
            deathMessage(player, totalTime);
            for (int i = 1; i < totalTime; i++) {
                int finalI = i;
                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        deathMessage(player, totalTime  - finalI);
                    }
                };
                runnable.runTaskLater(m_coolearth, i*20);
            }
            Util.broadcastMessage(playersInfo.getTeam().getChatColor() + player.getName() + ChatColor.GRAY + " died");
        }
    }

    private void deathMessage(Player player, int time) {
        String respawn = ChatColor.YELLOW + "You will respawn in " + ChatColor.RED + time + " " + ChatColor.YELLOW + "seconds!";
        player.sendTitle(ChatColor.RED + "YOU DIED!", respawn,0, 21,0);
        player.sendMessage(respawn);
    }
}
