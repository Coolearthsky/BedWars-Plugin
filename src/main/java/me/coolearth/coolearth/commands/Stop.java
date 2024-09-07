package me.coolearth.coolearth.commands;

import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.listener.BlockListener;
import me.coolearth.coolearth.listener.ShopListener;
import me.coolearth.coolearth.startstop.StopGame;
import me.coolearth.coolearth.timed.Generators;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;

public class Stop implements CommandExecutor {

    private final StopGame m_stopGame;
    public Stop(StopGame stopGame) {
        m_stopGame = stopGame;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this");
            return false;
        }
        m_stopGame.stop();
        return true;
    }
}
