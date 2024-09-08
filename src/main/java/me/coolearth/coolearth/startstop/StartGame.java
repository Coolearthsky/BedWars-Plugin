package me.coolearth.coolearth.startstop;

import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.timed.Generators;

public class StartGame {
    private final Generators m_generators;
    private final PlayerInfo m_playerInfo;
    public StartGame(Generators generators, PlayerInfo playerInfo) {
        m_generators = generators;
        m_playerInfo = playerInfo;
    }

    public void start() {
        Util.resetTeams();
        m_playerInfo.startPlayers();
        Util.setupPlayers();
        m_playerInfo.startTeamGenerators();
        m_generators.start();
        GlobalVariables.gameStarted();
    }
}
