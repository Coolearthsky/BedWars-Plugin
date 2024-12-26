package me.coolearth.coolearth.PacketManager;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import me.coolearth.coolearth.Util.TeamUtil;
import me.coolearth.coolearth.Util.Util;
import me.coolearth.coolearth.global.GlobalVariables;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ArmorPackets {

    private static ProtocolManager m_manager;
    private static JavaPlugin m_coolearth;
    private static Map<UUID,BukkitRunnable> m_runnable;

    public static void register(JavaPlugin coolearth) {
        m_manager = ProtocolLibrary.getProtocolManager();
        m_coolearth = coolearth;
        m_runnable = new HashMap<>();
        if (m_coolearth == null) {
            Bukkit.getLogger().warning("NO JAVAPLUGIN");
            return;
        }

        m_manager.addPacketListener(new PacketAdapter(m_coolearth, PacketType.Play.Client.CHAT) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                event.setCancelled(true);
                if (!player.getScoreboardTags().contains("player") || !GlobalVariables.isGameActive()) {
                    Util.broadcastMessage(ChatColor.WHITE + player.getName() + ": " + event.getPacket().getStrings().read(0));
                    return;
                }
                TeamUtil team = Util.getTeam(player);
                Util.broadcastMessage(team.getChatColor() + "[" + team.getName().toUpperCase() + "] " + ChatColor.WHITE + player.getName() + ": " + event.getPacket().getStrings().read(0));
            }
        });

        m_manager.addPacketListener(new PacketAdapter(m_coolearth, PacketType.Play.Server.ENTITY_EQUIPMENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Entity read = packet.getEntityModifier(event).read(0);
                if (!(read instanceof Player)) return;
                Player player = (Player) read;
                if (!m_runnable.containsKey(player.getUniqueId())) return;
                setInvisArmor(packet);
            }
        });
    }

    public static void setInvis(Player player) {
        PacketContainer packetContainer  = m_manager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packetContainer.getIntegers().write(0, player.getEntityId());
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> value = new ArrayList<>();
        value.add(new Pair<>(EnumWrappers.ItemSlot.MAINHAND, player.getInventory().getItemInMainHand()));
        value.add(new Pair<>(EnumWrappers.ItemSlot.OFFHAND, player.getInventory().getItemInOffHand()));
        value.add(new Pair<>(EnumWrappers.ItemSlot.FEET, new ItemStack(Material.AIR)));
        value.add(new Pair<>(EnumWrappers.ItemSlot.LEGS, new ItemStack(Material.AIR)));
        value.add(new Pair<>(EnumWrappers.ItemSlot.CHEST, new ItemStack(Material.AIR)));
        value.add(new Pair<>(EnumWrappers.ItemSlot.HEAD, new ItemStack(Material.AIR)));
        packetContainer.getSlotStackPairLists().write(0,value);
        setInvisArmor(packetContainer);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (Util.getTeam(p).equals(Util.getTeam(player)) || !p.getScoreboardTags().contains("player")) {
                continue;
            }
            m_manager.sendServerPacket(p, packetContainer);
        }
        if (m_runnable.containsKey(player.getUniqueId())) {
            BukkitRunnable runnable = m_runnable.get(player.getUniqueId());
            runnable.cancel();
            m_runnable.remove(player.getUniqueId());
        }
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                stopLoop(player);
            }
        };
        runnable.runTaskLater(m_coolearth, 20*30+1);
        m_runnable.put(player.getUniqueId(), runnable);
    }

    public static void stopLoop(Player player) {
        if (!m_runnable.containsKey(player.getUniqueId())) return;
        PacketContainer packetContainer = m_manager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packetContainer.getIntegers().write(0, player.getEntityId());
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> value = new ArrayList<>();
        value.add(new Pair<>(EnumWrappers.ItemSlot.MAINHAND, player.getInventory().getItemInMainHand()));
        value.add(new Pair<>(EnumWrappers.ItemSlot.OFFHAND, player.getInventory().getItemInOffHand()));
        value.add(new Pair<>(EnumWrappers.ItemSlot.FEET, player.getInventory().getBoots()));
        value.add(new Pair<>(EnumWrappers.ItemSlot.LEGS, player.getInventory().getLeggings()));
        value.add(new Pair<>(EnumWrappers.ItemSlot.CHEST, player.getInventory().getChestplate()));
        value.add(new Pair<>(EnumWrappers.ItemSlot.HEAD, player.getInventory().getHelmet()));
        packetContainer.getSlotStackPairLists().write(0, value);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (Util.getTeam(p).equals(Util.getTeam(player)) || !p.getScoreboardTags().contains("player")) {
                continue;
            }
            m_manager.sendServerPacket(p, packetContainer);
        }
        m_runnable.get(player.getUniqueId()).cancel();
        m_runnable.remove(player.getUniqueId());
    }

    private static void setInvisArmor(PacketContainer fakeEquipmentPacket) {
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> value = fakeEquipmentPacket.getSlotStackPairLists().read(0);
        if (value.size() > 5) {
            value.set(2,new Pair<>(EnumWrappers.ItemSlot.FEET, new ItemStack(Material.AIR)));
            value.set(3,new Pair<>(EnumWrappers.ItemSlot.LEGS, new ItemStack(Material.AIR)));
            value.set(4,new Pair<>(EnumWrappers.ItemSlot.CHEST, new ItemStack(Material.AIR)));
            value.set(5,new Pair<>(EnumWrappers.ItemSlot.HEAD, new ItemStack(Material.AIR)));
        } else {
            value.add(new Pair<>(EnumWrappers.ItemSlot.FEET, new ItemStack(Material.AIR)));
            value.add(new Pair<>(EnumWrappers.ItemSlot.LEGS, new ItemStack(Material.AIR)));
            value.add(new Pair<>(EnumWrappers.ItemSlot.CHEST, new ItemStack(Material.AIR)));
            value.add(new Pair<>(EnumWrappers.ItemSlot.HEAD, new ItemStack(Material.AIR)));
        }
        fakeEquipmentPacket.getSlotStackPairLists().write(0, value);
    }
}
