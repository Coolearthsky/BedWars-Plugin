package me.coolearth.coolearth.commands;

import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.scoreboard.Board;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Teams implements CommandExecutor {

    private final PlayerInfo m_playerInfo;
    private final Board m_board;
    public Teams(PlayerInfo playerInfo, Board board) {
        m_playerInfo = playerInfo;
        m_board = board;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this");
            return false;
        }
        if (!GlobalVariables.isGameActive()) {
            sender.sendMessage("The game must be active to activate this command");
            return false;
        }
        if (args.length <= 1) {
            Player player = (Player) sender;
            TeamUtil team = TeamUtil.get(Util.makeFirstCapital(args[0].toLowerCase()));
            if (team != TeamUtil.NONE) {
                m_playerInfo.updateTeams(player, team);
                m_board.updateAllScoreboards();
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
            TeamUtil team = TeamUtil.get(Util.makeFirstCapital(args[1].toLowerCase()));
            if (team != TeamUtil.NONE) {
                m_playerInfo.updateTeams(player,team);
                m_board.updateAllScoreboards();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
