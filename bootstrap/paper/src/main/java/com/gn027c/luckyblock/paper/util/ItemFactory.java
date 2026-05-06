package com.gn027c.luckyblock.paper.util;

import com.gn027c.luckyblock.paper.gnluckyblock;
import com.cryptomorin.xseries.XMaterial;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemFactory {

    // NamespacedKey constants — khởi tạo lazy khi plugin sẵn sàng
    public static NamespacedKey KEY_LUCKY_BLOCK_ID;
    public static NamespacedKey KEY_LUCKY_BLOCK_LUCK;
    public static NamespacedKey KEY_PREVIEW_TOOL;

    /**
     * Gọi từ gnluckyblock.onEnable() để đăng ký các NamespacedKey.
     */
    public static void init(gnluckyblock plugin) {
        KEY_LUCKY_BLOCK_ID   = new NamespacedKey(plugin, "lucky_block_id");
        KEY_LUCKY_BLOCK_LUCK = new NamespacedKey(plugin, "lucky_block_luck");
        KEY_PREVIEW_TOOL     = new NamespacedKey(plugin, "lucky_block_preview_tool");
    }

    /**
     * Tạo Lucky Block dựa trên loại (id) và độ may mắn tùy chỉnh.
     */
    public static ItemStack createLuckyBlock(String id, int luck, int amount) {
        gnluckyblock plugin = gnluckyblock.getInstance();
        List<Map<?, ?>> luckyBlocks = plugin.getConfig().getMapList("lucky-blocks");

        Map<String, Object> blockConfig = null;

        // Xử lý ID "random"
        if ("random".equalsIgnoreCase(id) && !luckyBlocks.isEmpty()) {
            int index = (int) (Math.random() * luckyBlocks.size());
            blockConfig = (Map<String, Object>) luckyBlocks.get(index);
            id = String.valueOf(blockConfig.get("id"));
        }

        // Tìm blockConfig dựa trên ID
        if (blockConfig == null) {
            for (Map<?, ?> map : luckyBlocks) {
                if (id.equalsIgnoreCase(String.valueOf(map.get("id")))) {
                    blockConfig = (Map<String, Object>) map;
                    break;
                }
            }
        }

        // Fallback về block đầu tiên nếu không tìm thấy id
        if (blockConfig == null && !luckyBlocks.isEmpty()) {
            blockConfig = (Map<String, Object>) luckyBlocks.get(0);
            id = String.valueOf(blockConfig.get("id"));
        }

        if (blockConfig == null) {
            id = "classic";
            if (luck == -999) luck = 0;
        } else {
            if (luck == -999) luck = ((Number) blockConfig.getOrDefault("luck", 0)).intValue();
        }

        String materialName = blockConfig != null ? (String) blockConfig.getOrDefault("material", "SPONGE") : "SPONGE";
        String displayName  = blockConfig != null ? (String) blockConfig.getOrDefault("display-name", "<yellow>Lucky Block</yellow>") : "<yellow>Lucky Block</yellow>";
        List<String> loreTemplate = blockConfig != null ? (List<String>) blockConfig.get("lore") : new ArrayList<>();
        String texture = blockConfig != null ? (String) blockConfig.getOrDefault("texture", "") : "";

        ItemStack item = XMaterial.matchXMaterial(materialName).orElse(XMaterial.SPONGE).parseItem();
        if (item == null) item = new ItemStack(org.bukkit.Material.SPONGE);
        item.setAmount(amount);

        // --- Set skull texture nếu là PLAYER_HEAD và có texture ---
        if (!texture.isEmpty() && XMaterial.matchXMaterial(item) == XMaterial.PLAYER_HEAD
                && item.getItemMeta() instanceof org.bukkit.inventory.meta.SkullMeta) {
            org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) item.getItemMeta();
            try {
                org.bukkit.profile.PlayerProfile profile = org.bukkit.Bukkit.createPlayerProfile(java.util.UUID.randomUUID());
                String textureUrl = texture;
                if (!textureUrl.startsWith("http")) {
                    if (textureUrl.length() > 64) {
                        try {
                            String decoded = new String(java.util.Base64.getDecoder().decode(textureUrl));
                            // TÃ¬m URL skin trong JSON (VÃ­ dá»¥: {"textures":{"SKIN":{"url":"http://..."}}})
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("https?://textures\\.minecraft\\.net/texture/[a-zA-Z0-9]+");
                            java.util.regex.Matcher matcher = pattern.matcher(decoded);
                            if (matcher.find()) {
                                textureUrl = matcher.group();
                            }
                        } catch (Exception e) {
                            textureUrl = "https://textures.minecraft.net/texture/" + textureUrl;
                        }
                    } else {
                        textureUrl = "https://textures.minecraft.net/texture/" + textureUrl;
                    }
                }
                
                // Ä áº£m báº£o dÃ¹ng HTTPS
                if (textureUrl.startsWith("http://")) {
                    textureUrl = textureUrl.replace("http://", "https://");
                }
                profile.getTextures().setSkin(new java.net.URL(textureUrl));
                skullMeta.setOwnerProfile(profile);
            } catch (Exception e) {
                // Bỏ qua lỗi texture
            }
            item.setItemMeta(skullMeta);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String luckStr = (luck >= 0 ? "+" : "") + luck;

            setDisplayName(meta, MiniMessage.miniMessage().deserialize(displayName.replace("%luck%", luckStr)));

            List<Component> lore = new ArrayList<>();
            if (loreTemplate != null) {
                for (String line : loreTemplate) {
                    lore.add(MiniMessage.miniMessage().deserialize(line.replace("%luck%", luckStr)));
                }
            }
            if (lore.isEmpty()) {
                lore.add(MiniMessage.miniMessage().deserialize("<gray>Hãy đặt và phá khối này để thử vận may!</gray>"));
            }
            setLore(meta, lore);

            // Ghi metadata bằng PersistentDataContainer — không dùng reflection
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(KEY_LUCKY_BLOCK_ID,   PersistentDataType.STRING,  id);
            pdc.set(KEY_LUCKY_BLOCK_LUCK, PersistentDataType.INTEGER, luck);

            item.setItemMeta(meta);
        }

        return item;
    }

    public static ItemStack createLuckyBlock(int amount) {
        return createLuckyBlock("classic", -999, amount);
    }

    /**
     * Trả về true nếu ItemStack là một Lucky Block hợp lệ.
     */
    public static boolean isLuckyBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_LUCKY_BLOCK_ID, PersistentDataType.STRING);
    }

    /**
     * Lấy lucky_block_id từ ItemStack. Trả về null nếu không phải LuckyBlock.
     */
    public static String getLuckyBlockId(ItemStack item) {
        if (!isLuckyBlock(item)) return null;
        return item.getItemMeta().getPersistentDataContainer().get(KEY_LUCKY_BLOCK_ID, PersistentDataType.STRING);
    }

    /**
     * Lấy độ luck từ ItemStack. Trả về 0 nếu không có.
     */
    public static int getLuckyBlockLuck(ItemStack item) {
        if (!isLuckyBlock(item)) return 0;
        Integer luck = item.getItemMeta().getPersistentDataContainer().get(KEY_LUCKY_BLOCK_LUCK, PersistentDataType.INTEGER);
        return luck != null ? luck : 0;
    }

    public static ItemStack createPreviewItem() {
        ItemStack item = XMaterial.SPYGLASS.parseItem();
        if (item == null) item = XMaterial.COMPASS.parseItem();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            setDisplayName(meta, MiniMessage.miniMessage().deserialize("<gradient:#00ffcc:#00ccff><b>Kính Lúp Soi Lucky Block</b></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Dùng để soi thông tin của Lucky Block</gray>"));
            setLore(meta, lore);

            // Đánh dấu là preview tool bằng PDC
            meta.getPersistentDataContainer().set(KEY_PREVIEW_TOOL, PersistentDataType.BYTE, (byte) 1);

            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Trả về true nếu item là Preview Tool.
     */
    public static boolean isPreviewTool(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(KEY_PREVIEW_TOOL, PersistentDataType.BYTE);
    }

    private static void setDisplayName(ItemMeta meta, Component component) {
        meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(component));
    }

    private static void setLore(ItemMeta meta, List<Component> components) {
        List<String> legacyLore = new ArrayList<>();
        for (Component c : components) {
            legacyLore.add(LegacyComponentSerializer.legacySection().serialize(c));
        }
        meta.setLore(legacyLore);
    }
}
