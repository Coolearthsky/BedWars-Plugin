package me.coolearth.coolearth;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Coolearth extends JavaPlugin {
    private CoolearthContainer coolearthContainer;
    private static Coolearth instance;

    @Override
    public void onEnable() {
        Bukkit.getLogger().info("ENABLING");
        coolearthContainer = new CoolearthContainer(this);
    }

    @Override
    public void onDisable() {
        coolearthContainer.onDisable();
    }

    public static Coolearth getInstance() {
        return instance;
    }
}
