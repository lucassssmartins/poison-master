package br.com.poison.core.command.sender;

import net.md_5.bungee.api.chat.BaseComponent;

import java.util.UUID;

public interface CommandSender {

    default boolean equals(UUID id) {
        return getUuid().equals(id);
    }

    UUID getUuid();

    String getName();

    boolean isPlayer();

    void sendMessage(String message);

    void sendMessage(String... message);

    void sendMessage(BaseComponent message);

    void sendMessage(BaseComponent... message);
}
