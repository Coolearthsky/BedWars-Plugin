package me.coolearth.coolearth.listener;

import me.coolearth.coolearth.block.BlockManager;
import me.coolearth.coolearth.timed.EggManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ProjectileListener implements Listener {

    private final BlockManager m_blockManager;
    private final EggManager m_egg;
    public ProjectileListener(EggManager egg, BlockManager blockManager) {
        m_egg = egg;
        m_blockManager = blockManager;
    }

    @EventHandler
    public void onEggThrow(ProjectileLaunchEvent event) {
        if (event.getEntityType().equals(EntityType.EGG)) {
            m_egg.newEgg(event.getEntity(), m_blockManager);
        }
    }

    @EventHandler
    public void onEggThrow(PlayerEggThrowEvent event) {
        event.setHatching(false);
    }

    private void summonFireball(Player player) {
        player.launchProjectile(Fireball.class).setVelocity(player.getLocation().getDirection().multiply(0.5));
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        EntityType entity = event.getEntityType();
        if(entity.equals(EntityType.FIREBALL)) {
            Fireball f = (Fireball) event.getEntity();
            Location location = f.getLocation();
            location.getWorld().createExplosion(location, 2, true, true );
        } else if (entity.equals(EntityType.EGG)) {
            m_egg.eggDies(event.getEntity());
        }
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;
        if (item.getType().equals(Material.FIRE_CHARGE)) {
            if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                event.setCancelled(true);
                Player player = event.getPlayer();
                if (player.getGameMode().equals(GameMode.CREATIVE)) {
                    summonFireball(player);
                    return;
                }
                item.setAmount(item.getAmount() - 1);
                player.getInventory().setItem(event.getHand(), item);
                summonFireball(player);
            }
        }
    }
}
