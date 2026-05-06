package com.gn027c.luckyblock.paper.listener;

import com.gn027c.luckyblock.paper.block.BlockModule;
import com.gn027c.luckyblock.paper.reward.RewardModule;
import com.gn027c.luckyblock.core.reward.Reward;
import com.gn027c.luckyblock.paper.util.ItemFactory;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

public class LuckyBlockListener implements Listener {
    private final RewardModule rewardModule;
    private final BlockModule blockModule;
    private final MiniMessage mm = MiniMessage.miniMessage();

    // Set để tránh thực thi giếng nhiều lần trong cùng 1 tick
    private final Set<UUID> wellCooldown = new HashSet<>();

    public LuckyBlockListener(RewardModule rewardModule, BlockModule blockModule) {
        this.rewardModule = rewardModule;
        this.blockModule = blockModule;
    }

    // =========================================================
    // LUCKY BLOCK PLACE
    // =========================================================
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item == null || XMaterial.matchXMaterial(item) == XMaterial.AIR) return;

        String id = ItemFactory.getLuckyBlockId(item);
        if (id != null) {
            int luck = ItemFactory.getLuckyBlockLuck(item);
            blockModule.addLocation(event.getBlock().getLocation(), id, luck);

            org.bukkit.Bukkit.getScheduler().runTask(com.gn027c.luckyblock.paper.gnluckyblock.getInstance(), () -> {
                Block block = event.getBlockPlaced();
                block.setType(Material.YELLOW_STAINED_GLASS);

                org.bukkit.entity.ItemDisplay display = (org.bukkit.entity.ItemDisplay) block.getWorld().spawnEntity(
                    block.getLocation().add(0.5, 0.5, 0.5),
                    org.bukkit.entity.EntityType.ITEM_DISPLAY
                );

                ItemStack headItem = item.clone();
                headItem.setAmount(1);
                display.setItemStack(headItem);

                display.setTransformation(new org.bukkit.util.Transformation(
                    new org.joml.Vector3f(0, 0, 0),
                    new org.joml.Quaternionf(),
                    new org.joml.Vector3f(0.6f, 0.6f, 0.6f),
                    new org.joml.Quaternionf()
                ));
                display.setItemDisplayTransform(org.bukkit.entity.ItemDisplay.ItemDisplayTransform.FIXED);
                display.addScoreboardTag("lucky_block_display");
            });
        }
    }

    // =========================================================
    // LUCKY BLOCK BREAK
    // =========================================================
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (blockModule.isLuckyBlock(block.getLocation())) {
            BlockModule.LuckyBlockData data = blockModule.getData(block.getLocation());
            int blockLuck = (data != null) ? data.luck : 0;
            blockModule.removeLocation(block.getLocation());

            block.getWorld().getNearbyEntities(block.getLocation().add(0.5, 0.5, 0.5), 0.1, 0.1, 0.1).stream()
                .filter(e -> e instanceof org.bukkit.entity.ItemDisplay && e.getScoreboardTags().contains("lucky_block_display"))
                .forEach(org.bukkit.entity.Entity::remove);

            event.setDropItems(false);

            try {
                block.getWorld().spawnParticle(org.bukkit.Particle.valueOf("EXPLOSION_NORMAL"), block.getLocation().add(0.5, 0.5, 0.5), 1);
                block.getWorld().spawnParticle(org.bukkit.Particle.valueOf("FIREWORKS_SPARK"), block.getLocation().add(0.5, 0.5, 0.5), 30);
                block.getWorld().spawnParticle(org.bukkit.Particle.valueOf("VILLAGER_HAPPY"), block.getLocation().add(0.5, 0.5, 0.5), 20);
            } catch (Exception ignored) {}

            XSound.ENTITY_FIREWORK_ROCKET_BLAST.play(block.getLocation());
            XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(block.getLocation());

            int threshold = com.gn027c.luckyblock.paper.gnluckyblock.getInstance().getConfig().getInt("settings.luck-threshold", 100);
            Reward reward = rewardModule.getRewardManager().getRandomReward(blockLuck, threshold);

            if (reward != null) {
                rewardModule.executeReward(player, reward);
            }
        }
    }

    // =========================================================
    // PREVIEW TOOL
    // =========================================================
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreviewInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !ItemFactory.isPreviewTool(item)) return;

        event.setCancelled(true);
        Block block = event.getClickedBlock();
        if (block == null || !blockModule.isLuckyBlock(block.getLocation())) return;

        BlockModule.LuckyBlockData data = blockModule.getData(block.getLocation());
        if (data == null) return;

        XSound.BLOCK_AMETHYST_BLOCK_CHIME.play(block.getLocation());
        try {
            block.getWorld().spawnParticle(org.bukkit.Particle.valueOf("VILLAGER_HAPPY"), block.getLocation().add(0.5, 1.2, 0.5), 10, 0.2, 0.2, 0.2);
        } catch (Exception ignored) {}

        // All ASCII-safe MiniMessage — no Vietnamese in this string
        String msg = "<gradient:#00fbff:#0077ff><b>[Lucky Block Info]</b></gradient> <gray>ID:</gray> <white>" + data.id + "</white> <gray>|</gray> <yellow>Luck:</yellow> <white>" + data.luck + "%</white>";
        com.gn027c.luckyblock.paper.gnluckyblock.getInstance().getAudience(player).sendActionBar(mm.deserialize(msg));
    }

    // =========================================================
    // WISHING WELL - RIGHT CLICK (dùng nugget trực tiếp tay)
    // =========================================================
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWishingWellInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        // Chỉ xử lý RIGHT_CLICK_BLOCK, và chỉ main hand để tránh kích hoạt 2 lần
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() != Material.GOLD_NUGGET) return;

        Block block = event.getClickedBlock();
        if (block == null || !block.hasMetadata("wishing_well")) return;

        // Guard: tránh kích hoạt 2 lần trong cùng 1 event cycle
        if (wellCooldown.contains(player.getUniqueId())) return;
        wellCooldown.add(player.getUniqueId());
        org.bukkit.Bukkit.getScheduler().runTask(com.gn027c.luckyblock.paper.gnluckyblock.getInstance(), () -> wellCooldown.remove(player.getUniqueId()));

        event.setCancelled(true);
        itemInHand.setAmount(itemInHand.getAmount() - 1);
        executeWellReward(player, block.getLocation());
    }

    // =========================================================
    // WISHING WELL - DROP (ném hạt vàng vào giếng)
    // =========================================================
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWishingWellDrop(org.bukkit.event.player.PlayerDropItemEvent event) {
        org.bukkit.entity.Item itemEntity = event.getItemDrop();
        if (itemEntity.getItemStack().getType() != Material.GOLD_NUGGET) return;

        // Dùng flag atomic để đảm bảo executeWellReward chỉ chạy 1 lần cho mỗi item entity
        final boolean[] executed = {false};

        new org.bukkit.scheduler.BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                // Timeout hoặc item đã bị xóa
                if (ticks++ > 100 || !itemEntity.isValid()) {
                    this.cancel();
                    return;
                }
                Block b = itemEntity.getLocation().getBlock();
                // Chỉ kích hoạt khi item nằm trong khối nước có metadata giếng
                if (b.hasMetadata("wishing_well") && b.getType() == Material.WATER) {
                    // Guard: chỉ chạy 1 lần
                    if (executed[0]) {
                        this.cancel();
                        return;
                    }
                    executed[0] = true;
                    itemEntity.remove();
                    this.cancel();
                    // executeWellReward phải chạy sau cancel/remove để metadata đã được set
                    executeWellReward(event.getPlayer(), b.getLocation());
                }
            }
        }.runTaskTimer(com.gn027c.luckyblock.paper.gnluckyblock.getInstance(), 2L, 1L);
    }

    // =========================================================
    // WISHING WELL - EXECUTE REWARD
    // =========================================================
    private void executeWellReward(Player player, org.bukkit.Location clickedLoc) {
        // Tìm centerLoc từ metadata của khối được click
        org.bukkit.Location centerLoc = null;
        if (clickedLoc.getBlock().hasMetadata("wishing_well_center")) {
            Object val = clickedLoc.getBlock().getMetadata("wishing_well_center").get(0).value();
            if (val instanceof org.bukkit.Location) {
                centerLoc = (org.bukkit.Location) val;
            }
        }
        if (centerLoc == null) return;

        Block centerBlock = centerLoc.getBlock();

        int uses = centerBlock.hasMetadata("wishing_well_uses")
            ? centerBlock.getMetadata("wishing_well_uses").get(0).asInt() : 0;

        if (uses <= 0) {
            // [Gi\u1ebfng \u01af\u1edbc Nguy\u1ec7n] Gi\u1ebfng \u0111\u00e3 c\u1ea1n ki\u1ec7t n\u0103ng l\u01b0\u1ee3ng!
            com.gn027c.luckyblock.paper.gnluckyblock.getInstance().getLanguageManager().sendMessage(
                com.gn027c.luckyblock.paper.gnluckyblock.getInstance().getAudience(player), 
                "rewards.wishing-well-empty"
            );
            return;
        }

        uses--;
        centerBlock.setMetadata("wishing_well_uses",
            new org.bukkit.metadata.FixedMetadataValue(com.gn027c.luckyblock.paper.gnluckyblock.getInstance(), uses));

        // Hiệu ứng
        try {
            clickedLoc.getWorld().spawnParticle(org.bukkit.Particle.valueOf("VILLAGER_HAPPY"), clickedLoc.clone().add(0.5, 1, 0.5), 20, 0.5, 0.5, 0.5);
            clickedLoc.getWorld().spawnParticle(org.bukkit.Particle.valueOf("WATER_SPLASH"), clickedLoc.clone().add(0.5, 1, 0.5), 50, 0.2, 0.2, 0.2);
        } catch (Exception ignored) {}
        XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(clickedLoc);

        Reward reward = rewardModule.getRewardManager().getRandomReward();
        if (reward != null) {
            // [\u0110i\u1ec1u \u01b0\u1edbc] \u0110i\u1ec1u \u01b0\u1edbc c\u1ee7a b\u1ea1n \u0111\u00e3 \u0111\u01b0\u1ee3c ch\u1ea5p nh\u1eadn! (C\u00f2n l\u1ea1i X l\u1ea7n)
            com.gn027c.luckyblock.paper.gnluckyblock.getInstance().getLanguageManager().sendMessageWithPlaceholders(
                com.gn027c.luckyblock.paper.gnluckyblock.getInstance().getAudience(player), 
                "rewards.wishing-well-granted", 
                "uses", String.valueOf(uses)
            );
            rewardModule.executeReward(player, reward);
        }

        if (uses <= 0) {
            // Gi\u1ebfng \u01b0\u1edbc nguy\u1ec7n \u0111\u00e3 bi\u1ebfn m\u1ea5t!
            com.gn027c.luckyblock.paper.gnluckyblock.getInstance().getLanguageManager().sendMessage(
                com.gn027c.luckyblock.paper.gnluckyblock.getInstance().getAudience(player), 
                "rewards.wishing-well-vanished"
            );
            try {
                clickedLoc.getWorld().spawnParticle(org.bukkit.Particle.valueOf("SMOKE_LARGE"), centerLoc, 100, 1, 1, 1);
            } catch (Exception ignored) {}
            XSound.ENTITY_GENERIC_EXTINGUISH_FIRE.play(centerLoc);

            // Xóa toàn bộ khối giếng
            for (int y = -1; y <= 4; y++) {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        Block b = centerLoc.clone().add(x, y - 1, z).getBlock();
                        if (b.hasMetadata("wishing_well")) {
                            b.setType(Material.AIR);
                            b.removeMetadata("wishing_well", com.gn027c.luckyblock.paper.gnluckyblock.getInstance());
                            b.removeMetadata("wishing_well_center", com.gn027c.luckyblock.paper.gnluckyblock.getInstance());
                        }
                    }
                }
            }
        }
    }
}
