package me.coolearth.coolearth.commands;

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
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this");
            return false;
        }
        m_startGame.start();
        return true;
    }
  }
