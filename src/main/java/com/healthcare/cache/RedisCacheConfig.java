package com.healthcare.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis-backed caching for frequently-read, rarely-written data (patient
 * demographic lookups, etc). A short 10-minute TTL is used so that even if a
 * cache eviction is somehow missed, stale PHI doesn't linger indefinitely.
 *
 * Gated behind app.cache.redis-enabled (default true) so this bean is skipped
 * entirely when no Redis server is available (e.g. local dev without Redis
 * installed, or the test profile) — Spring then falls back to the simple
 * in-memory cache manager configured via spring.cache.type=simple.
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.cache", name = "redis-enabled", havingValue = "true", matchIfMissing = true)
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(new ObjectMapper())));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
