package me.coolearth.coolearth.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodListener implements Listener {
    @EventHandler
    public void onLooseFood(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
}
