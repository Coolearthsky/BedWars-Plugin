package me.coolearth.coolearth.listener;

import me.coolearth.coolearth.shops.ShopManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class ShopListener implements Listener {
    private final ShopManager m_shopManager;

    public ShopListener(ShopManager shopManager) {
        m_shopManager = shopManager;
    }

    @EventHandler
    public void onInventoryPickup(EntityPickupItemEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) return;
        m_shopManager.onInventoryPickup((Player) event.getEntity());
    }

    @EventHandler
    public void onShopClick(PlayerInteractEntityEvent event) {
        Entity rightClicked = event.getRightClicked();
        if (rightClicked.getType() != EntityType.VILLAGER) return;
        event.setCancelled(true);
        m_shopManager.onShopClick(rightClicked.getScoreboardTags(),event.getPlayer());
    }

    @EventHandler
    public void onShopClose(InventoryCloseEvent event) {
        m_shopManager.onShopClose((Player) event.getPlayer());
    }

    @EventHandler
    public void onShopItemClick(InventoryClickEvent event) {
        m_shopManager.onShopItemClick(event);
    }
}
