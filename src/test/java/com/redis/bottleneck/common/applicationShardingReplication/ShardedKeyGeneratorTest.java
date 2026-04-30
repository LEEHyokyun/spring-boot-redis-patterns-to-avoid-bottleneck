package com.redis.bottleneck.common.applicationShardingReplication;

import com.redis.bottleneck.common.detailCaches.applicationShardingReplication.ShardedKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ShardedKeyGeneratorTest {

    ShardedKeyGenerator shardedKeyGenerator = new ShardedKeyGenerator();

    @Test
    void shardedKeyListTest(){
        //given
        String key = "testKey";
        int shardCount = 3;

        //when
        List<String> shardedKeys = shardedKeyGenerator.generateShardedKeys(key, shardCount);

        //then
        Assertions.assertEquals(shardCount, shardedKeys.size());
        for(int i = 0 ; i < shardedKeys.size() ; i++){
            Assertions.assertEquals(key + ":" + i, shardedKeys.get(i));
        }
        log.info("CHECK SHARD KEY LIST  {} ", shardedKeys);
    }

    @Test
    void randomGenerateShardedKeyTest(){
        //given
        String key = "testKey";
        int shardCount = 3;

        //when
        List<String> shardedKeys = shardedKeyGenerator.generateShardedKeys(key, shardCount);

        //then
        for(int i = 0 ; i < 10 ; i++){
            String shardedKey = shardedKeyGenerator.findRandomShardedKey(key, shardCount);
            assertThat(shardedKey).isIn(shardedKeys);
            log.info("CHECK GENERATED SHARD KEY : {} ", shardedKey);
        }
    }
}