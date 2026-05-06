package com.gn027c.luckyblock.paper.command;

import com.gn027c.luckyblock.paper.gnluckyblock;
import com.gn027c.luckyblock.paper.util.ItemFactory;
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
        plugin.reloadPlugin();
        plugin.getLanguageManager().sendMessage(plugin.getAudience(actor.getSender()), "command.reload-success");
    }

    @Subcommand("preview")
    public void preview(Player player) {
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
}
