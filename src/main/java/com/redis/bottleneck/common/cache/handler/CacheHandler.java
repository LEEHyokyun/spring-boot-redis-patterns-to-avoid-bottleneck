package com.redis.bottleneck.common.cache.handler;

import com.redis.bottleneck.common.cache.strategy.CacheStrategy;

import java.time.Duration;
import java.util.function.Supplier;

public interface CacheHandler {
    <T> T fetch(String key, Duration ttl, Supplier<T> supplier, Class<T> clazz);
    void put(String key, Duration ttl, Object value);
    void evict(String key);
    boolean supports(CacheStrategy cacheStrategy);
}
