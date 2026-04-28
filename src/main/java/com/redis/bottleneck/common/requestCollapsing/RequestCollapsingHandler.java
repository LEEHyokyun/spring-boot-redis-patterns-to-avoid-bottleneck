package com.redis.bottleneck.common.requestCollapsing;

import com.redis.bottleneck.common.cache.handler.CacheHandler;
import com.redis.bottleneck.common.distributedLock.DistributedLockProvider;
import com.redis.bottleneck.common.serde.DataSerializer;
import com.redis.bottleneck.common.strategy.CacheStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestCollapsingHandler implements CacheHandler {

    private final DistributedLockProvider distributedLockProvider;
    private final StringRedisTemplate stringRedisTemplate;

    /*
     * lock 획득을 위한 POLLING 주기
     * */
    private static final long POLLING_INTERVAL_MILLIS = 150; //50

    /*
     * lock 미획득 시 대기, 최대 대기 시간을 지정
     * */
    private static final long WAITING_TIME_MILLIS = 2000; //2000

    @Override
    public <T> T fetch(String key, Duration ttl, Supplier<T> supplier, Class<T> clazz) {

        //캐싱
        String cached = stringRedisTemplate.opsForValue().get(key);

        if(cached != null){
            return DataSerializer.deserializeOrNull(cached, clazz);
        }

        String lockKey = this.genLockKey(key);

        //없으면 분산락 획득 성공한 최초 요청에 대해서만 캐시 갱신
        if(distributedLockProvider.lock(lockKey, Duration.ofSeconds(10))){ //10
            log.info("[RequestCollapsingHandler.fetch] locked : {}", lockKey);
            try {
                //획득
                return this.refresh(key, ttl, supplier);
            } finally {
                distributedLockProvider.unlock(lockKey);
            }
        }

        /*
        * 분산락을 획득하지 못하였을 경우 반복적으로 polling 하여 분산 락 획득을 대기.
        * 선행 요청이 락 갱신을 실패할 수도 있기에, 모든 요청에 POLLING 및 대해 갱신 여부를 판단.
        * */
        long startTime = System.nanoTime();
        while(System.nanoTime() - startTime < TimeUnit.MILLISECONDS.toNanos(WAITING_TIME_MILLIS)){
            cached = stringRedisTemplate.opsForValue().get(key);

            if(cached != null){
                return DataSerializer.deserializeOrNull(cached, clazz);
            }

            //무한반복이 아닌, POLLING INTERVAL을 주기로 반복
            try{
                //후행 요청 polling(무작정 캐싱요청이 아닌 기다림)
                TimeUnit.MILLISECONDS.sleep(POLLING_INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                log.error("[RequestCollapsingHandler][ERROR] Interrupted while waiting for key [{}]", key);
                break;
            }
        }
        return this.refresh(key, ttl, supplier);
    }

    private String genLockKey(String key){
        /*
        * distributed-lock:REQUEST_COLLAPSING:{key}
        * */
        return CacheStrategy.REQUEST_COLLAPSING + ":" + key;
    }

    private <T> T refresh(String key, Duration ttl, Supplier<T> supplier) {
        //원본 추출
        T data = supplier.get();
        //캐시 갱신
        this.put(key, ttl, data);

        return data;
    }

    @Override
    public void put(String key, Duration ttl, Object value) {
        stringRedisTemplate.opsForValue().set(key, DataSerializer.serializeOrException(value), ttl);
    }

    @Override
    public void evict(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public boolean supports(CacheStrategy cacheStrategy) {
        return CacheStrategy.REQUEST_COLLAPSING == cacheStrategy;
    }
}
