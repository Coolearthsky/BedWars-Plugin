package me.coolearth.coolearth.commands;

import me.coolearth.coolearth.listener.ShopListener;
import me.coolearth.coolearth.Util.Team;
import me.coolearth.coolearth.players.PlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Teams implements CommandExecutor {

    private final PlayerInfo m_listener;
    public Teams(PlayerInfo listener) {
        m_listener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this");
            return false;
        }
        if (args.length <= 1) {
            Player player = (Player) sender;
            Team team = Team.get(args[0]);
            if (team != Team.NONE) {
                m_listener.updateTeams(player, team);
                return true;
            } else {
                return false;
            }
        }
        else if (args.length == 2) {
            Player player = null;
            for (Player players : Bukkit.getOnlinePlayers()) {
                if (args[0].equals(players.getName())) {
                    player = players;
                } else {
                    return false;
                }
            }
            if (player == null) return false;
            Team team = Team.get(args[1]);
            if (team != Team.NONE) {
                m_listener.updateTeams(player,team);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
