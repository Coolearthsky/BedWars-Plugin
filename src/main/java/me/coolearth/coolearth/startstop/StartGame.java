package me.coolearth.coolearth.startstop;

import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.scoreboard.Board;
import me.coolearth.coolearth.timed.Generators;
import me.coolearth.coolearth.timed.VoidCheck;

public class StartGame {
    private final Generators m_generators;
    private final PlayerInfo m_playerInfo;
    private final Board m_board;
    private final VoidCheck m_voidCheck;
    public StartGame(Generators generators, PlayerInfo playerInfo, Board board, VoidCheck voidCheck) {
        m_generators = generators;
        m_playerInfo = playerInfo;
        m_board = board;
        m_voidCheck = voidCheck;
    }

    public void start() {
        Util.resetTeams();
        m_playerInfo.startPlayers();
        Util.setupPlayers();
        m_playerInfo.startTeamGenerators();
        m_generators.start();
        m_board.startScoreboards();
        m_voidCheck.startVoidCheck();
        Util.spawnArmorStands();
        GlobalVariables.gameStarted();
    }
}
