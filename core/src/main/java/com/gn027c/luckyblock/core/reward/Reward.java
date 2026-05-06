package com.gn027c.luckyblock.core.reward;

import java.util.List;
import java.util.Map;

public class Reward {
    private final String id;
    private final RewardType type;
    private final Rarity rarity;
    private final int weight;
    private final int luck;
    private final Map<String, Object> data;
    private final String announcement;

    public Reward(String id, RewardType type, Rarity rarity, int weight, int luck, Map<String, Object> data, String announcement) {
        this.id = id;
        this.type = type;
        this.rarity = rarity;
        this.weight = weight;
        this.luck = luck;
        this.data = data;
        this.announcement = announcement;
    }

    public String getId() {
        return id;
    }

    public RewardType getType() {
        return type;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public int getWeight() {
        return weight;
    }

    public int getLuck() {
        return luck;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String getAnnouncement() {
        return announcement;
    }
}

