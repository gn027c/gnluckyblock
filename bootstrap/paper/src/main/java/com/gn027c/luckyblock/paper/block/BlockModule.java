package com.gn027c.luckyblock.paper.block;

import com.gn027c.luckyblock.core.manager.AbstractModule;
import com.gn027c.luckyblock.paper.gnluckyblock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlockModule extends AbstractModule {
    private final gnluckyblock plugin;
    private final Map<Location, LuckyBlockData> luckyBlockLocations = new HashMap<>();
    private File dataFile;

    public static class LuckyBlockData {
        public String id;
        public int luck;

        public LuckyBlockData(String id, int luck) {
            this.id = id;
            this.luck = luck;
        }
    }

    public BlockModule(gnluckyblock plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        loadData();
    }

    @Override
    public void onDisable() {
        saveData();
    }

    public void addLocation(Location loc, String id, int luck) {
        luckyBlockLocations.put(loc, new LuckyBlockData(id, luck));
    }

    public void addLocation(Location loc) {
        addLocation(loc, "classic", 0);
    }

    public LuckyBlockData getData(Location loc) {
        return luckyBlockLocations.get(loc);
    }

    public boolean removeLocation(Location loc) {
        return luckyBlockLocations.remove(loc) != null;
    }

    public boolean isLuckyBlock(Location loc) {
        return luckyBlockLocations.containsKey(loc);
    }

    private void loadData() {
        if (!dataFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = config.getConfigurationSection("blocks");
        if (section == null) return;

        for (String locKey : section.getKeys(false)) {
            Location loc = stringToLocation(locKey);
            if (loc != null) {
                String id = section.getString(locKey + ".id", "classic");
                int luck = section.getInt(locKey + ".luck", 0);
                luckyBlockLocations.put(loc, new LuckyBlockData(id, luck));
            }
        }
    }

    private void saveData() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<Location, LuckyBlockData> entry : luckyBlockLocations.entrySet()) {
            String locStr = locationToString(entry.getKey());
            config.set("blocks." + locStr + ".id", entry.getValue().id);
            config.set("blocks." + locStr + ".luck", entry.getValue().luck);
        }
        try {
            config.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String locationToString(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private Location stringToLocation(String str) {
        String[] parts = str.split(",");
        if (parts.length != 4) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        return new Location(world, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
    }
}

