package me.coolearth.coolearth.commands;

import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.startstop.StartGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Start implements CommandExecutor {

    private final StartGame m_startGame;
    public Start(StartGame startGame) {
        m_startGame = startGame;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (GlobalVariables.isGameActive()) {
            sender.sendMessage("The game must be inactive to activate this command");
            return false;
        }
        m_startGame.start();
        return true;
    }
  }
