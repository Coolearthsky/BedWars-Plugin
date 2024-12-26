package me.coolearth.coolearth.players;

import me.coolearth.coolearth.PacketManager.ArmorPackets;
import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.Constants;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.timed.Generators;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.UUID;

public class PlayerJoinLeaveManager {
    private final JavaPlugin m_coolearth;
    private final Generators m_generators;

    public PlayerJoinLeaveManager(Generators generators, JavaPlugin coolearth) {
        m_coolearth = coolearth;
        m_generators = generators;
    }

    public void onPlayerJoin(Player player1) {
        if (player1.getLastPlayed() == 0) {
            player1.addScoreboardTag("player");
        }
        UUID player = player1.getUniqueId();
        Bukkit.getLogger().info(player1.getAddress().getAddress().getAddress().toString());
        Bukkit.getLogger().info(player1.getAddress().toString());
        if (!player1.getScoreboardTags().contains("player") || !GlobalVariables.isGameActive()) {
            player1.setGameMode(GameMode.ADVENTURE);
            player1.setFallDistance(0);
            player1.setPlayerListHeader(ChatColor.AQUA + "Welcome to Bedwars!");
            player1.setVelocity(new Vector());
            player1.teleport(Constants.getPregameSpawn());
            Util.broadcastMessage(ChatColor.YELLOW + player1.getName() + " has joined!");
            return;
        }
        if (PlayerInfo.getPlayers().containsKey(player)) {
            m_generators.updateSafe(player1);
            PlayerAddons playersInfo = PlayerInfo.getPlayersInfo(player1);
            if (playersInfo.getAlive() && player1.getGameMode().equals(GameMode.SPECTATOR)) {
                playersInfo.onRespawn();
            }
            Util.broadcastMessage(playersInfo.getTeam().getChatColor() + player1.getName() + ChatColor.GRAY + " reconnected.");
            return;
        };
        TeamUtil team = Util.getMostEmptyAliveTeam();
        if (team == null) {
            player1.setPlayerListHeader(ChatColor.AQUA + "Welcome to Bedwars!");
            player1.setGameMode(GameMode.ADVENTURE);
            player1.setFallDistance(0);
            player1.setVelocity(new Vector());
            player1.teleport(Constants.getPregameSpawn());
            return;
        }
        Util.broadcastMessage(team.getChatColor() + player1.getName() + ChatColor.GRAY + " reconnected.");
        PlayerAddons value = new PlayerAddons(m_coolearth, team, player);
        PlayerInfo.getPlayers().put(player, value);
        TeamInfo teamInfo = PlayerInfo.getTeamInfo(team);
        teamInfo.createUpgradesForPlayer(player);
        teamInfo.getPeopleOnTeam().put(player, value);
        Util.removeTeams(player1);
        player1.addScoreboardTag(team.getName());
        Util.setupPlayerFromStart(player1);
        player1.sendMessage(Util.getStartMessage());
        m_generators.updateSafe(player1);
    }

    public void onPlayerQuit(Player player) {
        if (!player.getScoreboardTags().contains("player") || !GlobalVariables.isGameActive()) {
            Util.broadcastMessage(ChatColor.YELLOW + player.getName() + " has quit!");
            return;
        }
        Util.broadcastMessage(Util.getTeam(player).getChatColor() + player.getName() + ChatColor.GRAY + " disconnected.");
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        ArmorPackets.stopLoop(player);
    }
}