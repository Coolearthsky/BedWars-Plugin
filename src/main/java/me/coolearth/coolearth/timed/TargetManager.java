package me.coolearth.coolearth.timed;

import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TargetManager {
    private final JavaPlugin m_coolearth;
    private final MobManager m_mobManager;
    private final Map<Mob, BukkitRunnable> m_runnableMap = new HashMap<>();

    public TargetManager(JavaPlugin coolearth, MobManager mobManager){
        m_coolearth = coolearth;
        m_mobManager = mobManager;
    }

    public void addLivingEntity(Mob entity, Player player, long livingTime) {
        m_mobManager.placeMob(entity, livingTime);
        TeamUtil team = Util.getTeam(player);
        if (team == TeamUtil.NONE) return;
        entity.addScoreboardTag(team.getName());
        BukkitRunnable runnable = new BukkitRunnable() {
            private final TeamUtil m_team = team;
            @Override
            public void run() {
                LivingEntity currentTarget = null;
                int radius = 14;
                List<Entity> nearbyEntities = entity.getNearbyEntities(radius, radius, radius);
                double closestDistanceSquared = 100000;
                for (Entity badEntity : nearbyEntities) {
                    if (!(badEntity instanceof LivingEntity)) {
                        continue;
                    }
                    if (badEntity instanceof Player) {
                        if (!((Player) badEntity).getGameMode().equals(GameMode.SURVIVAL)) {
                            continue;
                        }
                    }
                    TeamUtil opponentTeam = Util.getTeamEntity(badEntity);
                    if (opponentTeam == m_team || opponentTeam == TeamUtil.NONE) continue;
                    LivingEntity livingEntity = (LivingEntity) badEntity;
                    if (livingEntity.hasPotionEffect(PotionEffectType.INVISIBILITY)) continue;
                    double distanceSquared = entity.getLocation().distanceSquared(livingEntity.getLocation());

                    if (distanceSquared < closestDistanceSquared) {
                        closestDistanceSquared = distanceSquared;
                        currentTarget = livingEntity;
                    }
                }
                entity.setTarget(currentTarget);
            }
        };
        runnable.runTaskTimer(m_coolearth, 0,0);
        m_runnableMap.put(entity, runnable);
    }

    public void mobDies(Mob egg) {
        m_runnableMap.get(egg).cancel();
        m_runnableMap.remove(egg);
    }

    public void resetAllLoops() {
        for (BukkitRunnable runnable : m_runnableMap.values()) {
            runnable.cancel();
        }
        m_runnableMap.clear();
    }
}
