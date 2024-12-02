package me.coolearth.coolearth.commands;

import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.startstop.GameController;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Start implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (GlobalVariables.isGameActive()) {
            sender.sendMessage("The game must be inactive to activate this command");
            return false;
        }
        GameController.start();
        return true;
    }
  }
