package me.coolearth.coolearth.startstop;

import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.block.BlockManager;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.scoreboard.Board;
import me.coolearth.coolearth.timed.*;
import org.bukkit.entity.*;

public class GameController {
    public static BlockManager m_blockManager;
    public static Generators m_generators;
    public static TargetManager m_targetManager;
    public static EggManager m_eggManager;
    public static VoidCheck m_voidCheck;
    public static HealthUpdate m_healthUpdate;

    public static void register(BlockManager blockManager, Generators generators, EggManager eggManager, TargetManager targetManager, VoidCheck voidCheck, HealthUpdate healthUpdate) {
        m_blockManager = blockManager;
        m_generators = generators;
        m_eggManager = eggManager;
        m_targetManager= targetManager;
        m_voidCheck = voidCheck;
        m_healthUpdate = healthUpdate;
    }

    public static void start() {
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

    public static void stop() {
        GlobalVariables.gameEnded();
        PlayerInfo.stopLoops();
        m_generators.resetAllLoops();
        m_eggManager.resetAllLoops();
        m_targetManager.resetAllLoops();
        m_blockManager.resetMap();
        Board.endScoreboards();
        Util.clearChests();
        Util.emptyPlayers();
        m_voidCheck.stopVoidCheck();
        Util.killAllEntities(Item.class, IronGolem.class, Silverfish.class, Snowball.class, Egg.class, ArmorStand.class, Arrow.class, Villager.class);
    }
}
