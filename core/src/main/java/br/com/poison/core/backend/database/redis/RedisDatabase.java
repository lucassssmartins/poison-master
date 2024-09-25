package br.com.poison.core.backend.database.redis;

import br.com.poison.core.util.Util;
import br.com.poison.core.util.json.JsonUtil;
import br.com.poison.core.Core;
import br.com.poison.core.backend.database.Database;
import br.com.poison.core.backend.database.DatabaseCredentials;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

@Getter
@RequiredArgsConstructor
public class RedisDatabase implements Database {

    private final DatabaseCredentials credential;

    private JedisPool pool;

    public RedisDatabase(boolean local) {
        this(local
                // Local
                ? DatabaseCredentials.builder()
                .host("127.0.0.1")
                .port(6379)
                .build()
                // Server
                : DatabaseCredentials.builder().build());
    }

    @Override
    public void init() {
        long start = System.currentTimeMillis();

        Core.getLogger().info("Conectando ao Redis...");

        try {
            JedisPoolConfig config = new JedisPoolConfig();

            config.setMaxTotal(128);

            if (credential.getPassword() == null || !credential.getPassword().isEmpty()) {
                pool = new JedisPool(config, credential.getHost(), credential.getPort(), 0, credential.getPassword());
            } else {
                pool = new JedisPool(config, credential.getHost(), credential.getPort(), 0);
            }

            Core.getLogger().info("Conexão com o Redis efetuada com sucesso. (Tempo: " + Util.formatMS(start) + "ms)");
        } catch (Exception e) {
            Core.getLogger().log(Level.SEVERE, "Não foi possível conectar ao banco de dados Redis...", e);
        }
    }

    @Override
    public void end() {
        if (hasConnection()) {
            pool.destroy();
        }
    }

    @Override
    public boolean hasConnection() {
        return pool != null && !pool.isClosed();
    }

    public boolean exists(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.exists(key);
        }
    }

    public void flush() {
        try (Jedis jedis = pool.getResource()) {
            jedis.flushAll();
        }
    }

    public void save(String key, Object object) {
        if (exists(key)) return;

        Map<String, String> fields = JsonUtil.objectToMap(object);

        for (String value : fields.values()) {
            if (value == null) continue;

            try (Jedis jedis = pool.getResource()) {
                jedis.hmset(key, fields);
            }

        }
    }

    public void update(String key, Object object) {
        if (exists(key)) {

            Map<String, String> fields = JsonUtil.objectToMap(object);

            try (Jedis jedis = pool.getResource()) {
                jedis.hmset(key, fields);
            }

        }
    }

    public void publish(String channel, String message) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, message);
        }
    }

    public void save(String key, Object object, int expire) {
        save(key, object);

        try (Jedis jedis = pool.getResource()) {
            jedis.expire(key, expire);
        }
    }

    public void delete(String key) {
        try (Jedis jedis = pool.getResource()) {
            jedis.del(key);
        }
    }

    public void cache(String key, int seconds) {
        try (Jedis jedis = pool.getResource()) {
            jedis.expire(key, seconds);
        }
    }

    public void persist(String key) {
        try (Jedis jedis = pool.getResource()) {
            jedis.persist(key);
        }
    }

    public void delete(String key, String... fields) {
        try (Jedis jedis = pool.getResource()) {
            jedis.hdel(key, fields);
        }
    }

    public <T> T load(String key, Class<T> tClass) {
        if (!exists(key)) return null;

        T t;

        try (Jedis jedis = pool.getResource()) {
            Map<String, String> fields = jedis.hgetAll(key);

            if (fields == null || fields.isEmpty()) return null;

            t = JsonUtil.mapToObject(fields, tClass);
        }

        return t;
    }

    public <T> List<T> loadAll(String key, Class<T> type) {
        List<T> list = new ArrayList<>();

        try (Jedis jedis = pool.getResource()) {
            Set<String> keys = jedis.keys(key + "*");

            if (keys == null || keys.isEmpty()) return list;

            for (String keyName : keys) {
                Map<String, String> fields = jedis.hgetAll(keyName);

                T object = JsonUtil.mapToObject(fields, type);

                if (object != null)
                    list.add(type.cast(object));
            }
        }

        return list;
    }

    public void removeAll(String key) {
        try (Jedis jedis = pool.getResource()) {
            Set<String> keys = jedis.keys(key + "*");

            for (String keyName : keys)
                jedis.del(keyName);
        }
    }

    public int getTotalCount(String key) {
        int count;

        try (Jedis jedis = pool.getResource()) {
            Set<String> members = jedis.smembers(key);

            count = members.size();

            return count;
        }
    }
}
