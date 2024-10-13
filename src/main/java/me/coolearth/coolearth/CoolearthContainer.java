package me.coolearth.coolearth;

import me.coolearth.coolearth.PacketManager.ArmorPackets;
import me.coolearth.coolearth.block.BlockManager;
import me.coolearth.coolearth.commands.*;
import me.coolearth.coolearth.damage.DeathManager;
import me.coolearth.coolearth.listener.*;
import me.coolearth.coolearth.players.PlayerAddons;
import me.coolearth.coolearth.players.PlayerInfo;
import me.coolearth.coolearth.players.PlayerJoinLeaveManager;
import me.coolearth.coolearth.scoreboard.Board;
import me.coolearth.coolearth.shops.ShopManager;
import me.coolearth.coolearth.startstop.StartGame;
import me.coolearth.coolearth.startstop.StopGame;
import me.coolearth.coolearth.timed.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CoolearthContainer {

    //Timed events
    private final SpongeManager spongeManager;
    private final Generators generators;
    private final TargetManager targetManager;
    private final EggManager eggManager;
    private final VoidCheck voidCheck;
    private final ArmorStands armorStands;

    //Listeners
    private final BlockListener blockListener;
    private final ShopListener shopListener;
    private final DeathListener deathListener;
    private final InventoryManager inventoryListener;
    private final PlayerListener playerListener;
    private final ProjectileListener projectileListener;
    private final FoodListener foodListener;
    private final MobListener mobListener;

    //Scoreboard
    private final Board board;

    //Managers
    private final MobManager mobManager;
    private final ShopManager shopManager;
    private final BlockManager blockManager;
    private final DeathManager deathManager;
    private final StopGame stopGame;
    private final StartGame startGame;
    private final PlayerJoinLeaveManager playerManager;

    public CoolearthContainer(JavaPlugin coolearth) {

        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            ArmorPackets.register(coolearth);
        }
        //Block manager, manages certain areas and where players can place blocks
        blockManager = new BlockManager();

        PlayerInfo.register(coolearth);

        //Scoreboard
        board = new Board();

        //Timed events
        generators = new Generators(coolearth, board);
        eggManager = new EggManager(coolearth);
        spongeManager = new SpongeManager(coolearth);
        mobManager = new MobManager(coolearth);
        targetManager = new TargetManager(coolearth, mobManager);
        voidCheck = new VoidCheck(coolearth);
        armorStands = new ArmorStands(coolearth);


        //Start and stopgame controllers
        startGame = new StartGame(generators, board, voidCheck);
        stopGame = new StopGame(board, blockManager, generators, eggManager, targetManager, voidCheck);

        //Death manager
        shopManager = new ShopManager( coolearth);
        deathManager = new DeathManager(board, stopGame, coolearth);
        playerManager = new PlayerJoinLeaveManager( generators, coolearth);

        //Listeners
        blockListener = new BlockListener( blockManager, spongeManager, board);
        inventoryListener = new InventoryManager();
        deathListener = new DeathListener(deathManager);
        playerListener = new PlayerListener(playerManager);
        shopListener = new ShopListener(shopManager);
        foodListener = new FoodListener();
        projectileListener = new ProjectileListener(eggManager, blockManager);
        mobListener = new MobListener(targetManager);

        //Registering event listeners
        Bukkit.getPluginManager().registerEvents(blockListener,coolearth);
        Bukkit.getPluginManager().registerEvents(mobListener,coolearth);
        Bukkit.getPluginManager().registerEvents(shopListener,coolearth);
        Bukkit.getPluginManager().registerEvents(deathListener,coolearth);
        Bukkit.getPluginManager().registerEvents(playerListener,coolearth);
        Bukkit.getPluginManager().registerEvents(inventoryListener,coolearth);
        Bukkit.getPluginManager().registerEvents(foodListener,coolearth);
        Bukkit.getPluginManager().registerEvents(projectileListener,coolearth);

        //Creating start/stop commands
        Reset reset = new Reset(startGame, stopGame);
        Start start = new Start(startGame);
        Stop stop = new Stop(stopGame);

        //Team based commands
        Upgrade upgrade = new Upgrade();
        Teams teams = new Teams(board);



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
