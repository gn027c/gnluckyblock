package com.gn027c.luckyblock.paper.command;

import com.gn027c.luckyblock.paper.gnluckyblock;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LampCommandManager {

    private final gnluckyblock plugin;
    private BukkitCommandHandler handler;

    public LampCommandManager(gnluckyblock plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        // Khá»Ÿi táº¡o Lamp handler
        this.handler = BukkitCommandHandler.create(plugin);

        handler.getAutoCompleter().registerSuggestion("item_types", (args, sender, command) -> {
            return java.util.Arrays.asList("luckyblock", "preview");
        });

        handler.getAutoCompleter().registerSuggestion("luckyblock_ids", (args, sender, command) -> {
            List<String> ids = new ArrayList<>();
            ids.add("random");
            List<Map<?, ?>> list = plugin.getConfig().getMapList("lucky-blocks");
            for (Map<?, ?> map : list) {
                Object id = map.get("id");
                if (id != null) ids.add(id.toString());
            }
            return ids;
        });

        // ÄÄƒng kÃ½ class chá»©a lá»‡nh
        handler.register(new LuckyBlockCommand(plugin));
    }

    public BukkitCommandHandler getHandler() {
        return handler;
    }
}

