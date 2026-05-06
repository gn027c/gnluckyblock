package com.gn027c.luckyblock.paper.util;

import com.gn027c.luckyblock.paper.gnluckyblock;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Logger;

/**
 * Centralized logger that respects the logging flags defined in config.yml.
 *
 * Flags (under the 'logging' key):
 *   - config     : logs when config/rewards are loaded or reloaded (default: false)
 *   - commands   : logs when a player uses a /gnlb command (default: true)
 *   - block-break: logs when a player places or breaks a Lucky Block (default: false)
 *   - outcomes   : logs the reward outcome when a Lucky Block is triggered (default: false)
 */
public class PluginLogger {

    public enum Flag {
        CONFIG("logging.config", false),
        COMMANDS("logging.commands", true),
        BLOCK_BREAK("logging.block-break", false),
        OUTCOMES("logging.outcomes", false);

        private final String configPath;
        private final boolean defaultValue;

        Flag(String configPath, boolean defaultValue) {
            this.configPath = configPath;
            this.defaultValue = defaultValue;
        }
    }

    // ─────────────────────────────────────────────────────────────

    /** Log an INFO message if the given flag is enabled in config. */
    public static void log(Flag flag, String message) {
        if (!isEnabled(flag)) return;
        getLogger().info(formatTag(flag) + " " + message);
    }

    /** Log a WARNING message if the given flag is enabled in config. */
    public static void warn(Flag flag, String message) {
        if (!isEnabled(flag)) return;
        getLogger().warning(formatTag(flag) + " " + message);
    }

    // ─────────────────────────────────────────────────────────────

    private static boolean isEnabled(Flag flag) {
        gnluckyblock plugin = gnluckyblock.getInstance();
        if (plugin == null) return false;
        FileConfiguration config = plugin.getConfig();
        return config.getBoolean(flag.configPath, flag.defaultValue);
    }

    private static Logger getLogger() {
        gnluckyblock plugin = gnluckyblock.getInstance();
        if (plugin == null) return Logger.getLogger("gnluckyblock");
        return plugin.getLogger();
    }

    private static String formatTag(Flag flag) {
        return switch (flag) {
            case CONFIG      -> "[LOG:CONFIG]";
            case COMMANDS    -> "[LOG:CMD]";
            case BLOCK_BREAK -> "[LOG:BLOCK]";
            case OUTCOMES    -> "[LOG:OUTCOME]";
        };
    }
}
