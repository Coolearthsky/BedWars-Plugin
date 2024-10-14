package me.coolearth.coolearth.startstop;

import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.block.BlockManager;
import me.coolearth.coolearth.global.GlobalVariables;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.scoreboard.Board;
import me.coolearth.coolearth.timed.*;
import org.bukkit.entity.*;

public class StopGame {
    public final BlockManager m_blockManager;
    public final Generators m_generators;
    public final TargetManager m_targetManager;
    public final EggManager m_eggManager;
    public final VoidCheck m_voidCheck;

    public StopGame(BlockManager blockManager, Generators generators, EggManager eggManager, TargetManager targetManager, VoidCheck voidCheck) {
        m_blockManager = blockManager;
        m_generators = generators;
        m_eggManager = eggManager;
        m_targetManager= targetManager;
        m_voidCheck = voidCheck;
    }

    public void stop() {
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
