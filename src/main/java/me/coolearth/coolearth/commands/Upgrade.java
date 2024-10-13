package me.coolearth.coolearth.commands;

import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.players.TeamInfo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Upgrade implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!GlobalVariables.isGameActive()) {
            sender.sendMessage("The game must be active to activate this command");
            return false;
        }
        TeamInfo teamInfo = PlayerInfo.getTeamInfo(TeamUtil.get(Util.makeFirstCapital(args[0].toLowerCase())));
        if (teamInfo == null) {
            sender.sendMessage("Unknown argument");
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
