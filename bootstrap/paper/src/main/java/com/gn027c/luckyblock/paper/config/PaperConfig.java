package com.gn027c.luckyblock.paper.config;

import com.gn027c.luckyblock.api.IConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;

public class PaperConfig implements IConfiguration {
    private final FileConfiguration config;

    public PaperConfig(FileConfiguration config) {
        this.config = config;
    }

    @Override
    public String getString(String path) {
        return config.getString(path);
    }

    @Override
    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    @Override
    public int getInt(String path) {
        return config.getInt(path);
    }

    @Override
    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    @Override
    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }
}

