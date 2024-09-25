package br.com.poison.core.backend.database.redis.channel;

import br.com.poison.core.backend.database.redis.RedisDatabase;
import br.com.poison.core.Core;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.logging.Level;

@Getter
public class RedisPubSub {

    private final RedisDatabase redis;

    private final JedisPubSub pubSub;

    private final String[] channels;

    public RedisPubSub(JedisPubSub pubSub, String... channels) {
        this.redis = Core.getRedisDatabase();

        this.pubSub = pubSub;

        this.channels = channels;
    }

    public void registerChannels() {
        try (Jedis jedis = redis.getPool().getResource()) {
            try {
                jedis.subscribe(pubSub, channels);
            } catch (Exception e) {
                Core.getLogger().log(Level.SEVERE, "Não foi possível registar os canais do Redis...", e);

                try {
                    pubSub.unsubscribe();
                } catch (Exception ignored) {
                }

                registerChannels();
            }
        }
    }
}
