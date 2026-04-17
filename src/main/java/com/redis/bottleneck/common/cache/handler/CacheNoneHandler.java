package com.redis.bottleneck.common.cache.handler;

import com.redis.bottleneck.common.cache.aop.CacheStrategy;
import com.redis.bottleneck.common.cache.aop.Cacheable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheNoneHandler implements CacheHandler {
    @Override
    public <T> T fetch(String key, Duration ttl, Supplier<T> supplier, Class<T> clazz) {
        log.info("[CacheNoneHandler.fetch INFO] key  {} ", key);
        return supplier.get();
    }

    @Override
    public void put(String key, Duration ttl, Object value) {
        log.info("[CacheNoneHandler.put INFO] key  {} ", key);
    }

    @Override
    public void evict(String key) {
        log.info("[CacheNoneHandler.evict INFO] key  {} ", key);
    }

    @Override
    public boolean supports(CacheStrategy cacheStrategy) {
        return cacheStrategy == CacheStrategy.NONE;
    }
}
