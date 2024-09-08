package me.coolearth.coolearth.commands;

import me.coolearth.coolearth.listener.ShopListener;
import me.coolearth.coolearth.Util.Team;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.players.TeamInfo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Upgrade implements CommandExecutor {

    private final PlayerInfo m_shopListener;
    public Upgrade(PlayerInfo shopListener) {
        m_shopListener = shopListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        TeamInfo teamInfo = m_shopListener.getTeamInfo(Team.get(args[0]));
        if (teamInfo == null) {
            sender.sendMessage("Match is not on");
            return false;
        }
        if (args.length > 1) {
            if (args[1].equals("1")) {
                teamInfo.setUpgradeLevel(0);
            } else if(args[1].equals("2")) {
                teamInfo.setUpgradeLevel(1);
            }else if(args[1].equals("3")) {
                teamInfo.setUpgradeLevel(2);
            }else if(args[1].equals("4")) {
                teamInfo.setUpgradeLevel(3);
            }else if(args[1].equals("5")) {
                teamInfo.setUpgradeLevel(4);
            } else {
                throw new UnsupportedOperationException("Out of bounds for upgrades");
            }
        } else {
            teamInfo.upgradeToNextLevel();
        }
        return true;
    }
}
