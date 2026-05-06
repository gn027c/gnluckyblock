package com.gn027c.luckyblock.paper.gui;

import com.gn027c.luckyblock.core.reward.Reward;
import com.gn027c.luckyblock.core.reward.RewardType;
import com.gn027c.luckyblock.paper.gnluckyblock;
import com.gn027c.luckyblock.paper.reward.RewardModule;
import com.cryptomorin.xseries.XMaterial;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RewardGUI implements Listener {
    private final gnluckyblock plugin;
    private final RewardModule rewardModule;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final NamespacedKey KEY_ACTION = new NamespacedKey(gnluckyblock.getInstance(), "gui_action");
    private final NamespacedKey KEY_TYPE = new NamespacedKey(gnluckyblock.getInstance(), "gui_type");
    private final NamespacedKey KEY_PAGE = new NamespacedKey(gnluckyblock.getInstance(), "gui_page");
    private final NamespacedKey KEY_REWARD_ID = new NamespacedKey(gnluckyblock.getInstance(), "gui_reward_id");

    // Identifier prefixes dùng để nhận diện inventory của plugin trong onClick
    private static final String TITLE_PREFIX_MAIN = "Lucky Block - Danh m\u1ee5c";
    private static final String TITLE_PREFIX_CATEGORY = "LB - ";

    public RewardGUI(gnluckyblock plugin, RewardModule rewardModule) {
        this.plugin = plugin;
        this.rewardModule = rewardModule;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // === Dùng Adventure API trực tiếp, không qua legacy serializer ===

    public void openMainMenu(Player player) {
        Component title = mm.deserialize("<gold>Lucky Block - Danh m\u1ee5c</gold>");
        Inventory inv = Bukkit.createInventory(null, 27, title);

        inv.setItem(10, createItem(XMaterial.DIAMOND, "<aqua>V\u1eact ph\u1ea9m (ITEM)</aqua>", "OPEN_TYPE", "ITEM", 0));
        inv.setItem(11, createItem(XMaterial.ZOMBIE_HEAD, "<green>Th\u1ef1c th\u1ec3 (ENTITY)</green>", "OPEN_TYPE", "ENTITY", 0));
        inv.setItem(12, createItem(XMaterial.TNT, "<red>B\u1eaby (TRAP)</red>", "OPEN_TYPE", "TRAP", 0));
        inv.setItem(14, createItem(XMaterial.BRICK, "<yellow>C\u1ea3nh quan (STRUCTURE)</yellow>", "OPEN_TYPE", "STRUCTURE", 0));
        inv.setItem(15, createItem(XMaterial.POTION, "<light_purple>Hi\u1ec7u \u1ee9ng (EFFECT)</light_purple>", "OPEN_TYPE", "EFFECT", 0));
        inv.setItem(16, createItem(XMaterial.COMMAND_BLOCK, "<white>L\u1ec7nh (COMMAND)</white>", "OPEN_TYPE", "COMMAND", 0));

        player.openInventory(inv);
    }

    public void openCategoryMenu(Player player, RewardType type, int page) {
        Component title = mm.deserialize("<gold>LB - " + type.name() + " (Trang " + (page + 1) + ")</gold>");
        Inventory inv = Bukkit.createInventory(null, 54, title);

        List<Reward> allRewards = rewardModule.getRewardManager().getRewards().stream()
                .filter(r -> r.getType() == type)
                .collect(Collectors.toList());

        List<Reward> goodRewards = allRewards.stream().filter(this::isGood).collect(Collectors.toList());
        List<Reward> badRewards = allRewards.stream().filter(r -> !isGood(r)).collect(Collectors.toList());

        int itemsPerPage = 16;
        int start = page * itemsPerPage;

        inv.setItem(4, createItem(XMaterial.LIME_STAINED_GLASS_PANE, "<green>\u2714 T\u1ed1t</green>", null, null, 0));

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int idx = start + (i * 4 + j);
                if (idx < goodRewards.size()) {
                    inv.setItem((i + 1) * 9 + j, createRewardItem(goodRewards.get(idx)));
                }
            }
            inv.setItem((i + 1) * 9 + 4, createItem(XMaterial.WHITE_STAINED_GLASS_PANE, "<gray>|</gray>", null, null, 0));
            for (int j = 5; j < 9; j++) {
                int idx = start + (i * 4 + (j - 5));
                if (idx < badRewards.size()) {
                    inv.setItem((i + 1) * 9 + j, createRewardItem(badRewards.get(idx)));
                }
            }
        }

        // Header dấu hiệu bên phải
        inv.setItem(8, createItem(XMaterial.RED_STAINED_GLASS_PANE, "<red>\u2716 X\u1ea5u</red>", null, null, 0));

        for (int i = 45; i < 54; i++) {
            inv.setItem(i, createItem(XMaterial.GRAY_STAINED_GLASS_PANE, "<gray> </gray>", null, null, 0));
        }

        if (page > 0) {
            inv.setItem(45, createItem(XMaterial.ARROW, "<yellow>\u25c0 Trang tr\u01b0\u1edbc</yellow>", "OPEN_TYPE", type.name(), page - 1));
        }
        inv.setItem(49, createItem(XMaterial.BARRIER, "<red>Tr\u1edf l\u1ea1i</red>", "BACK", null, 0));

        int totalMax = Math.max(goodRewards.size(), badRewards.size());
        if (start + itemsPerPage < totalMax) {
            inv.setItem(53, createItem(XMaterial.ARROW, "<yellow>Trang sau \u25b6</yellow>", "OPEN_TYPE", type.name(), page + 1));
        }

        player.openInventory(inv);
    }

    private boolean isGood(Reward r) {
        if (r.getLuck() > 0) return true;
        if (r.getLuck() < 0) return false;
        String id = r.getId().toLowerCase();
        String[] badKeywords = {"troll", "bad", "tnt", "lava", "phantom", "creeper", "cage", "fling",
                "lightning", "sand_burial", "gravel_rain", "silverfish", "shuffle", "scramble",
                "potion_cloud", "strip_armor", "drop_held", "fake_creeper", "magma", "minecart",
                "cobweb", "void_hole", "nothing", "hole_10", "random_mouse", "inverted",
                "levitation", "set_hp_2", "cursed_pumpkin"};
        for (String kw : badKeywords) {
            if (id.contains(kw)) return false;
        }
        return true;
    }

    private ItemStack createItem(XMaterial mat, String miniMessageName, String action, String type, int page) {
        ItemStack item = mat.parseItem();
        if (item == null) item = new ItemStack(Material.STONE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Dùng Adventure API trực tiếp
            meta.displayName(mm.deserialize("<!italic>" + miniMessageName));
            if (action != null) meta.getPersistentDataContainer().set(KEY_ACTION, PersistentDataType.STRING, action);
            if (type != null) meta.getPersistentDataContainer().set(KEY_TYPE, PersistentDataType.STRING, type);
            meta.getPersistentDataContainer().set(KEY_PAGE, PersistentDataType.INTEGER, page);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createRewardItem(Reward r) {
        XMaterial mat = XMaterial.PAPER;
        switch (r.getType()) {
            case ITEM: mat = XMaterial.CHEST; break;
            case ENTITY: mat = XMaterial.ZOMBIE_SPAWN_EGG; break;
            case EFFECT: mat = XMaterial.GLASS_BOTTLE; break;
            case COMMAND: mat = XMaterial.COMMAND_BLOCK; break;
            case TRAP: mat = XMaterial.TNT; break;
            case STRUCTURE: mat = XMaterial.BRICK; break;
        }
        ItemStack item = mat.parseItem();
        if (item == null) item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String color = isGood(r) ? "<green>" : "<red>";
            String closeColor = isGood(r) ? "</green>" : "</red>";
            // Tên item: dùng adventure displayName
            meta.displayName(mm.deserialize("<!italic>" + color + r.getId() + closeColor));

            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<!italic><gray>Lo\u1ea1i: <white>" + r.getType().name() + "</white></gray>"));
            lore.add(mm.deserialize("<!italic><gray>Tr\u1ecdng s\u1ed1: <white>" + r.getWeight() + "</white></gray>"));
            lore.add(mm.deserialize("<!italic><gray>\u0110\u1ed9 may m\u1eafn: " + (r.getLuck() >= 0 ? "<green>" : "<red>") + r.getLuck() + (r.getLuck() >= 0 ? "</green>" : "</red>") + "</gray>"));

            if (r.getType() == RewardType.ITEM) {
                Map<String, Object> data = r.getData();
                if (data.containsKey("items")) {
                    List<?> items = (List<?>) data.get("items");
                    lore.add(mm.deserialize("<!italic><gray>V\u1eadt ph\u1ea9m: <white>" + items.size() + " lo\u1ea1i</white></gray>"));
                    lore.add(mm.deserialize("<!italic><gray>Ch\u1ebf \u0111\u1ed9: <gold>C\u1ed1 \u0111\u1ecbnh</gold></gray>"));
                } else if (data.containsKey("material")) {
                    String matName = (String) data.get("material");
                    int amount = data.containsKey("amount") ? ((Number) data.get("amount")).intValue() : 1;
                    lore.add(mm.deserialize("<!italic><gray>V\u1eadt ph\u1ea9m: <white>" + matName + "</white></gray>"));
                    lore.add(mm.deserialize("<!italic><gray>S\u1ed1 l\u01b0\u1ee3ng: <white>" + amount + "</white></gray>"));
                    lore.add(mm.deserialize("<!italic><gray>Ch\u1ebf \u0111\u1ed9: <gold>C\u1ed1 \u0111\u1ecbnh</gold></gray>"));
                }
            } else if (r.getType() == RewardType.TRAP && r.getId().contains("dice")) {
                lore.add(mm.deserialize("<!italic><gray>Ch\u1ebf \u0111\u1ed9: <gold>Ng\u1eabu nhi\u00ean (1-6)</gold></gray>"));
            }

            if (r.getAnnouncement() != null && !r.getAnnouncement().isEmpty()) {
                // Announcement có thể chứa MiniMessage tag, dùng trực tiếp
                lore.add(mm.deserialize("<!italic><gray>Th\u00f4ng b\u00e1o: </gray>" + r.getAnnouncement()));
            }
            lore.add(Component.empty());
            lore.add(mm.deserialize("<!italic><yellow>Click \u0111\u1ec3 th\u1eed th\u1ef1c thi!</yellow>"));

            // Dùng Adventure API trực tiếp cho lore
            meta.lore(lore);

            meta.getPersistentDataContainer().set(KEY_ACTION, PersistentDataType.STRING, "EXECUTE");
            meta.getPersistentDataContainer().set(KEY_REWARD_ID, PersistentDataType.STRING, r.getId());

            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Dùng Adventure title để check, tránh dùng legacy getTitle()
        Component titleComponent = event.getView().title();
        String plainTitle = PlainTextComponentSerializer.plainText().serialize(titleComponent);
        if (!plainTitle.contains("Lucky Block") && !plainTitle.contains("LB - ")) return;

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        String action = meta.getPersistentDataContainer().get(KEY_ACTION, PersistentDataType.STRING);
        if (action == null) return;

        switch (action) {
            case "OPEN_TYPE":
                String typeStr = meta.getPersistentDataContainer().get(KEY_TYPE, PersistentDataType.STRING);
                int page = meta.getPersistentDataContainer().getOrDefault(KEY_PAGE, PersistentDataType.INTEGER, 0);
                if (typeStr != null) openCategoryMenu(player, RewardType.valueOf(typeStr), page);
                break;
            case "BACK":
                openMainMenu(player);
                break;
            case "EXECUTE":
                String rewardId = meta.getPersistentDataContainer().get(KEY_REWARD_ID, PersistentDataType.STRING);
                if (rewardId != null) {
                    for (Reward r : rewardModule.getRewardManager().getRewards()) {
                        if (r.getId().equalsIgnoreCase(rewardId)) {
                            player.closeInventory();
                            rewardModule.executeReward(player, r);
                            plugin.getAudience(player).sendMessage(mm.deserialize(
                                "<green>\u0110\u00e3 th\u1ef1c thi ph\u1ea7n th\u01b0\u1edfng: </green><yellow>" + r.getId() + "</yellow>"));
                            return;
                        }
                    }
                }
                break;
        }
    }
}
