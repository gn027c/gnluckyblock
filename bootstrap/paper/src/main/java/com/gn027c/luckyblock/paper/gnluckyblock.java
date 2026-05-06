package com.gn027c.luckyblock.paper;

import com.gn027c.luckyblock.core.config.LanguageManager;
import com.gn027c.luckyblock.core.config.MessageService;
import com.gn027c.luckyblock.core.manager.ModuleManager;
import com.gn027c.luckyblock.paper.block.BlockModule;
import com.gn027c.luckyblock.paper.config.PaperConfig;
import com.gn027c.luckyblock.paper.reward.RewardModule;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.audience.Audience;
import java.io.File;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;

public class gnluckyblock extends JavaPlugin {
    private static gnluckyblock instance;
    private boolean packetEventsLoaded = false;
    private BukkitAudiences adventure;
    
    private MessageService messageService;
    private LanguageManager languageManager;
    private ModuleManager moduleManager;
    private RewardModule rewardModule;
    private BlockModule blockModule;
    
    private PaperConfig mainConfig;

    @Override
    public void onLoad() {
        // ÄÆ°á»£c gá»i khi server khá»Ÿi Ä‘á»™ng. PlugMan KHÃ”NG gá»i láº¡i method nÃ y khi reload.
        loadPacketEvents();
    }

    /**
     * Init PacketEvents má»™t cÃ¡ch an toÃ n. CÃ³ guard Ä‘á»ƒ trÃ¡nh init trÃ¹ng láº·p
     * khi PlugMan reload (onLoad bá»‹ skip, onEnable gá»i láº¡i).
     */
    private void loadPacketEvents() {
        if (packetEventsLoaded) return;
        try {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
            PacketEvents.getAPI().getSettings().checkForUpdates(false);
            PacketEvents.getAPI().load();
            packetEventsLoaded = true;
        } catch (Exception e) {
            getLogger().warning("[PacketEvents] Could not load: " + e.getMessage());
            getLogger().warning("[PacketEvents] Preview effects will be disabled.");
        }
    }

    @Override
    public void onEnable() {
        // Guard: Náº¿u onLoad() bá»‹ skip (PlugMan reload), init PacketEvents á»Ÿ Ä‘Ã¢y
        loadPacketEvents();
        try {
            PacketEvents.getAPI().init();
        } catch (Exception e) {
            getLogger().warning("[PacketEvents] Could not init: " + e.getMessage());
        }
        try {
            instance = this;
            this.adventure = BukkitAudiences.create(this);
            
            // Khởi tạo NamespacedKey cho ItemFactory — phải làm trước mọi thứ khác
            com.gn027c.luckyblock.paper.util.ItemFactory.init(this);
            
            getLogger().info("--- Starting gnluckyblock ---");
            
            // 1. Setup Config & Language
            getLogger().info("[1/3] Loading configuration...");
            saveDefaultConfig();
            this.mainConfig = new PaperConfig(getConfig());
            
            this.messageService = new MessageService();
            this.languageManager = new LanguageManager(messageService);
            loadLanguage();
            getLogger().info("-> Language loaded successfully.");

            // 2. Setup Modules
            getLogger().info("[2/3] Initializing modules...");
            this.moduleManager = new ModuleManager();
            
            this.rewardModule = new RewardModule(this);
            this.blockModule = new BlockModule(this);

            this.moduleManager.registerModule(rewardModule);
            this.moduleManager.registerModule(blockModule);
            
            this.moduleManager.enableModules();
            getLogger().info("-> Activated " + moduleManager.getModules().size() + " modules.");

            // 3. Register Commands (Lamp Framework)
            com.gn027c.luckyblock.paper.command.LampCommandManager commandManager = new com.gn027c.luckyblock.paper.command.LampCommandManager(this);
            commandManager.registerCommands();
            getLogger().info("-> Commands registered via Lamp Framework.");

            // 4. Register Listeners
            getLogger().info("[3/3] Registering events...");
            getServer().getPluginManager().registerEvents(new com.gn027c.luckyblock.paper.listener.LuckyBlockListener(rewardModule, blockModule), this);

            getLogger().info("--- gnluckyblock is ready! ---");
        } catch (Exception e) {
            getLogger().severe("!!! CRITICAL ERROR DURING PLUGIN STARTUP !!!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // 1. Terminate PacketEvents safely
        try {
            if (PacketEvents.getAPI() != null) {
                PacketEvents.getAPI().terminate();
            }
        } catch (Exception e) {
            getLogger().warning("[PacketEvents] Error during terminate: " + e.getMessage());
        }
        packetEventsLoaded = false;

        // 2. Disable all modules (saves data)
        if (this.moduleManager != null) {
            this.moduleManager.disableModules();
        }

        // 3. Cancel ALL scheduled tasks to prevent ghost runnables after PlugMan reload
        getServer().getScheduler().cancelTasks(this);

        // 4. Close Adventure platform
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }

        // 5. Clear static reference
        instance = null;
    }

    public void loadLanguage() {
        String lang = mainConfig.getString("settings.language", "vi");
        String fileName = "languages/" + lang + ".yml";
        File file = new File(getDataFolder(), fileName);
        
        if (!file.exists()) {
            saveResource(fileName, false);
        }
        
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        this.languageManager.setLangConfig(new PaperConfig(yaml));
    }

    public void reloadPlugin() {
        reloadConfig();
        this.mainConfig = new PaperConfig(getConfig());
        loadLanguage();
        rewardModule.reloadRewards();
        getLogger().info("Plugin has been reloaded.");
    }

    public static gnluckyblock getInstance() {
        return instance;
    }

    public BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    public Audience getAudience(Object obj) {
        if (obj instanceof Audience) return (Audience) obj;
        return adventure().sender((org.bukkit.command.CommandSender) obj);
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public com.gn027c.luckyblock.paper.block.BlockModule getBlockModule() {
        return blockModule;
    }

    public RewardModule getRewardModule() {
        return rewardModule;
    }
}

