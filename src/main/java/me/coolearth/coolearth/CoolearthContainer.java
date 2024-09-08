package me.coolearth.coolearth;

import me.coolearth.coolearth.PacketManager.ArmorPackets;
import me.coolearth.coolearth.block.BlockManager;
import me.coolearth.coolearth.commands.*;
import me.coolearth.coolearth.listener.*;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.startstop.StartGame;
import me.coolearth.coolearth.startstop.StopGame;
import me.coolearth.coolearth.timed.EggManager;
import me.coolearth.coolearth.timed.Generators;
import me.coolearth.coolearth.timed.SpongeManager;
import me.coolearth.coolearth.timed.TargetManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CoolearthContainer {

    //Timed events
    private final SpongeManager spongeManager;
    private final Generators generators;
    private final TargetManager targetManager;
    private final EggManager eggManager;

    //Listeners
    private final BlockListener blockListener;
    private final ShopListener shopListener;
    private final DeathManager deathListener;
    private final InventoryManager inventoryListener;
    private final ProjectileListener projectileListener;
    private final FoodListener foodListener;
    private final MobListener mobListener;

    private final PlayerInfo playerInfo;
    private final BlockManager blockManager;
    private final StopGame stopGame;
    private final StartGame startGame;

    public CoolearthContainer(JavaPlugin coolearth) {

        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            ArmorPackets.register();
        }

        //Block manager, manages certain areas and where players can place blocks
        blockManager = new BlockManager();

        //Timed events
        generators = new Generators(coolearth);
        eggManager = new EggManager(coolearth);
        spongeManager = new SpongeManager(coolearth);
        targetManager = new TargetManager(coolearth);

        //Player info
        playerInfo = new PlayerInfo(coolearth);

        //Listeners
        blockListener = new BlockListener(playerInfo, blockManager, spongeManager);
        inventoryListener = new InventoryManager();
        deathListener = new DeathManager(playerInfo);
        shopListener = new ShopListener(playerInfo, coolearth);
        foodListener = new FoodListener(playerInfo);
        projectileListener = new ProjectileListener(eggManager, blockManager);
        mobListener = new MobListener(targetManager);

        //Registering event listeners
        Bukkit.getPluginManager().registerEvents(blockListener,coolearth);
        Bukkit.getPluginManager().registerEvents(mobListener,coolearth);
        Bukkit.getPluginManager().registerEvents(shopListener,coolearth);
        Bukkit.getPluginManager().registerEvents(deathListener,coolearth);
        Bukkit.getPluginManager().registerEvents(inventoryListener,coolearth);
        Bukkit.getPluginManager().registerEvents(foodListener,coolearth);
        Bukkit.getPluginManager().registerEvents(projectileListener,coolearth);

        //Start and stopgame controllers
        startGame = new StartGame(generators, playerInfo);
        stopGame = new StopGame(playerInfo, blockManager, generators, eggManager, targetManager);

        //Creating start/stop commands
        Reset reset = new Reset(startGame, stopGame);
        Start start = new Start(startGame);
        Stop stop = new Stop(stopGame);

        //Team based commands
        Upgrade upgrade = new Upgrade(playerInfo);
        Teams teams = new Teams(playerInfo);

        //Registering commands
        coolearth.getCommand("reset").setExecutor(reset);
        coolearth.getCommand("upgrade").setExecutor(upgrade);
        coolearth.getCommand("teams").setExecutor(teams);
        coolearth.getCommand("start").setExecutor(start);
        coolearth.getCommand("stopMatch").setExecutor(stop);
    }

    public void onDisable() {
        stopGame.stop();
        Bukkit.getLogger().info("DISABLING");
    }
}
