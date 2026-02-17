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

    public static void saveActivePunishment(UUID playerId, String type, String id, PunishmentReason reason, long expiresAt, java.util.List<PunishmentTag> tags) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = PREFIX + "active:" + playerId;

            Gson gson = new Gson();
            java.util.HashMap<String, String> data = new java.util.HashMap<>(Map.of(
                    "type", type,
                    "banId", id,
                    "reason", gson.toJson(reason),
                    "expiresAt", String.valueOf(expiresAt)
            ));
            if (tags != null && !tags.isEmpty()) {
                data.put("tags", gson.toJson(tags));
            }

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
            PunishmentReason reason = gson.fromJson(data.get("reason"), PunishmentReason.class);

            java.util.List<PunishmentTag> tags = java.util.List.of();
            String tagsJson = data.get("tags");
            if (tagsJson != null && !tagsJson.isBlank()) {
                tags = java.util.List.of(gson.fromJson(tagsJson, PunishmentTag[].class));
            }

            return Optional.of(new ActivePunishment(type, banId, reason, expiresAt, tags));
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
}
