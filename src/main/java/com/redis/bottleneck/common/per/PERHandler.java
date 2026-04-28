package com.redis.bottleneck.common.per;

import com.redis.bottleneck.common.cache.handler.CacheHandler;
import com.redis.bottleneck.common.strategy.CacheStrategy;
import com.redis.bottleneck.common.serde.DataSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class PERHandler implements CacheHandler {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public <T> T fetch(String key, Duration ttl, Supplier<T> supplier, Class<T> clazz) {
        String cached = stringRedisTemplate.opsForValue().get(key);

        if(cached == null){
            //cache 갱신
            return this.refreshKey(key, ttl, supplier);
        }

        //1차 역직렬화 : PERModel
        PERModel data = DataSerializer.deserializeOrNull(cached, PERModel.class);

        if(data == null){
            //data 없으면 원본에서 가져오기
            return this.refreshKey(key ,ttl, supplier);
        }

        //그 요청이 per 대상인지 확인하고, 갱신 대상이면 바로 갱신.
        if(data.isInChargeOfRecomputation(1)){
            return this.refreshKey(key, ttl, supplier);
        }

        //2차 역직렬화 : 최종 원본 데이터
        T finallyDeserializedData = data.deserializeModelData(clazz);
        if(finallyDeserializedData == null){
            return this.refreshKey(key ,ttl, supplier);
        }

        return finallyDeserializedData;
    }

    private <T> T refreshKey(String key, Duration ttl, Supplier<T> supplier) {
        //supplier = data source로부터 원본 추출
        long startMillis = Instant.now().toEpochMilli();
        long computationMillis = Instant.now().toEpochMilli() - startMillis;

        //원본 data 반환 및 캐시 갱신
        T originalData = supplier.get();
        this.put(key, ttl, originalData, computationMillis);

        return originalData;
    }

    private void put(String key, Duration ttl, Object data, long computationMillis) {
        PERModel perModel = PERModel.of(
                data, computationMillis, ttl
        );

        //캐시 대상은 PERModel.
        stringRedisTemplate.opsForValue().set(key, DataSerializer.serializeOrException(perModel), ttl);
    }

    @Override
    public void put(String key, Duration ttl, Object value) {
        this.put(key, ttl, value, 100);
    }

    @Override
    public void evict(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public boolean supports(CacheStrategy cacheStrategy) {
        return CacheStrategy.PER == cacheStrategy;
    }
}
