package br.com.poison.core.backend.database.redis.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class RedisMessage {

    private final MessageType type;

    private final String channel;

    public abstract void publish();

    public enum MessageType {
        BUKKIT, BUNGEE
    }
}
