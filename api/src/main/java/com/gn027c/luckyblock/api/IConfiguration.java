package com.gn027c.luckyblock.api;

import java.util.List;

public interface IConfiguration {
    String getString(String path);
    String getString(String path, String def);
    int getInt(String path);
    boolean getBoolean(String path);
    java.util.List<String> getStringList(String path);
}

