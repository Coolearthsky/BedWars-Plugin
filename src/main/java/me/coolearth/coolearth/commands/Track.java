package me.coolearth.coolearth.commands;

import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.players.PlayerAddons;
import me.coolearth.coolearth.players.PlayerInfo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Track implements CommandExecutor {
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
        if (args.length != 1) {
            sender.sendMessage("You must have an argument of the team you are tracking");
            return false;
        }
        TeamUtil team = TeamUtil.get(Util.makeFirstCapital(args[0].toLowerCase()));
        if (team == null) {
            sender.sendMessage("You must have an argument of a real team");
            return false;
        }
        PlayerAddons playersInfo = PlayerInfo.getPlayersInfo((Player) sender);
        if (team.equals(playersInfo.getTeam())) {
            sender.sendMessage("The team cannot be your own");
            return false;
        }
        if (playersInfo.trackingTeam(team)) {
            playersInfo.stopTrackingTeam(team);
        } else {
            playersInfo.addTeamToTracker(team);
        }
        return true;
    }
}
