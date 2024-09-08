package me.coolearth.coolearth.commands;

import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.timed.Generators;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartRunnables implements CommandExecutor {

    private final PlayerInfo m_playerInfo;
    private final Generators m_generators;

    public StartRunnables(PlayerInfo playerInfo, Generators generators) {
        m_playerInfo = playerInfo;
        m_generators = generators;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        m_generators.start();
        return true;
    }
}
