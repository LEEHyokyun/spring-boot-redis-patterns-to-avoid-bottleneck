package com.redis.bottleneck.service.strategy.bloomfilter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BloomFilterHandler {
    private final StringRedisTemplate stringRedisTemplate;

    /*
    * hashed indices update on redis bitmap
    * */
    public void add(BloomFilter bloomFilter, String value) {
        stringRedisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection con = (StringRedisConnection) action;

            String key = this.genKey(bloomFilter);
            List<Long> hashedIndices = bloomFilter.hash(value);
            for(Long hashedIndex : hashedIndices) {
                con.setBit(key, hashedIndex, true);
            }
            return null;
        });
    }

    /*
    * false Positive
    */
    public boolean mightContain(BloomFilter bloomFilter, String value){
        return stringRedisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection con = (StringRedisConnection) action;

            String key = this.genKey(bloomFilter);
            List<Long> hashedIndices = bloomFilter.hash(value);
            for(Long hashedIndex : hashedIndices) {
                con.setBit(key, hashedIndex, true);
            }

            return null;
        }).stream()  //hashed indices를 look up해서, bloom filter에 값이 존재한다면 false positive(caching / db 확인)
                .map(Boolean.class::cast)
                .allMatch(Boolean.TRUE::equals); //모두 있을때 false positive
    }

    /*
    * redis bitmap을 저장한 key 값을 삭제
    * */
    public void delete(BloomFilter bloomFilter){
        stringRedisTemplate.delete(this.genKey(bloomFilter));
    }

    private String genKey(BloomFilter bloomFilter) {
        return this.genKey(bloomFilter.getId());
    }

    private String genKey(String id){
        return "bloom-filter:%s".formatted(id);
    }
}
