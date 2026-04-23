package com.redis.bottleneck.common.bloomfilter.splitBloomfilter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Component
@RequiredArgsConstructor
public class SplitBloomFilterHandler {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<Object, Object> redisTemplate;

    public void init(SplitBloomfilter splitBloomfilter) {

        //각 splited 된 영역에 대해 key를 생성하며,
        //최초 split bloom filter의 크기는 8MB.

        for(long splitIndex = 0L ; splitIndex < splitBloomfilter.getSplitCount(); splitIndex++){
            String key = this.genKey(splitBloomfilter, splitIndex); //split bloom filter(split index)에 매핑되는 key값.
            long bitSize = splitBloomfilter.findBitSizeOfSplitIndex(splitIndex);

            //전체 bloom filter 중 일부를 먼저 할당받겠다는 의도
            for(long offset = 0L ; offset < bitSize ; offset += 8L * 1024 * 1024 * 8) {
                redisTemplate.opsForValue().setBit(key, offset, false);
            }
        }
    }

    public void add(SplitBloomfilter splitBloomfilter, String value) {
        stringRedisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection con = (StringRedisConnection) action;

            List<Long> hashedIndices = splitBloomfilter.getBloomFilter().hash(value);

            for(Long hashedIndex : hashedIndices) {
                long splitIndex = splitBloomfilter.findSplitIndex(hashedIndex);
                con.setBit(
                        this.genKey(splitBloomfilter, splitIndex),
                        hashedIndex % splitBloomfilter.BIT_SPLIT_SIZE,
                        true);
            }

            return null;
        });
    }

    public boolean mightContain(SplitBloomfilter splitBloomfilter, String value) {
        return stringRedisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection con = (StringRedisConnection) action;

            List<Long> hashedIndices = splitBloomfilter.getBloomFilter().hash(value);

            for(Long hashedIndex : hashedIndices) {
                long splitIndex = splitBloomfilter.findSplitIndex(hashedIndex);
                con.getBit(
                        this.genKey(splitBloomfilter, splitIndex),
                        hashedIndex % SplitBloomfilter.BIT_SPLIT_SIZE
                );
            }

            return null;
        })
                .stream()
                .map(Boolean.class::cast)
                .allMatch(Boolean.TRUE::equals)
        ;
    }

    public void delete(SplitBloomfilter splitBloomfilter){
        stringRedisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection con = (StringRedisConnection) action;
            this.genKeys(splitBloomfilter).forEach(con::del);

            return null;
        });
    }

    //split index에 대응되는 key 목록 생성
    private List<String> genKeys(SplitBloomfilter splitBloomfilter) {
        return LongStream.range(0L, splitBloomfilter.getSplitCount())
                .mapToObj(splitIndex -> this.genKey(splitBloomfilter, splitIndex))
                .toList()
                ;
    }

    //split index에 대응되는 key 생성
    private String genKey(SplitBloomfilter splitBloomfilter, long splitIndex){
        /*
        * split-bloom-filter:bloom-filter-user1
        * */
        return "split-bloom-filter:%s".formatted(splitBloomfilter.getId(), splitIndex);
    }
}
