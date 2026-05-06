package com.gn027c.luckyblock.core.reward;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class RewardManager {
    public enum RewardMode {
        TIERED, ABSOLUTE
    }

    private RewardMode mode = RewardMode.TIERED;
    private final Map<Rarity, List<Reward>> rewardsByRarity = new HashMap<>();
    private final Map<Rarity, Integer> totalWeightByRarity = new HashMap<>();

    public void setMode(RewardMode mode) {
        this.mode = mode;
    }

    public RewardManager() {
        for (Rarity rarity : Rarity.values()) {
            rewardsByRarity.put(rarity, new ArrayList<>());
            totalWeightByRarity.put(rarity, 0);
        }
    }

    public void registerReward(Reward reward) {
        Rarity rarity = reward.getRarity();
        rewardsByRarity.get(rarity).add(reward);
        totalWeightByRarity.put(rarity, totalWeightByRarity.get(rarity) + reward.getWeight());
    }

    public void clearRewards() {
        for (Rarity rarity : Rarity.values()) {
            rewardsByRarity.get(rarity).clear();
            totalWeightByRarity.put(rarity, 0);
        }
    }

    /**
     * Determines a random reward based on the current mode (TIERED or ABSOLUTE).
     */
    public Reward getRandomReward() {
        if (mode == RewardMode.ABSOLUTE) {
            List<Reward> allRewards = getRewards();
            if (allRewards.isEmpty()) return null;
            return allRewards.get(ThreadLocalRandom.current().nextInt(allRewards.size()));
        }

        // TIERED logic
        int chance = ThreadLocalRandom.current().nextInt(100);
        Rarity selectedRarity = Rarity.COMMON;

        if (chance < Rarity.LEGENDARY.getDropChance()) {
            selectedRarity = Rarity.LEGENDARY;
        } else if (chance < Rarity.LEGENDARY.getDropChance() + Rarity.EPIC.getDropChance()) {
            selectedRarity = Rarity.EPIC;
        } else if (chance < Rarity.LEGENDARY.getDropChance() + Rarity.EPIC.getDropChance() + Rarity.RARE.getDropChance()) {
            selectedRarity = Rarity.RARE;
        }

        // Ensure the selected rarity has rewards, otherwise fallback
        List<Reward> rarityRewards = rewardsByRarity.get(selectedRarity);
        if (rarityRewards.isEmpty()) {
            // Try to find any rarity that has rewards
            for (Rarity r : Rarity.values()) {
                if (!rewardsByRarity.get(r).isEmpty()) {
                    selectedRarity = r;
                    rarityRewards = rewardsByRarity.get(r);
                    break;
                }
            }
        }
        
        if (rarityRewards.isEmpty()) return null;

        int totalWeight = totalWeightByRarity.get(selectedRarity);
        int randomValue = ThreadLocalRandom.current().nextInt(totalWeight);
        int currentWeight = 0;

        for (Reward reward : rarityRewards) {
            currentWeight += reward.getWeight();
            if (randomValue < currentWeight) {
                return reward;
            }
        }
        
        return rarityRewards.get(rarityRewards.size() - 1);
    }

    /**
     * Picks a reward taking additional Luck into account.
     * @param blockLuck Luck index from -100 to 100
     * @param threshold Luck threshold for filtering
     */
    public Reward getRandomReward(int blockLuck, int threshold) {
        if (mode == RewardMode.ABSOLUTE) {
            List<Reward> filtered = new ArrayList<>();
            for (Reward r : getRewards()) {
                if (r.getLuck() >= (blockLuck - threshold)) {
                    filtered.add(r);
                }
            }
            if (filtered.isEmpty()) return getRandomReward(); // Fallback
            return filtered.get(ThreadLocalRandom.current().nextInt(filtered.size()));
        }

        // TIERED logic with Luck Modifier
        List<Reward> allRewards = getRewards();
        List<Reward> possibleRewards = new ArrayList<>();
        long totalModifiedWeight = 0;

        for (Reward reward : allRewards) {
            // Step 1: Filtering
            if (reward.getLuck() < (blockLuck - threshold)) {
                continue;
            }

            // Step 2: Weight Modification
            // Formula: Weight_final = Weight_base * (1 + (Luck_block * Luck_reward) / 1000)
            double multiplier = 1.0 + (blockLuck * reward.getLuck() / 1000.0);
            long modifiedWeight = Math.max(1, Math.round(reward.getWeight() * multiplier));
            
            possibleRewards.add(reward);
            totalModifiedWeight += modifiedWeight;
        }

        if (possibleRewards.isEmpty()) return getRandomReward(); // Fallback to basic random if no rewards match

        long randomValue = ThreadLocalRandom.current().nextLong(totalModifiedWeight);
        long currentWeight = 0;

        for (int i = 0; i < possibleRewards.size(); i++) {
            Reward reward = possibleRewards.get(i);
            double multiplier = 1.0 + (blockLuck * reward.getLuck() / 1000.0);
            long modifiedWeight = Math.max(1, Math.round(reward.getWeight() * multiplier));
            
            currentWeight += modifiedWeight;
            if (randomValue < currentWeight) {
                return reward;
            }
        }

        return possibleRewards.get(possibleRewards.size() - 1);
    }

    public List<Reward> getRewards() {
        List<Reward> allRewards = new ArrayList<>();
        for (List<Reward> list : rewardsByRarity.values()) {
            allRewards.addAll(list);
        }
        return allRewards;
    }
}

