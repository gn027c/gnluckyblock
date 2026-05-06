package com.gn027c.luckyblock.paper.reward;

import com.gn027c.luckyblock.core.manager.AbstractModule;
import com.gn027c.luckyblock.core.reward.Reward;
import com.gn027c.luckyblock.core.reward.RewardManager;
import com.gn027c.luckyblock.core.reward.RewardType;
import com.gn027c.luckyblock.paper.gnluckyblock;
import com.gn027c.luckyblock.paper.gui.RewardGUI;
import com.gn027c.luckyblock.paper.util.PluginLogger;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.messages.Titles;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardModule extends AbstractModule {
    private final gnluckyblock plugin;
    private final RewardManager rewardManager;
    private final RewardGUI rewardGUI;

    public RewardModule(gnluckyblock plugin) {
        this.plugin = plugin;
        this.rewardManager = new RewardManager();
        loadRewards();
        this.rewardGUI = new RewardGUI(plugin, this);
    }

    public void reloadRewards() {
        loadRewards();
    }

    public void loadRewards() {
        rewardManager.clearRewards();
        boolean debug = plugin.getConfig().getBoolean("settings.debug", false);

        // Cấu hình chế độ random (Mặc định là TIERED)
        String modeStr = plugin.getConfig().getString("settings.reward-mode", "TIERED").toUpperCase();
        try {
            rewardManager.setMode(com.gn027c.luckyblock.core.reward.RewardManager.RewardMode.valueOf(modeStr));
        } catch (IllegalArgumentException e) {
            rewardManager.setMode(com.gn027c.luckyblock.core.reward.RewardManager.RewardMode.TIERED);
        }

        File file = new File(plugin.getDataFolder(), "rewards.yml");
        if (!file.exists()) {
            plugin.saveResource("rewards.yml", false);
        }

        List<RewardValidator.ValidationIssue> allIssues = new ArrayList<>();

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);
            ConfigurationSection outcomes = config.getConfigurationSection("outcomes");
            if (outcomes != null) {
                for (String key : outcomes.getKeys(false)) {
                    ConfigurationSection rewardData = outcomes.getConfigurationSection(key);
                    if (rewardData == null) continue;

                    // ── Pre-validation (Geyser-style) ──────────────────────────────
                    List<RewardValidator.ValidationIssue> issues = RewardValidator.validate(key, rewardData);
                    allIssues.addAll(issues);

                    boolean hasError = issues.stream()
                            .anyMatch(i -> i.severity() == RewardValidator.ValidationIssue.Severity.ERROR);
                    if (hasError) {
                        if (debug) plugin.getLogger().warning("[DEBUG] Bỏ qua reward '" + key + "' do có lỗi cấu hình.");
                        continue; // skip broken rewards — don't register them
                    }
                    // ───────────────────────────────────────────────────────────────

                    String typeStr = rewardData.getString("type");
                    RewardType type = RewardType.valueOf(typeStr.toUpperCase());
                    String rarityStr = rewardData.getString("rarity", "COMMON").toUpperCase();
                    com.gn027c.luckyblock.core.reward.Rarity rarity;
                    try {
                        rarity = com.gn027c.luckyblock.core.reward.Rarity.valueOf(rarityStr);
                    } catch (IllegalArgumentException e) {
                        rarity = com.gn027c.luckyblock.core.reward.Rarity.COMMON;
                    }
                    int weight = rewardData.getInt("weight", 10);
                    int luck = rewardData.getInt("luck", 0);

                    // Tự động gán luck nếu chưa có dựa trên ID
                    if (!rewardData.contains("luck")) {
                        String idLower = key.toLowerCase();
                        if (idLower.contains("bad") || idLower.contains("troll") || idLower.contains("trap") || idLower.contains("tnt") || idLower.contains("lava") || idLower.contains("creeper")) {
                            luck = -50;
                        } else if (idLower.contains("good") || idLower.contains("lucky") || idLower.contains("diamond") || idLower.contains("emerald") || idLower.contains("netherite")) {
                            luck = 50;
                        }
                    }

                    String announcement = rewardData.getString("announcement", "");

                    Map<String, Object> data = new HashMap<>();
                    ConfigurationSection innerData = rewardData.getConfigurationSection("data");
                    if (innerData != null) {
                        data = sectionToMap(innerData);
                    }

                    rewardManager.registerReward(new Reward(key, type, rarity, weight, luck, data, announcement));
                }
            } else {
                plugin.getLogger().warning("Không tìm thấy section 'outcomes' trong rewards.yml");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Lỗi khi đọc rewards.yml: " + e.getMessage());
            if (debug) e.printStackTrace();
        }

        // Print validation report
        RewardValidator.printReport(plugin.getLogger(), allIssues, rewardManager.getRewards().size());
        PluginLogger.log(PluginLogger.Flag.CONFIG, "Đã nạp " + rewardManager.getRewards().size() + " phần thưởng từ rewards.yml.");
        plugin.getLogger().info("Đã nạp " + rewardManager.getRewards().size() + " phần thưởng LuckyBlock.");
    }

    /**
     * Recursively converts a ConfigurationSection (including nested MemorySections)
     * into a plain Map<String, Object> so that casting to Map won't throw ClassCastException.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> sectionToMap(ConfigurationSection section) {
        Map<String, Object> result = new HashMap<>();
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            if (value instanceof MemorySection) {
                result.put(key, sectionToMap((MemorySection) value));
            } else if (value instanceof List) {
                result.put(key, convertList((List<?>) value));
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Object> convertList(List<?> list) {
        java.util.ArrayList<Object> result = new java.util.ArrayList<>();
        for (Object item : list) {
            if (item instanceof MemorySection) {
                result.add(sectionToMap((MemorySection) item));
            } else if (item instanceof Map) {
                // Convert any nested MemorySections inside maps within lists
                Map<String, Object> map = new HashMap<>();
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) item).entrySet()) {
                    Object v = entry.getValue();
                    if (v instanceof MemorySection) {
                        map.put(String.valueOf(entry.getKey()), sectionToMap((MemorySection) v));
                    } else {
                        map.put(String.valueOf(entry.getKey()), v);
                    }
                }
                result.add(map);
            } else {
                result.add(item);
            }
        }
        return result;
    }

    public void executeReward(Player player, Reward reward) {
        if (reward == null) return;

        String rewardId  = reward.getId();
        RewardType type  = reward.getType();
        Map<String, Object> data = reward.getData();

        try {
            String announcement = reward.getAnnouncement();
            if (announcement != null && !announcement.isEmpty()) {
                String prefix = plugin.getLanguageManager().getRawMessage("prefix");
                plugin.getAudience(player).sendMessage(MiniMessage.miniMessage().deserialize(prefix + announcement));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[gnluckyblock] Lỗi khi gửi announcement cho reward '" + rewardId + "': " + e.getMessage());
        }

        try {
        switch (type) {
            case ITEM: {
                try {
                    if (data.containsKey("items")) {
                        List<Map<?, ?>> items = (List<Map<?, ?>>) data.get("items");
                        for (Map<?, ?> itemData : items) {
                            Material mat = Material.valueOf((String) itemData.get("material"));
                            int amount = (int) itemData.get("amount");
                            player.getInventory().addItem(new ItemStack(mat, amount));
                        }
                    } else if (data.containsKey("material")) {
                        Material mat = Material.valueOf((String) data.get("material"));
                        int amount = data.containsKey("amount") ? (int) data.get("amount") : 1;
                        player.getInventory().addItem(new ItemStack(mat, amount));
                    }
                } catch (Exception e) {
                    throw new RuntimeException("[ITEM] reward='" + rewardId + "' " + dataSnapshot(data), e);
                }
                break;
            }
            
            case EFFECT: {
                try {
                    String effectName = (String) data.get("effect");
                    int duration = (int) data.get("duration");
                    int amplifier = (int) data.get("amplifier");
                    XPotion.matchXPotion(effectName).ifPresent(xp -> player.addPotionEffect(xp.buildPotionEffect(duration, amplifier)));
                } catch (Exception e) {
                    throw new RuntimeException("[EFFECT] reward='" + rewardId + "' " + dataSnapshot(data), e);
                }
                break;
            }
            
            case COMMAND: {
                try {
                    if (data.containsKey("commands")) {
                        List<String> commands = (List<String>) data.get("commands");
                        for (String cmd : commands) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                        }
                    } else if (data.containsKey("command")) {
                        String cmd = (String) data.get("command");
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                    }
                } catch (Exception e) {
                    throw new RuntimeException("[COMMAND] reward='" + rewardId + "' " + dataSnapshot(data), e);
                }
                break;
            }
            
            case ENTITY: {
                if (data.containsKey("entities")) {
                    List<Map<?, ?>> entities = (List<Map<?, ?>>) data.get("entities");
                    for (Map<?, ?> entData : entities) {
                        String entityName = (String) entData.get("entity");
                        int amount = (int) entData.get("amount");
                        for (int i = 0; i < amount; i++) {
                            org.bukkit.entity.Entity ent = player.getWorld().spawnEntity(player.getLocation(), org.bukkit.entity.EntityType.valueOf(entityName));
                            if (entData.containsKey("name")) {
                                ent.setCustomNameVisible(true);
                                ent.setCustomName(LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize((String) entData.get("name"))));
                            }
                        }
                    }
                } else if (data.containsKey("entity")) {
                    String entityName = (String) data.get("entity");
                    int amount = data.containsKey("amount") ? (int) data.get("amount") : 1;
                    for (int i = 0; i < amount; i++) {
                        org.bukkit.entity.Entity ent = player.getWorld().spawnEntity(player.getLocation(), org.bukkit.entity.EntityType.valueOf(entityName));
                        if (data.containsKey("name")) {
                            ent.setCustomNameVisible(true);
                            ent.setCustomName(LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize((String) data.get("name"))));
                        }
                        
                        // Equip if living
                        if (ent instanceof org.bukkit.entity.LivingEntity living && data.containsKey("equipment")) {
                            Map<?, ?> eqData = (Map<?, ?>) data.get("equipment");
                            org.bukkit.inventory.EntityEquipment equip = living.getEquipment();
                            if (equip != null) {
                                if (eqData.containsKey("helmet")) {
                                    equip.setHelmet(new ItemStack(Material.valueOf((String) eqData.get("helmet"))));
                                    equip.setHelmetDropChance(1.0f);
                                }
                                if (eqData.containsKey("chestplate")) {
                                    equip.setChestplate(new ItemStack(Material.valueOf((String) eqData.get("chestplate"))));
                                    equip.setChestplateDropChance(1.0f);
                                }
                                if (eqData.containsKey("leggings")) {
                                    equip.setLeggings(new ItemStack(Material.valueOf((String) eqData.get("leggings"))));
                                    equip.setLeggingsDropChance(1.0f);
                                }
                                if (eqData.containsKey("boots")) {
                                    equip.setBoots(new ItemStack(Material.valueOf((String) eqData.get("boots"))));
                                    equip.setBootsDropChance(1.0f);
                                }
                                if (eqData.containsKey("mainhand")) {
                                    equip.setItemInMainHand(new ItemStack(Material.valueOf((String) eqData.get("mainhand"))));
                                    equip.setItemInMainHandDropChance(1.0f);
                                }
                            }
                        }
                    }
                }
                break;
            }
            case STRUCTURE:
            case TRAP: {
                try {
                    String action = (String) data.get("action");
                    if ("fling".equalsIgnoreCase(action)) {
                        org.bukkit.util.Vector vel = player.getVelocity();
                        double v = 2.0;
                        if (data.get("velocity") instanceof Number) v = ((Number) data.get("velocity")).doubleValue();
                        vel.setY(v);
                        player.setVelocity(vel);
                    } else if ("tnt".equalsIgnoreCase(action)) {
                        int amount = 4;
                        if (data.get("amount") instanceof Number) amount = ((Number) data.get("amount")).intValue();
                        for (int i = 0; i < amount; i++) {
                            org.bukkit.entity.EntityType tntType = null;
                            try { tntType = org.bukkit.entity.EntityType.valueOf("TNT"); } catch (Exception e) {
                                try { tntType = org.bukkit.entity.EntityType.valueOf("PRIMED_TNT"); } catch (Exception ex) {}
                            }
                            if (tntType != null) player.getWorld().spawnEntity(player.getLocation().add(Math.random() * 2 - 1, 1, Math.random() * 2 - 1), tntType);
                        }
                    } else if ("wishing_well".equalsIgnoreCase(action)) {
                        org.bukkit.Location loc = player.getLocation().subtract(0, 1, 0);
                        org.bukkit.Location centerLoc = loc.clone().add(0, 1, 0);
                        org.bukkit.metadata.FixedMetadataValue metaValue = new org.bukkit.metadata.FixedMetadataValue(com.gn027c.luckyblock.paper.gnluckyblock.getInstance(), true);
                        org.bukkit.metadata.FixedMetadataValue centerMeta = new org.bukkit.metadata.FixedMetadataValue(com.gn027c.luckyblock.paper.gnluckyblock.getInstance(), centerLoc);
                        
                        // Nền (3x3)
                        for (int x = -1; x <= 1; x++) {
                            for (int z = -1; z <= 1; z++) {
                                org.bukkit.block.Block b = loc.clone().add(x, 0, z).getBlock();
                                b.setType(Material.STONE_BRICKS);
                                b.setMetadata("wishing_well", metaValue);
                                b.setMetadata("wishing_well_center", centerMeta);
                            }
                        }
                        
                        // Thành giếng (3x3, giữa là nước)
                        for (int x = -1; x <= 1; x++) {
                            for (int z = -1; z <= 1; z++) {
                                org.bukkit.block.Block b = loc.clone().add(x, 1, z).getBlock();
                                if (x == 0 && z == 0) {
                                    b.setType(Material.WATER);
                                } else {
                                    b.setType(Material.STONE_BRICK_WALL);
                                }
                                b.setMetadata("wishing_well", metaValue);
                                b.setMetadata("wishing_well_center", centerMeta);
                            }
                        }
                        
                        // Trụ (4 góc)
                        int[] dx = {-1, 1, -1, 1};
                        int[] dz = {-1, -1, 1, 1};
                        for (int i = 0; i < 4; i++) {
                            for (int y = 2; y <= 3; y++) {
                                org.bukkit.block.Block b = loc.clone().add(dx[i], y, dz[i]).getBlock();
                                b.setType(Material.OAK_FENCE);
                                b.setMetadata("wishing_well", metaValue);
                                b.setMetadata("wishing_well_center", centerMeta);
                            }
                        }
                        
                        // Mái (3x3)
                        for (int x = -1; x <= 1; x++) {
                            for (int z = -1; z <= 1; z++) {
                                org.bukkit.block.Block b = loc.clone().add(x, 4, z).getBlock();
                                b.setType(Material.OAK_SLAB);
                                b.setMetadata("wishing_well", metaValue);
                                b.setMetadata("wishing_well_center", centerMeta);
                            }
                        }

                        // Đặt số lần sử dụng vào khối nước trung tâm
                        org.bukkit.block.Block center = centerLoc.getBlock();
                        center.setMetadata("wishing_well_uses", new org.bukkit.metadata.FixedMetadataValue(com.gn027c.luckyblock.paper.gnluckyblock.getInstance(), 5));
                        
                        // Give nuggets
                        player.getInventory().addItem(new ItemStack(Material.GOLD_NUGGET, 5));
                        
                        plugin.getLanguageManager().sendMessage(plugin.getAudience(player), "rewards.wishing-well-found");
                    } else if ("trading_post".equalsIgnoreCase(action)) {
                        org.bukkit.Location loc = player.getLocation();
                        loc.getBlock().setType(Material.LECTERN);
                        loc.clone().add(1, 0, 0).getBlock().setType(XMaterial.BARREL.parseMaterial());
                        loc.clone().add(0, 0, 1).getBlock().setType(XMaterial.BARREL.parseMaterial());
                        plugin.getLanguageManager().sendMessage(plugin.getAudience(player), "rewards.trading-post");
                    } else if ("lucky_tree".equalsIgnoreCase(action)) {
                        org.bukkit.Location loc = player.getLocation().add(2, 0, 0);
                        loc.getWorld().generateTree(loc, org.bukkit.TreeType.BIG_TREE);
                        loc.clone().add(0, 2, 0).getBlock().setType(Material.valueOf(com.gn027c.luckyblock.paper.gnluckyblock.getInstance().getConfig().getString("lucky-block.material", "GOLD_BLOCK")));
                        plugin.getLanguageManager().sendMessage(plugin.getAudience(player), "rewards.lucky-tree");
                    } else if ("anvil_cage".equalsIgnoreCase(action)) {
                        org.bukkit.Location centerLoc = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
                        centerLoc.setYaw(player.getLocation().getYaw());
                        centerLoc.setPitch(player.getLocation().getPitch());
                        player.teleport(centerLoc);

                        for (int y = 0; y <= 2; y++) {
                            for (int x = -1; x <= 1; x++) {
                                for (int z = -1; z <= 1; z++) {
                                    if (x == 0 && z == 0) continue; 
                                    centerLoc.clone().add(x, y, z).getBlock().setType(Material.GLASS);
                                }
                            }
                        }
                        // Clear the space above so the anvil doesn't land on blocks above the player
                        for (int y = 3; y <= 10; y++) {
                            centerLoc.clone().add(0, y, 0).getBlock().setType(Material.AIR);
                        }

                        // Dùng spawnFallingBlock để đảm bảo đe rơi xuống và gây sát thương
                        // Đặt độ cao rơi là 10 block
                        org.bukkit.entity.FallingBlock anvil = centerLoc.getWorld().spawnFallingBlock(centerLoc.clone().add(0, 10, 0), org.bukkit.Material.ANVIL.createBlockData());
                        anvil.setHurtEntities(true);
                        anvil.setDropItem(false);
                        anvil.setDamagePerBlock(2.0f); // Tăng sát thương mỗi block rơi
                    } else if ("water_cage".equalsIgnoreCase(action)) {
                        org.bukkit.Location centerLoc = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
                        centerLoc.setYaw(player.getLocation().getYaw());
                        centerLoc.setPitch(player.getLocation().getPitch());
                        player.teleport(centerLoc);

                        for (int y = 0; y <= 2; y++) {
                            for (int x = -1; x <= 1; x++) {
                                for (int z = -1; z <= 1; z++) {
                                    if (x == 0 && z == 0 && y < 2) continue;
                                    centerLoc.clone().add(x, y, z).getBlock().setType(Material.OBSIDIAN);
                                }
                            }
                        }
                        centerLoc.clone().add(0, 1, 0).getBlock().setType(Material.WATER);
                    } else if ("mini_tower".equalsIgnoreCase(action)) {
                        org.bukkit.Location loc = player.getLocation();
                        for (int y = 0; y <= 4; y++) {
                            loc.clone().add(0, y, 0).getBlock().setType(Material.COBBLESTONE_WALL);
                        }
                        loc.clone().add(0, 5, 0).getBlock().setType(Material.CHEST);
                        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) loc.clone().add(0, 5, 0).getBlock().getState();
                        Material[] possibleLoot = {Material.DIAMOND, Material.GOLD_INGOT, Material.IRON_INGOT, Material.EMERALD, Material.ENCHANTED_GOLDEN_APPLE};
                        for (int i = 0; i < 5; i++) {
                            Material lootMat = possibleLoot[(int) (Math.random() * possibleLoot.length)];
                            int lootAmount = (int) (Math.random() * 5) + 1;
                            chest.getInventory().addItem(new ItemStack(lootMat, lootAmount));
                        }
                    } else if ("lucky_villager".equalsIgnoreCase(action)) {
                        org.bukkit.entity.Villager villager = (org.bukkit.entity.Villager) player.getWorld().spawnEntity(player.getLocation(), org.bukkit.entity.EntityType.VILLAGER);
                        net.kyori.adventure.text.Component nameComp = MiniMessage.miniMessage().deserialize(plugin.getLanguageManager().getRawMessage("rewards.lucky-villager-name"));
                        villager.setCustomName(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(nameComp));
                        villager.setCustomNameVisible(true);
                        villager.setProfession(org.bukkit.entity.Villager.Profession.LIBRARIAN);
                        villager.setVillagerLevel(5);
                        villager.setAI(false);
                        villager.setInvulnerable(true);
                        villager.setRemoveWhenFarAway(false);
                        
                        List<org.bukkit.inventory.MerchantRecipe> recipes = new java.util.ArrayList<>();
                        
                        // 1. DIRT -> NETHERITE
                        org.bukkit.inventory.MerchantRecipe r1 = new org.bukkit.inventory.MerchantRecipe(new ItemStack(Material.NETHERITE_INGOT), 1);
                        r1.addIngredient(new ItemStack(Material.DIRT, 64));
                        recipes.add(r1);
                        
                        // 2. EMERALD -> TOTEM
                        org.bukkit.inventory.MerchantRecipe r2 = new org.bukkit.inventory.MerchantRecipe(new ItemStack(Material.TOTEM_OF_UNDYING), 1);
                        r2.addIngredient(new ItemStack(Material.EMERALD, 10));
                        recipes.add(r2);
  
                        // 3. BOOK + DIAMOND -> SHARPNESS X
                        ItemStack opBook = new ItemStack(XMaterial.ENCHANTED_BOOK.parseMaterial());
                        org.bukkit.inventory.meta.EnchantmentStorageMeta meta = (org.bukkit.inventory.meta.EnchantmentStorageMeta) opBook.getItemMeta();
                        XEnchantment.matchXEnchantment("SHARPNESS").ifPresent(xe -> meta.addStoredEnchant(xe.getEnchant(), 7, true));
                        opBook.setItemMeta(meta);
                        org.bukkit.inventory.MerchantRecipe r3 = new org.bukkit.inventory.MerchantRecipe(opBook, 1);
                        r3.addIngredient(new ItemStack(Material.BOOK, 1));
                        r3.addIngredient(new ItemStack(Material.DIAMOND, 32));
                        recipes.add(r3);
  
                        // 4. PICKAXE + EMERALD -> OP PICKAXE
                        ItemStack opPick = new ItemStack(XMaterial.DIAMOND_PICKAXE.parseMaterial());
                        XEnchantment.matchXEnchantment("EFFICIENCY").ifPresent(xe -> opPick.addUnsafeEnchantment(xe.getEnchant(), 6));
                        XEnchantment.matchXEnchantment("FORTUNE").ifPresent(xe -> opPick.addUnsafeEnchantment(xe.getEnchant(), 4));
                        XEnchantment.matchXEnchantment("UNBREAKING").ifPresent(xe -> opPick.addUnsafeEnchantment(xe.getEnchant(), 5));
                        org.bukkit.inventory.MerchantRecipe r4 = new org.bukkit.inventory.MerchantRecipe(opPick, 1);
                        r4.addIngredient(new ItemStack(Material.DIAMOND_PICKAXE, 1));
                        r4.addIngredient(new ItemStack(Material.EMERALD, 15));
                        recipes.add(r4);
                        
                        villager.setRecipes(recipes);
                        plugin.getLanguageManager().sendMessage(plugin.getAudience(player), "rewards.lucky-villager-spawn");
                    } else if ("target_game".equalsIgnoreCase(action)) {
                        org.bukkit.Location loc = player.getLocation().add(0, 3, 5);
                        loc.getBlock().setType(Material.TARGET);
                        loc.getBlock().setMetadata("target_game", new org.bukkit.metadata.FixedMetadataValue(com.gn027c.luckyblock.paper.gnluckyblock.getInstance(), true));
                        player.getInventory().addItem(new ItemStack(Material.BOW), new ItemStack(Material.ARROW, 16));
                        plugin.getLanguageManager().sendMessage(plugin.getAudience(player), "rewards.mini-game");
                    } else if ("lightning".equalsIgnoreCase(action)) {
                        player.getWorld().strikeLightning(player.getLocation());
                    } else if ("pyramid".equalsIgnoreCase(action)) {
                        org.bukkit.Location loc = player.getLocation();
                        for (int y = 0; y <= 3; y++) {
                            int size = 3 - y;
                            for (int x = -size; x <= size; x++) {
                                for (int z = -size; z <= size; z++) {
                                    loc.clone().add(x, y, z).getBlock().setType(org.bukkit.Material.SANDSTONE);
                                }
                            }
                        }
                        com.gn027c.luckyblock.paper.block.BlockModule blockModule = com.gn027c.luckyblock.paper.gnluckyblock.getInstance().getBlockModule();
                        int[] dx = {-1, 1, -1, 1};
                        int[] dz = {-1, -1, 1, 1};
                        for (int i = 0; i < 4; i++) {
                            org.bukkit.Location lbLoc = loc.clone().add(dx[i], 0, dz[i]);
                            org.bukkit.block.Block b = lbLoc.getBlock();
                            b.setType(Material.valueOf(com.gn027c.luckyblock.paper.gnluckyblock.getInstance().getConfig().getString("lucky-block.material", "GOLD_BLOCK")));
                            blockModule.addLocation(lbLoc);
                            try {
                                lbLoc.getWorld().spawnParticle(org.bukkit.Particle.valueOf("VILLAGER_HAPPY"), lbLoc.add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2);
                            } catch (Exception e) {}
                        }
                    } else if ("nether_portal".equalsIgnoreCase(action)) {
                        org.bukkit.Location loc = player.getLocation();
                        for (int y = 0; y <= 4; y++) {
                            for (int x = -1; x <= 2; x++) {
                                org.bukkit.block.Block b = loc.clone().add(x, y, 0).getBlock();
                                if (x == -1 || x == 2 || y == 0 || y == 4) {
                                    b.setType(Material.OBSIDIAN);
                                } else {
                                    b.setType(Material.NETHER_PORTAL, false);
                                    org.bukkit.block.data.Orientable portalData = (org.bukkit.block.data.Orientable) b.getBlockData();
                                    portalData.setAxis(org.bukkit.Axis.X);
                                    b.setBlockData(portalData);
                                }
                            }
                        }
                    } else if ("lava_pit".equalsIgnoreCase(action)) {
                        org.bukkit.Location loc = player.getLocation();
                        for (int x = -1; x <= 1; x++) {
                            for (int z = -1; z <= 1; z++) {
                                for (int y = -1; y >= -2; y--) {
                                    loc.clone().add(x, y, z).getBlock().setType(y == -2 ? Material.LAVA : Material.AIR);
                                }
                            }
                        }
                    } else if ("cobweb_trap".equalsIgnoreCase(action)) {
                        org.bukkit.Location loc = player.getLocation();
                        for (int y = 0; y <= 1; y++) {
                            for (int x = -1; x <= 1; x++) {
                                for (int z = -1; z <= 1; z++) {
                                    loc.clone().add(x, y, z).getBlock().setType(Material.COBWEB);
                                }
                            }
                        }
                    } else if ("xp_rain".equalsIgnoreCase(action)) {
                        for (int i = 0; i < 20; i++) {
                            org.bukkit.entity.EntityType xpType = null;
                            try { xpType = org.bukkit.entity.EntityType.valueOf("EXPERIENCE_BOTTLE"); } catch (Exception e) {
                                try { xpType = org.bukkit.entity.EntityType.valueOf("THROWN_EXP_BOTTLE"); } catch (Exception ex) {}
                            }
                            if (xpType != null) player.getWorld().spawnEntity(player.getLocation().add(Math.random() * 6 - 3, 5, Math.random() * 6 - 3), xpType);
                        }
                    } else if ("boat_trap".equalsIgnoreCase(action)) {
                        org.bukkit.entity.EntityType boatType = null;
                        try { boatType = org.bukkit.entity.EntityType.valueOf("OAK_BOAT"); } catch (Exception e) {
                            try { boatType = org.bukkit.entity.EntityType.valueOf("BOAT"); } catch (Exception ex) {}
                        }
                        if (boatType != null) {
                            org.bukkit.entity.Boat boat = (org.bukkit.entity.Boat) player.getWorld().spawnEntity(player.getLocation(), boatType);
                            boat.addPassenger(player);
                        }
                    } else if ("view_troll".equalsIgnoreCase(action)) {
                        org.bukkit.Location loc = player.getLocation();
                        loc.setYaw(loc.getYaw() + 180);
                        loc.setPitch(-90);
                        player.teleport(loc);
                        org.bukkit.potion.PotionEffectType nausea = null;
                        try { nausea = org.bukkit.potion.PotionEffectType.getByName("NAUSEA"); } catch (Exception e) {}
                        if (nausea == null) nausea = org.bukkit.potion.PotionEffectType.getByName("CONFUSION");
                        if (nausea != null) player.addPotionEffect(new org.bukkit.potion.PotionEffect(nausea, 200, 1));
                    } else if ("phantom_riders".equalsIgnoreCase(action)) {
                        try {
                            org.bukkit.entity.EntityType phantomType = org.bukkit.entity.EntityType.valueOf("PHANTOM");
                            for (int i = 0; i < 3; i++) {
                                org.bukkit.entity.Entity phantom = player.getWorld().spawnEntity(player.getLocation().add(0, 5, 0), phantomType);
                                org.bukkit.entity.Entity skeleton = player.getWorld().spawnEntity(player.getLocation().add(0, 5, 0), org.bukkit.entity.EntityType.SKELETON);
                                phantom.addPassenger(skeleton);
                            }
                        } catch (Exception e) {
                            // Fallback cho bản cũ (spawn bats + skeleton?)
                            for (int i = 0; i < 3; i++) {
                                player.getWorld().spawnEntity(player.getLocation().add(0, 5, 0), org.bukkit.entity.EntityType.SKELETON);
                            }
                        }
                    } else if (TrapHandler.handle(player, action, data)) {
                        // Handled by TrapHandler
                    }
                } catch (RuntimeException e) {
                    throw e; // already has context from inner try-catches
                } catch (Exception e) {
                    String action2 = data.containsKey("action") ? String.valueOf(data.get("action")) : "unknown";
                    throw new RuntimeException("[STRUCTURE/TRAP] reward='" + rewardId + "' action='" + action2 + "' " + dataSnapshot(data), e);
                }
                break;
            }
        }
        } catch (Exception e) {
            boolean debug = plugin.getConfig().getBoolean("settings.debug", false);
            String cause = e.getCause() != null ? e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage() : e.getMessage();
            plugin.getLogger().severe("[gnluckyblock] Lỗi thực thi reward '" + rewardId + "' (" + type + "): " + cause);
            if (debug) {
                plugin.getLogger().severe("  Player : " + player.getName());
                plugin.getLogger().severe("  Data   : " + dataSnapshot(data));
                e.printStackTrace();
            } else {
                plugin.getLogger().severe("  → Bật 'debug: true' trong config.yml để xem stack trace đầy đủ.");
            }
        }
    }

    /** Tóm tắt data map thành chuỗi ngắn để log (tránh in toàn bộ data dài) */
    private String dataSnapshot(Map<String, Object> data) {
        if (data == null || data.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        int count = 0;
        for (Map.Entry<String, Object> e : data.entrySet()) {
            if (count++ > 0) sb.append(", ");
            Object val = e.getValue();
            String valStr;
            if (val instanceof java.util.List) {
                valStr = "List[" + ((java.util.List<?>) val).size() + "]"; 
            } else if (val instanceof Map) {
                valStr = "Map{" + ((Map<?, ?>) val).keySet() + "}";
            } else {
                valStr = String.valueOf(val);
            }
            sb.append(e.getKey()).append("=").append(valStr);
            if (count >= 8) { sb.append(", ..."); break; }
        }
        return sb.append("}").toString();
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public RewardGUI getRewardGUI() {
        return rewardGUI;
    }
}
