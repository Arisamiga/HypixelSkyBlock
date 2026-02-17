package net.swofty.commons.punishment;

import com.google.gson.Gson;
import org.tinylog.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PunishmentRedis {
    private static final String PREFIX = "punish:";
    private static JedisPool jedisPool;
    private static volatile boolean initialized = false;
    private static volatile boolean connecting = false;

    public static void connect(String redisUri) {
        Thread.startVirtualThread(() -> connectSync(redisUri));
    }

    private static synchronized void connectSync(String redisUri) {
        if (initialized || connecting) return;
        connecting = true;

        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(20);
            poolConfig.setMaxIdle(5);
            poolConfig.setMinIdle(1);
            poolConfig.setMaxWait(Duration.ofSeconds(2));
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestWhileIdle(true);
            poolConfig.setBlockWhenExhausted(false);

            URI uri = URI.create(redisUri);
            jedisPool = new JedisPool(poolConfig, uri);

            // Test connection
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping();
            }

            initialized = true;
            Logger.info("PunishmentService: connected to Redis");
        } catch (Exception e) {
            Logger.warn("PunishmentService: Redis not available, punishments disabled");
            initialized = false;
            jedisPool = null;
        } finally {
            connecting = false;
        }
    }

    public static boolean isInitialized() {
        return initialized && jedisPool != null && !jedisPool.isClosed();
    }

    public static void saveActivePunishment(UUID playerId, String type, String id, PunishmentReason reason, long expiresAt) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = PREFIX + "active:" + playerId;

            Gson gson = new Gson();
            Map<String, String> data = Map.of(
                    "type", type,
                    "banId", id,
                    "reason", gson.toJson(reason), // most likely not optimal for performance
                    "expiresAt", String.valueOf(expiresAt)
            );

            jedis.hset(key, data);
            if (expiresAt > 0) {
                long ttlSeconds = (expiresAt - System.currentTimeMillis()) / 1000;
                if (ttlSeconds > 0) {
                    jedis.expire(key, (int) ttlSeconds);
                }
            }
        }
    }

    public static Optional<ActivePunishment> getActive(UUID playerId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = PREFIX + "active:" + playerId;
            Map<String, String> data = jedis.hgetAll(key);

            if (data.isEmpty()) return Optional.empty();

            String type = data.get("type");
            String banId = data.get("banId");
            long expiresAt = Long.parseLong(data.getOrDefault("expiresAt", "-1"));

            if (expiresAt > 0 && System.currentTimeMillis() > expiresAt) {
                jedis.del(key); // clean up expired
                return Optional.empty();
            }

            Gson gson = new Gson();
            PunishmentReason reason = gson.fromJson(data.get("reason"), PunishmentReason.class); // most likely not optimal for performance

            return Optional.of(new ActivePunishment(type, banId, reason, expiresAt));
        }
    }

    public static CompletableFuture<Long> revoke(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                String key = PREFIX + "active:" + playerId;
                return jedis.del(key);
            }
        });
    }

    public static Set<String> getAllBannedPlayerIds() {
        try (Jedis jedis = jedisPool.getResource()) {
            var cursor = "0";
            Set<String> result = new java.util.HashSet<>();
            var params = new redis.clients.jedis.params.ScanParams().match(PREFIX + "active:*").count(100);
            do {
                var scanResult = jedis.scan(cursor, params);
                for (String key : scanResult.getResult()) {
                    result.add(key.substring((PREFIX + "active:").length()));
                }
                cursor = scanResult.getCursor();
            } while (!"0".equals(cursor));
            return result;
        }
    }

}
