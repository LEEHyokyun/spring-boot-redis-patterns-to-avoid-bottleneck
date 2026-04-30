package com.redis.bottleneck.common.detailCaches.applicationShardingReplication;

import com.redis.bottleneck.common.cache.handler.CacheHandler;
import com.redis.bottleneck.common.serde.DataSerializer;
import com.redis.bottleneck.common.strategy.CacheStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationShardingReplicationHandler implements CacheHandler {

    private final StringRedisTemplate stringRedisTemplate;
    private final ShardedKeyGenerator shardedKeyGenerator;

    //shard count = 3개
    private static final int APPLICATION_SHARD_COUNT = 3;

    @Override
    public <T> T fetch(String key, Duration ttl, Supplier<T> supplier, Class<T> clazz) {

        String shardKey = shardedKeyGenerator.findRandomShardedKey(key, APPLICATION_SHARD_COUNT);
        String cached = stringRedisTemplate.opsForValue().get(shardKey);

        if(cached == null){
            return this.refresh(key, ttl, supplier);
        }

        T data = DataSerializer.deserializeOrNull(cached, clazz);

        if(data == null){
            return this.refresh(key, ttl, supplier);
        }

        return data;
    }

    private <T> T refresh(String key, Duration ttl, Supplier<T> supplier) {
        T originalData = supplier.get();

        log.info("[ApplicationShardingReplicationHandler] fetch original data : {}", originalData);

        this.put(key, ttl, originalData);

        return originalData;
    }

    @Override
    public void put(String key, Duration ttl, Object value) {
        List<String> shardKeys = shardedKeyGenerator.generateShardedKeys(key, APPLICATION_SHARD_COUNT);

        for(String shardKey : shardKeys){
            log.info("[ApplicationShardingReplicationHandler.put][INFO] shardKey = {} ", shardKey);
            stringRedisTemplate.opsForValue().set(shardKey, DataSerializer.serializeOrException(value), ttl);
        }
    }

    @Override
    public void evict(String key) {
        List<String> shardKeys = shardedKeyGenerator.generateShardedKeys(key, APPLICATION_SHARD_COUNT);

        for(String shardKey : shardKeys){
            log.info("[ApplicationShardingReplicationHandler.evict][INFO] shardKey = {} ", shardKey);
            stringRedisTemplate.delete(shardKey);
        }
    }

    @Override
    public boolean supports(CacheStrategy cacheStrategy) {
        /*
        * APPLICATION_HOT_KEY_SHARDING_AND_REPLICATION:article:{#articleId}:{#shardIndex}
        * */
        return CacheStrategy.APPLICATION_HOT_KEY_SHARDING_AND_REPLICATION == cacheStrategy;
    }
}
