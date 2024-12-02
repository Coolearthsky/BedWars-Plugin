package me.coolearth.coolearth.config;

import me.coolearth.coolearth.menus.menuItems.Items;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Config {

    private static JavaPlugin m_coolearth;

    public static void register(JavaPlugin coolearth) {
        m_coolearth = coolearth;
    }

    // Save items for a specific player to the configuration file
    public static void saveItemsToYml(UUID playerID, List<Items> items, String filename) {
        // Get the data folder of the plugin
        File dataFolder = m_coolearth.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        // Create the file
        File file = new File(dataFolder, filename);
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Get the player's section (using player's name or UUID)
        String playerPath = "players." + playerID.toString() + ".items";

        // Clear existing data for this player
        config.set(playerPath, null);

        // Save each item to the player's section in the configuration
        for (int i = 0; i < items.size(); i++) {
            Items item = items.get(i);
            String path = playerPath + "." + i + ".";

            config.set(path + "name", item.getName());
        }

        // Save the configuration to file
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load items for a specific player from the YML file
    public static List<Items> loadItemsFromYml(UUID playerID, String filename) {
        List<Items> items = new ArrayList<>();

        // Get the file
        File file = new File(m_coolearth.getDataFolder(), filename);
        if (!file.exists()) {
            return items;
        }

        // Load the configuration
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Check if the player has a section in the config
        String playerPath = "players." + playerID.toString() + ".items";
        if (config.contains(playerPath)) {
            // Iterate through the player's items
            for (String key : config.getConfigurationSection(playerPath).getKeys(false)) {
                String path = playerPath + "." + key + ".";

                String name = config.getString(path + "name");

                items.add(Items.get(name)); // Assuming Items.get(name) correctly returns an item object.
            }
        }

        return items;
    }
}
