package com.gn027c.luckyblock.core.config;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Map;

public class MessageService {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private String prefix = "";

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Component parse(String message, Map<String, String> placeholders) {
        String processedMessage = prefix + message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            processedMessage = processedMessage.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return miniMessage.deserialize(processedMessage);
    }

    public Component parse(String message) {
        return miniMessage.deserialize(prefix + message);
    }

    public void sendMessage(Audience audience, String message) {
        audience.sendMessage(parse(message));
    }

    public void sendMessage(Audience audience, String message, Map<String, String> placeholders) {
        audience.sendMessage(parse(message, placeholders));
    }
}

