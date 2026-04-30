package com.redis.bottleneck.common.detailCaches.applicationShardingReplication;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class ShardedKeyGenerator {

    /*
    * 기존의 전략인 hash(key) % shardCount를 적용하여 발생하는 쏠림 현상을 방지하고,
    * shard count만큼 분산하여 복제 저장하는 것이 핵심.
    * */
    public List<String> generateShardedKeys(String key, int shardCount) {
        return IntStream.range(0, shardCount)
                .mapToObj(shardIndex -> this.genShardKey(key, shardIndex))
                .toList()
                ;
    }

    /*
    * 찾을때 샤드 카운트 내에서 임의의 한 샤드를 골라 탐색, 조회 부하를 shard 개수만큼 분산한다.
    * */
    public String findRandomShardedKey(String key, int shardCount) {
        return this.genShardKey(key, RandomGenerator.getDefault().nextInt(shardCount));
    }

    private String genShardKey(String key, int shardIndex) {
        return key + ":" + shardIndex;
    }
}
