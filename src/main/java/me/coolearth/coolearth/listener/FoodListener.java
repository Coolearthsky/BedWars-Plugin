package me.coolearth.coolearth.listener;

import me.coolearth.coolearth.PacketManager.ArmorPackets;
import me.coolearth.coolearth.players.PlayerAddons;
import me.coolearth.coolearth.players.PlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FoodListener implements Listener {

    private final PlayerInfo m_playerInfo;
    public FoodListener(PlayerInfo playerInfo) {
        m_playerInfo = playerInfo;
    }

    @EventHandler
    public void onLooseFood(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack item = event.getItem();
        Material type = item.getType();
        if (type == Material.POTION) {
            inventory.setItem(event.getHand(), new ItemStack(Material.AIR));
            PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
            Bukkit.getLogger().info("working2423233444." + potionMeta.getItemName() + " W"+  potionMeta.getDisplayName());
            if (potionMeta.getDisplayName().equals(ChatColor.GREEN + "Invisibility Potion")) {
                Bukkit.getLogger().info("working2423233.");
                ArmorPackets.setInvis(player);
            }
            player.addPotionEffects(potionMeta.getCustomEffects());
        }
        else if (type == Material.GOLDEN_APPLE) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20*5, 1, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20*120, 0, false, false));
            item.setAmount(item.getAmount()-1);
            inventory.setItem(event.getHand(), item);
        }
        else if (type == Material.MILK_BUCKET) {
            inventory.setItem(event.getHand(), new ItemStack(Material.AIR));
            PlayerAddons playerAddons = m_playerInfo.getPlayers().get(player);
            if (playerAddons != null) {
                playerAddons.drankMilk();
            }
        }
    }

}
