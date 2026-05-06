package com.gn027c.hosting.paper;

import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class HostingPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Sá»­ dá»¥ng Adventure MiniMessage Ä‘á»ƒ log thÃ´ng bÃ¡o khá»Ÿi Ä‘á»™ng cá»±c Ä‘áº¹p
        var logger = getComponentLogger();
        logger.info(MiniMessage.miniMessage().deserialize(
            "<gradient:#5e4fa2:#f7941d><b>gn027cHosting</b></gradient> <gray>v" + getPluginMeta().getVersion() + " Ä‘Ã£ Ä‘Æ°á»£c kÃ­ch hoáº¡t!</gray>"
        ));
        
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("gn027cHosting Ä‘Ã£ dá»«ng.");
    }
}

