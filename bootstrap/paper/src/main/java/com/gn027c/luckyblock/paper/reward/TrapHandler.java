package com.gn027c.luckyblock.paper.reward;

import com.gn027c.luckyblock.paper.gnluckyblock;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Random;

public class TrapHandler {
    private static final Random RANDOM = new Random();

    public static boolean handle(Player player, String action, Map<String, Object> data) {
        switch (action.toLowerCase()) {
            case "sand_burial": {
                Location loc = player.getLocation();
                for (int y = 0; y <= 5; y++)
                    for (int x = -2; x <= 2; x++)
                        for (int z = -2; z <= 2; z++)
                            player.getWorld().spawnFallingBlock(loc.clone().add(x, y, z), Material.SAND.createBlockData());
                return true;
            }
            case "gravel_rain": {
                Location loc = player.getLocation();
                for (int i = 0; i < 9; i++)
                    player.getWorld().spawnFallingBlock(loc.clone().add(i % 3 - 1, 8, i / 3 - 1), Material.GRAVEL.createBlockData());
                return true;
            }
            case "stone_rain": {
                Location loc = player.getLocation().add(0, 15, 0);
                for (int i = 0; i < 15; i++) {
                    double x = RANDOM.nextDouble() * 6 - 3;
                    double z = RANDOM.nextDouble() * 6 - 3;
                    FallingBlock fb = player.getWorld().spawnFallingBlock(loc.clone().add(x, 0, z), Material.COBBLESTONE.createBlockData());
                    fb.setHurtEntities(true);
                    fb.setDropItem(false);
                }
                return true;
            }
            case "ice_cage": {
                Location loc = player.getLocation();
                for (int y = 0; y <= 2; y++)
                    for (int x = -1; x <= 1; x++)
                        for (int z = -1; z <= 1; z++) {
                            if (x == 0 && z == 0 && y < 2) continue;
                            loc.clone().add(x, y, z).getBlock().setType(Material.PACKED_ICE);
                        }
                return true;
            }
            case "silverfish_surprise": {
                Location loc = player.getLocation();
                for (int i = 0; i < 8; i++)
                    player.getWorld().spawnEntity(loc.clone().add(RANDOM.nextDouble()*4-2, 0, RANDOM.nextDouble()*4-2), EntityType.SILVERFISH);
                return true;
            }
            case "inventory_shuffle": {
                ItemStack[] contents = player.getInventory().getContents().clone();
                for (int i = contents.length - 1; i > 0; i--) {
                    int j = RANDOM.nextInt(i + 1);
                    ItemStack t = contents[i]; contents[i] = contents[j]; contents[j] = t;
                }
                player.getInventory().setContents(contents);
                return true;
            }
            case "hotbar_scramble": {
                for (int i = 8; i > 0; i--) {
                    int j = RANDOM.nextInt(i + 1);
                    ItemStack a = player.getInventory().getItem(i);
                    player.getInventory().setItem(i, player.getInventory().getItem(j));
                    player.getInventory().setItem(j, a);
                }
                return true;
            }
            case "fling_forward": {
                player.setVelocity(player.getLocation().getDirection().multiply(4).setY(0.5));
                return true;
            }
            case "fling_random": {
                player.setVelocity(new org.bukkit.util.Vector(RANDOM.nextDouble()*4-2, 2, RANDOM.nextDouble()*4-2));
                return true;
            }
            case "fire_ring": {
                Location loc = player.getLocation();
                for (int i = 0; i < 12; i++) {
                    double angle = i * Math.PI * 2 / 12;
                    loc.clone().add(Math.cos(angle)*2, 0, Math.sin(angle)*2).getBlock().setType(Material.FIRE);
                }
                return true;
            }
            case "soul_sand_trap": {
                Location loc = player.getLocation();
                for (int x = -2; x <= 2; x++)
                    for (int z = -2; z <= 2; z++)
                        loc.clone().add(x, -1, z).getBlock().setType(Material.SOUL_SAND);
                return true;
            }
            case "honey_trap": {
                Location loc = player.getLocation();
                for (int x = -1; x <= 1; x++)
                    for (int z = -1; z <= 1; z++)
                        loc.clone().add(x, -1, z).getBlock().setType(Material.HONEY_BLOCK);
                return true;
            }
            case "slime_bounce": {
                Location loc = player.getLocation();
                for (int x = -1; x <= 1; x++)
                    for (int z = -1; z <= 1; z++)
                        loc.clone().add(x, -1, z).getBlock().setType(Material.SLIME_BLOCK);
                return true;
            }
            case "firework_show": {
                Location loc = player.getLocation();
                for (int i = 0; i < 5; i++) {
                    EntityType fwType;
                    try { fwType = EntityType.valueOf("FIREWORK_ROCKET"); } catch (Exception e) { fwType = EntityType.valueOf("FIREWORK"); }
                    Firework fw = (Firework) player.getWorld().spawnEntity(loc.clone().add(RANDOM.nextInt(5)-2, 0, RANDOM.nextInt(5)-2), fwType);
                    org.bukkit.inventory.meta.FireworkMeta fm = fw.getFireworkMeta();
                    fm.addEffect(org.bukkit.FireworkEffect.builder()
                        .withColor(Color.fromRGB(RANDOM.nextInt(256),RANDOM.nextInt(256),RANDOM.nextInt(256)))
                        .withFade(Color.fromRGB(RANDOM.nextInt(256),RANDOM.nextInt(256),RANDOM.nextInt(256)))
                        .with(org.bukkit.FireworkEffect.Type.values()[RANDOM.nextInt(5)])
                        .trail(true).flicker(true).build());
                    fm.setPower(1 + RANDOM.nextInt(2));
                    fw.setFireworkMeta(fm);
                }
                return true;
            }
            case "diamond_rain": {
                Location loc = player.getLocation().add(0, 10, 0);
                for (int i = 0; i < 10; i++)
                    player.getWorld().dropItemNaturally(loc.clone().add(RANDOM.nextDouble()*4-2, 0, RANDOM.nextDouble()*4-2), new ItemStack(Material.DIAMOND));
                return true;
            }
            case "lava_tower": {
                Location loc = player.getLocation().add(3,0,0);
                for (int y = 0; y <= 6; y++) loc.clone().add(0,y,0).getBlock().setType(Material.NETHERRACK);
                loc.clone().add(0,7,0).getBlock().setType(Material.LAVA);
                return true;
            }
            case "tnt_house": {
                Location loc = player.getLocation().add(3,0,0);
                for (int y = 0; y <= 2; y++)
                    for (int x = 0; x <= 2; x++)
                        for (int z = 0; z <= 2; z++) {
                            if (x==1&&z==1&&y>0) loc.clone().add(x,y,z).getBlock().setType(Material.AIR);
                            else loc.clone().add(x,y,z).getBlock().setType(Material.TNT);
                        }
                return true;
            }
            case "nothing": {
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return true;
            }
            case "lucky_dice": {
                int roll = 1 + RANDOM.nextInt(6);
                gnluckyblock.getInstance().getAudience(player).sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize("<gold>\ud83c\udfb2 B\u1ea1n tung \u0111\u01b0\u1ee3c sá»‘ <bold>" + roll + "</bold>!</gold>"));
                if (roll == 6) {
                    player.getInventory().addItem(new ItemStack(Material.DIAMOND, 10));
                    gnluckyblock.getInstance().getAudience(player).sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize("<green>JACKPOT! 10 Kim c\u01b0\u01a1ng!</green>"));
                } else if (roll == 1) {
                    player.getWorld().strikeLightning(player.getLocation());
                    gnluckyblock.getInstance().getAudience(player).sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize("<red>Xui qu\u00e1! Bá»‹ s\u00e9t \u0111\u00e1nh!</red>"));
                } else {
                    player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, roll));
                }
                return true;
            }
            case "farm_auto": {
                Location loc = player.getLocation().add(2, -1, 2);
                for (int x = -2; x <= 2; x++)
                    for (int z = -2; z <= 2; z++) {
                        loc.clone().add(x, 0, z).getBlock().setType(Material.FARMLAND);
                        loc.clone().add(x, 1, z).getBlock().setType(Material.WHEAT);
                        if (x == 0 && z == 0) loc.clone().add(x, 0, z).getBlock().setType(Material.WATER);
                    }
                loc.clone().add(2, 1, 2).getBlock().setType(Material.CHEST);
                return true;
            }
            case "library": {
                Location loc = player.getLocation().add(2, 0, 2);
                for (int y = 0; y <= 2; y++)
                    for (int x = -2; x <= 2; x++)
                        for (int z = -2; z <= 2; z++) {
                            if (Math.abs(x) == 2 || Math.abs(z) == 2)
                                loc.clone().add(x, y, z).getBlock().setType(Material.BOOKSHELF);
                        }
                loc.clone().add(0, 0, 0).getBlock().setType(Material.ENCHANTING_TABLE);
                return true;
            }
            case "garden": {
                Location loc = player.getLocation();
                Material[] flowers = {Material.POPPY, Material.DANDELION, Material.BLUE_ORCHID, Material.ALLIUM, Material.AZURE_BLUET};
                for (int x = -3; x <= 3; x++)
                    for (int z = -3; z <= 3; z++) {
                        if (RANDOM.nextInt(3) == 0)
                            loc.clone().add(x, 0, z).getBlock().setType(flowers[RANDOM.nextInt(flowers.length)]);
                    }
                return true;
            }
            case "smithing_station": {
                Location loc = player.getLocation().add(2, 0, 0);
                loc.clone().add(0, 0, 0).getBlock().setType(Material.SMITHING_TABLE);
                loc.clone().add(1, 0, 0).getBlock().setType(Material.ANVIL);
                loc.clone().add(0, 0, 1).getBlock().setType(Material.BLAST_FURNACE);
                loc.clone().add(1, 0, 1).getBlock().setType(Material.CHEST);
                return true;
            }
            case "end_portal_mini": {
                Location loc = player.getLocation().add(3, -1, 3);
                for (int x = -1; x <= 1; x++)
                    for (int z = -1; z <= 1; z++) {
                        loc.clone().add(x, 0, z).getBlock().setType(Material.END_STONE);
                        if (Math.abs(x) == 1 || Math.abs(z) == 1)
                            loc.clone().add(x, 1, z).getBlock().setType(Material.END_PORTAL_FRAME);
                    }
                loc.clone().add(0, 1, 0).getBlock().setType(Material.END_PORTAL);
                return true;
            }
            
            // ===== NEW TRAPS =====
            case "tnt_minecart": { // Sá»\u00eda lá»—i xe má»\u008f thuốc n\u1ed5
                Location loc = player.getLocation().add(RANDOM.nextInt(3) - 1, 0, RANDOM.nextInt(3) - 1);
                try {
                    // Trong 1.21.1, d\u00f9ng EntityType.MINECART_TNT tr\u1ef1c ti\u1ebfp
                    player.getWorld().spawnEntity(loc, EntityType.MINECART_TNT);
                } catch (Exception e) {
                    // Fallback
                    player.getWorld().spawn(loc, org.bukkit.entity.minecart.ExplosiveMinecart.class);
                }
                return true;
            }
            case "hole_10": { // Há»‘ 10 block
                Location loc = player.getLocation();
                for (int y = 0; y >= -10; y--) {
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            loc.clone().add(x, y, z).getBlock().setType(Material.AIR);
                        }
                    }
                }
                return true;
            }
            case "random_mouse": { // Di chu\u1ed9t ng\u1eabu nhi\u00ean
                Location loc = player.getLocation();
                loc.setYaw(RANDOM.nextFloat() * 360);
                loc.setPitch(RANDOM.nextFloat() * 180 - 90);
                player.teleport(loc);
                return true;
            }
            case "inverted_controls": { // \u0110i\u1ec1u khi\u1ec3n ng\u01b0\u1ee3c (d\u00f9ng Nausea v\u00e0 hi\u1ec7u \u1ee9ng lag)
                player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 0));
                return true;
            }
            case "levitation_trap": { // B\u1ecb hi\u1ec7u \u1ee9ng bay
                player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 2));
                return true;
            }
            case "lava_lake": { // Há»“ lava (lava d\u01b0\u1edbi, t\u01a1 nh\u1ec7n tr\u00ean)
                Location loc = player.getLocation();
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        loc.clone().add(x, -2, z).getBlock().setType(Material.LAVA);
                        loc.clone().add(x, -1, z).getBlock().setType(Material.COBWEB);
                        loc.clone().add(x, 0, z).getBlock().setType(Material.AIR);
                    }
                }
                return true;
            }
            case "set_hp_2": { // Set m\u00e1u c\u00f2n 2
                player.setHealth(2.0);
                return true;
            }
            case "cursed_pumpkin": { // Đội bí ngô lời nguyền
                ItemStack pumpkin = new ItemStack(Material.CARVED_PUMPKIN);
                ItemMeta meta = pumpkin.getItemMeta();
                if (meta != null) {
                    meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
                    meta.setDisplayName(ChatColor.RED + "Lời Nguyền Bí Ngô");
                    pumpkin.setItemMeta(meta);
                }
                player.getInventory().setHelmet(pumpkin);
                return true;
            }
            case "chest_roulette": { // Minigame Rương Roulette
                Location loc = player.getLocation().add(1, 0, 0);
                loc.getBlock().setType(Material.CHEST);
                org.bukkit.block.Chest chest = (org.bukkit.block.Chest) loc.getBlock().getState();
                
                new BukkitRunnable() {
                    int count = 0;
                    Material[] pool = {Material.DIAMOND, Material.GOLD_INGOT, Material.EMERALD, Material.IRON_INGOT, Material.NETHERITE_INGOT, Material.APPLE, Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE, Material.EXPERIENCE_BOTTLE};
                    
                    @Override
                    public void run() {
                        if (count >= 20) {
                            chest.getInventory().clear();
                            Material finalMat = pool[RANDOM.nextInt(pool.length)];
                            chest.getInventory().addItem(new ItemStack(finalMat, RANDOM.nextInt(3) + 1));
                            loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                            try {
                                loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0.5, 0.5, 0.5), 20, 0.5, 0.5, 0.5);
                            } catch (Exception e) {}
                            this.cancel();
                            return;
                        }
                        
                        chest.getInventory().clear();
                        for (int i = 0; i < 27; i++) {
                            if (RANDOM.nextBoolean()) {
                                chest.getInventory().setItem(i, new ItemStack(pool[RANDOM.nextInt(pool.length)]));
                            }
                        }
                        try {
                            loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1 + (count * 0.05f));
                        } catch (Exception e) {}
                        count++;
                    }
                }.runTaskTimer(gnluckyblock.getInstance(), 0, 3);
                return true;
            }
        }
        return false;
    }
}
