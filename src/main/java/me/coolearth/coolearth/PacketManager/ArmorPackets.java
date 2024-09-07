package me.coolearth.coolearth.PacketManager;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import me.coolearth.coolearth.Util.Util;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class ArmorPackets {

    private static ProtocolManager m_manager;

    public static void register() {
        m_manager = ProtocolLibrary.getProtocolManager();
        Plugin coolearth = Bukkit.getPluginManager().getPlugin("coolearth");
        if (coolearth == null) {
            Bukkit.getLogger().warning("NO JAVAPLUGIN");
            return;
        }
        m_manager.addPacketListener(new PacketAdapter(coolearth, PacketType.Play.Server.ENTITY_EQUIPMENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Entity read = packet.getEntityModifier(event).read(0);
                if (!(read instanceof Player)) return;
                if (checkInvalid((Player) read, event.getPlayer())) return;
                setInvisArmor(packet);
            }
        });

        m_manager.addPacketListener(new PacketAdapter(coolearth, PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Entity read = packet.getEntityModifier(event).read(0);
                if (!(read instanceof Player)) return;
                if (checkInvalid((Player) read, event.getPlayer())) return;
                setInvis(read.getEntityId());
            }
        });
    }

    public static void setInvis(int EntityId) {
        PacketContainer fakeEquipmentPacket = m_manager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        fakeEquipmentPacket.getIntegers().write(0, EntityId);
        setInvisArmor(fakeEquipmentPacket);
    }

    private static void setInvisArmor(PacketContainer fakeEquipmentPacket) {
        ArrayList<Pair<EnumWrappers.ItemSlot, ItemStack>> value = new ArrayList<>();
        value.add(new Pair<>(EnumWrappers.ItemSlot.MAINHAND, new ItemStack(Material.AIR)));
        value.add(new Pair<>(EnumWrappers.ItemSlot.OFFHAND, new ItemStack(Material.AIR)));
        value.add(new Pair<>(EnumWrappers.ItemSlot.FEET, new ItemStack(Material.AIR)));
        value.add(new Pair<>(EnumWrappers.ItemSlot.LEGS, new ItemStack(Material.AIR)));
        value.add(new Pair<>(EnumWrappers.ItemSlot.CHEST, new ItemStack(Material.AIR)));
        value.add(new Pair<>(EnumWrappers.ItemSlot.HEAD, new ItemStack(Material.AIR)));
        value.add(new Pair<>(EnumWrappers.ItemSlot.BODY, new ItemStack(Material.AIR)));
        fakeEquipmentPacket.getSlotStackPairLists().write(0, value);
    }

    private static boolean checkInvalid(Player sourcePlayer, Player targetPlayer) {
        return !sourcePlayer.hasPotionEffect(PotionEffectType.INVISIBILITY) || sourcePlayer.getScoreboardTags().contains("player") || Util.getTeam(targetPlayer) == Util.getTeam(sourcePlayer);
    }
}
