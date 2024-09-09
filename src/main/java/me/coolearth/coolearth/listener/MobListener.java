package me.coolearth.coolearth.listener;

import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.timed.TargetManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

public class MobListener implements Listener {
    private final TargetManager m_targetManager;

    public MobListener(TargetManager targetManager) {
        m_targetManager = targetManager;
    }

    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;
        if (item.getType().equals(Material.IRON_GOLEM_SPAWN_EGG) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            Block clickedBlock = event.getClickedBlock();
            Block spawnBlock = clickedBlock.getRelative(event.getBlockFace());
            Location spawnLocation = spawnBlock.getLocation().add(0.5, 0, 0.5);
            Player player = event.getPlayer();
            Mob entity = spawnLocation.getWorld().spawn(spawnLocation, IronGolem.class);
            m_targetManager.addLivingEntity(entity,player);
            if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                item.setAmount(item.getAmount() - 1);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntityType().equals(EntityType.SNOWBALL)) {
            Projectile entity = event.getEntity();
            ProjectileSource shooter = entity.getShooter();
            if (!(shooter instanceof Player)) return;
            Player player = (Player) shooter;
            Mob mob = entity.getWorld().spawn(entity.getLocation(), Silverfish.class);
            m_targetManager.addLivingEntity(mob, player);
        }
    }
    @EventHandler
    public void onDeath(EntityDeathEvent event){
        if (event.getEntityType().equals(EntityType.IRON_GOLEM)){
            m_targetManager.mobDies((Mob) event.getEntity());
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!event.getEntityType().equals(EntityType.PLAYER)) return;
        if (event.getDamager().getScoreboardTags().contains(Util.getTeam((Player) event.getEntity()).getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent event){
        if (event.getEntityType().equals(EntityType.IRON_GOLEM) || event.getEntityType().equals(EntityType.SILVERFISH)){
            LivingEntity target = event.getTarget();
            if (target == null) return;
            TeamUtil team = Util.getTeamEntity(target);
            if (team.equals(Util.getTeamEntity(event.getEntity())) || team.equals(TeamUtil.NONE)) {
                event.setCancelled(true);
            }
        }
    }
}