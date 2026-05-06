package com.gn027c.luckyblock.paper.reward;

import com.gn027c.luckyblock.core.reward.RewardType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Pre-validates all reward configurations at startup (inspired by GeyserMC's validation approach).
 * Instead of crashing during gameplay, all config errors are caught and reported at load time.
 */
public class RewardValidator {

    /**
     * Represents a single validation problem found in rewards.yml.
     */
    public record ValidationIssue(Severity severity, String rewardId, String message) {
        public enum Severity { ERROR, WARNING }

        @Override
        public String toString() {
            return "[" + severity + "] reward='" + rewardId + "': " + message;
        }
    }

    /**
     * Validates a single reward's ConfigurationSection.
     * Returns a list of issues. Empty list = all good.
     */
    public static List<ValidationIssue> validate(String key, ConfigurationSection section) {
        List<ValidationIssue> issues = new ArrayList<>();

        // --- 1. type field ---
        String typeStr = section.getString("type");
        if (typeStr == null) {
            issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, key, "Thiếu trường 'type'"));
            return issues; // cannot continue without type
        }

        RewardType type;
        try {
            type = RewardType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, key,
                    "Giá trị 'type' không hợp lệ: '" + typeStr + "'. Các giá trị hợp lệ: " + java.util.Arrays.toString(RewardType.values())));
            return issues;
        }

        ConfigurationSection data = section.getConfigurationSection("data");

        // --- 2. Type-specific validation ---
        switch (type) {
            case ITEM -> validateItem(key, data, issues);
            case EFFECT -> validateEffect(key, data, issues);
            case COMMAND -> validateCommand(key, data, issues);
            case ENTITY -> validateEntity(key, data, issues);
            case STRUCTURE, TRAP -> validateStructureTrap(key, data, issues);
        }

        // --- 3. General field warnings ---
        int weight = section.getInt("weight", -999);
        if (weight == -999) {
            issues.add(new ValidationIssue(ValidationIssue.Severity.WARNING, key, "Thiếu 'weight', sẽ dùng mặc định 10"));
        } else if (weight <= 0) {
            issues.add(new ValidationIssue(ValidationIssue.Severity.WARNING, key, "'weight' phải > 0, hiện tại: " + weight));
        }

        return issues;
    }

    // ───────────────────────────────────────────────────────────────
    // Type Validators
    // ───────────────────────────────────────────────────────────────

    private static void validateItem(String key, ConfigurationSection data, List<ValidationIssue> issues) {
        if (data == null) {
            issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, key, "[ITEM] Thiếu section 'data'"));
            return;
        }
        if (data.contains("material")) {
            checkMaterial(key, data.getString("material"), issues);
        } else if (data.contains("items")) {
            List<?> items = data.getList("items");
            if (items == null || items.isEmpty()) {
                issues.add(new ValidationIssue(ValidationIssue.Severity.WARNING, key, "[ITEM] 'items' rỗng"));
            } else {
                for (int i = 0; i < items.size(); i++) {
                    Object item = items.get(i);
                    if (item instanceof java.util.Map<?, ?> map) {
                        String mat = (String) map.get("material");
                        checkMaterial(key, mat, issues);
                        if (!map.containsKey("amount")) {
                            issues.add(new ValidationIssue(ValidationIssue.Severity.WARNING, key, "[ITEM] items[" + i + "] thiếu 'amount'"));
                        }
                    }
                }
            }
        } else {
            issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, key, "[ITEM] Cần 'material' hoặc 'items'"));
        }
    }

    private static void validateEffect(String key, ConfigurationSection data, List<ValidationIssue> issues) {
        if (data == null) {
            issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, key, "[EFFECT] Thiếu section 'data'"));
            return;
        }
        if (!data.contains("effect")) {
            issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, key, "[EFFECT] Thiếu trường 'effect'"));
        }
        if (!data.contains("duration")) {
            issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, key, "[EFFECT] Thiếu trường 'duration'"));
        }
        if (!data.contains("amplifier")) {
            issues.add(new ValidationIssue(ValidationIssue.Severity.WARNING, key, "[EFFECT] Thiếu 'amplifier', mặc định 0"));
        }
    }

    private static void validateCommand(String key, ConfigurationSection data, List<ValidationIssue> issues) {
        if (data == null) {
            issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, key, "[COMMAND] Thiếu section 'data'"));
            return;
        }
        if (!data.contains("command") && !data.contains("commands")) {
            issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, key, "[COMMAND] Cần 'command' hoặc 'commands'"));
        }
    }

    private static void validateEntity(String key, ConfigurationSection data, List<ValidationIssue> issues) {
        if (data == null) {
            issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, key, "[ENTITY] Thiếu section 'data'"));
            return;
        }
        if (data.contains("entity")) {
            checkEntity(key, data.getString("entity"), issues);
        } else if (data.contains("entities")) {
            List<?> entities = data.getList("entities");
            if (entities != null) {
                for (Object e : entities) {
                    if (e instanceof java.util.Map<?, ?> map) {
                        checkEntity(key, (String) map.get("entity"), issues);
                    }
                }
            }
        } else {
            issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, key, "[ENTITY] Cần 'entity' hoặc 'entities'"));
        }
    }

    private static void validateStructureTrap(String key, ConfigurationSection data, List<ValidationIssue> issues) {
        if (data == null) {
            issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, key, "[STRUCTURE/TRAP] Thiếu section 'data'"));
            return;
        }
        if (!data.contains("action")) {
            issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, key, "[STRUCTURE/TRAP] Thiếu trường 'action'"));
        }
    }

    // ───────────────────────────────────────────────────────────────
    // Helpers
    // ───────────────────────────────────────────────────────────────

    private static void checkMaterial(String key, String matStr, List<ValidationIssue> issues) {
        if (matStr == null) {
            issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, key, "Tên material là null"));
            return;
        }
        try {
            Material mat = Material.valueOf(matStr.toUpperCase());
            if (mat == Material.AIR) {
                issues.add(new ValidationIssue(ValidationIssue.Severity.WARNING, key, "Material 'AIR' có thể là lỗi cấu hình"));
            }
        } catch (IllegalArgumentException e) {
            issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, key,
                    "Material không tồn tại trong Minecraft 1.21: '" + matStr + "'"));
        }
    }

    private static void checkEntity(String key, String entityStr, List<ValidationIssue> issues) {
        if (entityStr == null) {
            issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, key, "Tên entity là null"));
            return;
        }
        try {
            EntityType.valueOf(entityStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            issues.add(new ValidationIssue(ValidationIssue.Severity.ERROR, key,
                    "EntityType không tồn tại: '" + entityStr + "'"));
        }
    }

    /**
     * Prints a formatted validation report to the server logger.
     * Returns true if there are no errors (warnings are OK).
     */
    public static boolean printReport(Logger logger, List<ValidationIssue> allIssues, int totalLoaded) {
        long errors = allIssues.stream().filter(i -> i.severity() == ValidationIssue.Severity.ERROR).count();
        long warnings = allIssues.stream().filter(i -> i.severity() == ValidationIssue.Severity.WARNING).count();

        if (allIssues.isEmpty()) {
            logger.info("[gnluckyblock] ✔ Xác thực rewards.yml: tất cả " + totalLoaded + " phần thưởng hợp lệ.");
            return true;
        }

        logger.warning("╔══════════════════════════════════════════════");
        logger.warning("║  GNLUCKYBLOCK - BÁO CÁO XÁC THỰC REWARDS");
        logger.warning("╠══════════════════════════════════════════════");
        logger.warning("║  Đã nạp: " + totalLoaded + " phần thưởng");
        logger.warning("║  Lỗi: " + errors + "  |  Cảnh báo: " + warnings);
        logger.warning("╠══════════════════════════════════════════════");

        for (ValidationIssue issue : allIssues) {
            if (issue.severity() == ValidationIssue.Severity.ERROR) {
                logger.warning("║  ✖ " + issue);
            } else {
                logger.warning("║  ⚠ " + issue);
            }
        }

        logger.warning("╠══════════════════════════════════════════════");
        if (errors > 0) {
            logger.warning("║  ► Bật debug trong config.yml để xem chi tiết hơn");
            logger.warning("║  ► Các phần thưởng bị lỗi sẽ KHÔNG được nạp");
        }
        logger.warning("╚══════════════════════════════════════════════");

        return errors == 0;
    }
}
