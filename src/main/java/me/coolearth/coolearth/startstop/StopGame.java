package me.coolearth.coolearth.startstop;

import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.block.BlockManager;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.timed.EggManager;
import me.coolearth.coolearth.timed.Generators;
import me.coolearth.coolearth.timed.TargetManager;
import org.bukkit.entity.*;

public class StopGame {
    public final BlockManager m_blockManager;
    public final Generators m_generators;
    public final PlayerInfo m_playerInfo;
    public final TargetManager m_targetManager;
    public final EggManager m_eggManager;

    public StopGame(PlayerInfo playerInfo, BlockManager blockManager, Generators generators, EggManager eggManager, TargetManager targetManager) {
        m_blockManager = blockManager;
        m_generators = generators;
        m_eggManager = eggManager;
        m_targetManager= targetManager;
        m_playerInfo = playerInfo;
    }

    public void stop() {
        m_playerInfo.stopLoops();
        m_playerInfo.stopAllPlayers();
        m_generators.resetAllLoops();
        m_eggManager.resetAllLoops();
        m_targetManager.resetAllLoops();
        m_blockManager.resetMap();
        Util.clearAllEffects();
        Util.killAllEntities(Item.class, IronGolem.class, Silverfish.class, Snowball.class, Egg.class);
    }
}
