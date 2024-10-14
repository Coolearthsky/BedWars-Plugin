package me.coolearth.coolearth.startstop;

import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.scoreboard.Board;
import me.coolearth.coolearth.timed.Generators;
import me.coolearth.coolearth.timed.HealthUpdate;
import me.coolearth.coolearth.timed.VoidCheck;

public class StartGame {
    private final Generators m_generators;
    private final VoidCheck m_voidCheck;
    private final HealthUpdate m_healthUpdate;
    public StartGame(Generators generators, VoidCheck voidCheck, HealthUpdate healthUpdate) {
        m_generators = generators;
        m_voidCheck = voidCheck;
        m_healthUpdate = healthUpdate;
    }

    public void start() {
        Util.resetTeams();
        PlayerInfo.startPlayers();
        Util.setupPlayers();
        PlayerInfo.startTeamGenerators();
        m_generators.start();
        Board.startScoreboards();
        m_healthUpdate.fixHealth();
        m_voidCheck.startVoidCheck();
        Util.spawnArmorStands();
        Util.spawnShopsAndUpgrades();
        Util.broadcastStartMessage();
        GlobalVariables.gameStarted();
    }
}
