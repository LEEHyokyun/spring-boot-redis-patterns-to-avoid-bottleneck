package com.redis.bottleneck.common.detailCaches.jitter;

import com.redis.bottleneck.common.cache.handler.CacheHandler;
import com.redis.bottleneck.common.strategy.CacheStrategy;
import com.redis.bottleneck.common.serde.DataSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

@Component
@RequiredArgsConstructor
@Slf4j
public class JitterHandler implements CacheHandler {

    private final StringRedisTemplate stringRedisTemplate;
    private static final int JITTER_RANGE_SECONDS = 3; //변동성 3sec 이내

    @Override
    public <T> T fetch(String key, Duration ttl, Supplier<T> supplier, Class<T> clazz) {
        String cached = stringRedisTemplate.opsForValue().get(key);

        if(cached == null){
            return this.refreshKey(key, ttl, supplier);
        }

        T data = DataSerializer.deserializeOrNull(cached, clazz);

        if(data == null){
            return this.refreshKey(key, ttl, supplier);
        }

        return data;
    }

    private <T> T refreshKey(String key, Duration ttl, Supplier<T> supplier) {
        //supplier를 통해 datasource로 부터 데이터를 추출
        T refreshed = supplier.get();
        this.put(key, ttl, refreshed);

        return refreshed;
    }

    @Override
    public void put(String key, Duration ttl, Object value) {
        stringRedisTemplate.opsForValue().set(key, DataSerializer.serializeOrException(value), this.jitteredTTl(ttl));
    }

    private Duration jitteredTTl(Duration ttl) {
        /*
        * JITTER = 3 sec이므로, 이를 적용하기 위해서는 최소한 3초 이상의 TTL이 필요
        * */
        if (ttl.getSeconds() <= JITTER_RANGE_SECONDS){
            throw new IllegalArgumentException("Jittered TTL Should be more than "+ JITTER_RANGE_SECONDS);
        }

        /*
        * -3 ~ +3 sec의 jitter를 ttl에 적용한 jittered TTl 추출.
        * */
        int jitter = RandomGenerator.getDefault().nextInt(-JITTER_RANGE_SECONDS, +JITTER_RANGE_SECONDS + 1);

        log.info("jitter value : " + jitter);
        log.info("finally jittered ttl value : " + ttl.plusSeconds(jitter));

        return ttl.plusSeconds(jitter);
    }

    @Override
    public void evict(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public boolean supports(CacheStrategy cacheStrategy) {
        return CacheStrategy.Jitter == cacheStrategy;
    }

}
