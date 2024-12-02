package me.coolearth.coolearth.commands;

import me.coolearth.coolearth.startstop.GameController;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Reset implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GameController.stop();
        GameController.start();
        return true;
    }
}
