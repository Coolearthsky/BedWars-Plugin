package me.coolearth.coolearth.commands;

import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.startstop.GameController;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Stop implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!GlobalVariables.isGameActive()) {
            sender.sendMessage("The game must be active to activate this command");
            return false;
        }
        GameController.stop();
        return true;
    }
}
