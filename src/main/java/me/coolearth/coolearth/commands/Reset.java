package me.coolearth.coolearth.commands;

import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.listener.BlockListener;
import me.coolearth.coolearth.listener.ShopListener;
import me.coolearth.coolearth.startstop.StartGame;
import me.coolearth.coolearth.startstop.StopGame;
import me.coolearth.coolearth.timed.Generators;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;

public class Reset implements CommandExecutor {

    private final StartGame m_startGame;
    private final StopGame m_stopGame;
    public Reset(StartGame startGame, StopGame stopGame) {
        m_startGame = startGame;
        m_stopGame = stopGame;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this");
            return false;
        }
        m_stopGame.stop();
        m_startGame.start();
        return true;
    }
}
