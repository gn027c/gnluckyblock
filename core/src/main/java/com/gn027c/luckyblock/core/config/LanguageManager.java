package com.gn027c.luckyblock.core.config;

import com.gn027c.luckyblock.api.IConfiguration;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private final MessageService messageService;
    private IConfiguration langConfig;

    public LanguageManager(MessageService messageService) {
        this.messageService = messageService;
    }

    public void setLangConfig(IConfiguration langConfig) {
        this.langConfig = langConfig;
        this.messageService.setPrefix(langConfig.getString("prefix", ""));
    }

    public String getRawMessage(String path) {
        return langConfig.getString(path, "<red>Missing message: " + path + "</red>");
    }

    public void sendMessage(net.kyori.adventure.audience.Audience audience, String path) {
        messageService.sendMessage(audience, getRawMessage(path));
    }

    public void sendMessage(net.kyori.adventure.audience.Audience audience, String path, Map<String, String> placeholders) {
        messageService.sendMessage(audience, getRawMessage(path), placeholders);
    }

    public void sendMessageWithPlaceholders(net.kyori.adventure.audience.Audience audience, String path, String... placeholders) {
        if (placeholders.length % 2 != 0) {
            sendMessage(audience, path);
            return;
        }
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < placeholders.length; i += 2) {
            map.put(placeholders[i], placeholders[i + 1]);
        }
        sendMessage(audience, path, map);
    }
}
