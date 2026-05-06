package com.gn027c.luckyblock.paper.command;

import com.gn027c.luckyblock.paper.gnluckyblock;
import com.gn027c.luckyblock.paper.util.ItemFactory;
import com.gn027c.luckyblock.paper.util.PluginLogger;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Subcommand;

import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("gnlb")
@CommandPermission("gnluckyblock.admin")
public class LuckyBlockCommand {

    private final gnluckyblock plugin;

    public LuckyBlockCommand(gnluckyblock plugin) {
        this.plugin = plugin;
    }

    @Subcommand("reload")
    public void reload(BukkitCommandActor actor) {
        PluginLogger.log(PluginLogger.Flag.COMMANDS, actor.getSender().getName() + " sử dụng lệnh: /gnlb reload");
        plugin.reloadPlugin();
        plugin.getLanguageManager().sendMessage(plugin.getAudience(actor.getSender()), "command.reload-success");
    }

    @Subcommand("preview")
    public void preview(Player player) {
        PluginLogger.log(PluginLogger.Flag.COMMANDS, player.getName() + " sử dụng lệnh: /gnlb preview");
        plugin.getRewardModule().getRewardGUI().openMainMenu(player);
    }

    @Subcommand("give")
    @revxrsal.commands.annotation.AutoComplete("@players @item_types @luckyblock_ids * *")
    public void give(BukkitCommandActor actor,
                     Player target,
                     String itemType,
                     @revxrsal.commands.annotation.Optional String arg3,
                     @revxrsal.commands.annotation.Optional String arg4,
                     @revxrsal.commands.annotation.Optional String arg5) {
        
        PluginLogger.log(PluginLogger.Flag.COMMANDS, actor.getSender().getName() + " sử dụng lệnh: /gnlb give " + target.getName() + " " + itemType + " " + (arg3 != null ? arg3 : "") + " " + (arg4 != null ? arg4 : "") + " " + (arg5 != null ? arg5 : ""));
        
        String typeLower = itemType.toLowerCase();
        
        if (typeLower.equals("preview")) {
            target.getInventory().addItem(ItemFactory.createPreviewItem());
            plugin.getLanguageManager().sendMessageWithPlaceholders(plugin.getAudience(actor.getSender()), "command.give-preview-success", "player", target.getName());
        } else if (typeLower.equals("luckyblock")) {
            String id = arg3;
            if (id == null) {
                plugin.getLanguageManager().sendMessage(plugin.getAudience(actor.getSender()), "command.give-luckyblock-no-id");
                return;
            }
            int amount = 1;
            int luck = -999;
            
            if (arg4 != null) {
                try { amount = Integer.parseInt(arg4); } catch (Exception ignored) {}
            }
            if (arg5 != null) {
                try { luck = Integer.parseInt(arg5); } catch (Exception ignored) {}
            }
            
            ItemStack lb = ItemFactory.createLuckyBlock(id, luck, amount);
            target.getInventory().addItem(lb);
            plugin.getLanguageManager().sendMessageWithPlaceholders(plugin.getAudience(actor.getSender()), "command.give-luckyblock-success", 
                "amount", String.valueOf(amount), 
                "id", id, 
                "player", target.getName());
        } else {
            plugin.getLanguageManager().sendMessageWithPlaceholders(plugin.getAudience(actor.getSender()), "command.invalid-type");
        }
    }

    @Subcommand("dump")
    public void dump(BukkitCommandActor actor) {
        PluginLogger.log(PluginLogger.Flag.COMMANDS, actor.getSender().getName() + " sử dụng lệnh: /gnlb dump");
        org.bukkit.command.CommandSender sender = actor.getSender();
        int rewardCount = plugin.getRewardModule().getRewardManager().getRewards().size();
        boolean debug = plugin.getConfig().getBoolean("settings.debug", false);
        String lang = plugin.getConfig().getString("settings.language", "vi");
        String mode = plugin.getConfig().getString("settings.reward-mode", "TIERED");
        int luck = plugin.getConfig().getInt("settings.luck-threshold", 100);

        sender.sendMessage(org.bukkit.ChatColor.GOLD + "╔═══════════════ gnluckyblock dump ═══════════════");
        sender.sendMessage(org.bukkit.ChatColor.YELLOW + "║ Plugin    : gnluckyblock v" + plugin.getDescription().getVersion());
        sender.sendMessage(org.bukkit.ChatColor.YELLOW + "║ Server    : " + org.bukkit.Bukkit.getVersion());
        sender.sendMessage(org.bukkit.ChatColor.YELLOW + "║ Java      : " + System.getProperty("java.version"));
        sender.sendMessage(org.bukkit.ChatColor.YELLOW + "║ OS        : " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        sender.sendMessage(org.bukkit.ChatColor.GOLD + "╠═══════════════════════════════════════════════");
        sender.sendMessage(org.bukkit.ChatColor.AQUA + "║ Rewards   : " + rewardCount + " phần thưởng đã nạp");
        sender.sendMessage(org.bukkit.ChatColor.AQUA + "║ Mode      : " + mode);
        sender.sendMessage(org.bukkit.ChatColor.AQUA + "║ Language  : " + lang);
        sender.sendMessage(org.bukkit.ChatColor.AQUA + "║ Luck threshold: " + luck);
        sender.sendMessage(org.bukkit.ChatColor.AQUA + "║ Debug     : " + (debug ? org.bukkit.ChatColor.GREEN + "ON" : org.bukkit.ChatColor.RED + "OFF"));
        sender.sendMessage(org.bukkit.ChatColor.GOLD + "╠═══════════════════════════════════════════════");
        sender.sendMessage(org.bukkit.ChatColor.GRAY + "║ Gợi ý: Nếu gặp lỗi, hãy gửi output này kèm");
        sender.sendMessage(org.bukkit.ChatColor.GRAY + "║ stack trace từ console cho developer.");
        sender.sendMessage(org.bukkit.ChatColor.GOLD + "╚═══════════════════════════════════════════════");
    }
}
