package com.gn027c.luckyblock.core.reward;

public enum Rarity {
    COMMON(50),
    RARE(30),
    EPIC(15),
    LEGENDARY(5);

    private final int dropChance;

    Rarity(int dropChance) {
        this.dropChance = dropChance;
    }

    public int getDropChance() {
        return dropChance;
    }
}

