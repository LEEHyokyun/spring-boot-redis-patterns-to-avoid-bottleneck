package com.redis.bottleneck.common.bloomfilter.splitShardedBloomfilter;

import com.google.common.hash.Hashing;
import com.redis.bottleneck.common.bloomfilter.splitBloomfilter.SplitBloomfilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SplitShardedBloomfilterTest {

    @Test
    void createShardedSplitShardedBloomfilterAndCheckDataChunkCountTest(){
        //given
        int realShardCount = 3;
        SplitShardedBloomfilter splitShardedBloomfilter = SplitShardedBloomfilter.create(
                "testId", 1000, 0.01, realShardCount
        );

        long expectedTotalDataChunkCount = 1000L;
        long realDataCount = 0L;
        List<SplitBloomfilter> shards = splitShardedBloomfilter.getShards();

        //when
        for(int shardCount = 0 ; shardCount < realShardCount ; shardCount++){
            long count = shards.get(shardCount).getBloomFilter().getDataCount();
            if(shardCount == 0){
                Assertions.assertEquals(334L, count);
            }else{
                Assertions.assertEquals(333L, count);
            }

            realDataCount += count;
        }

        //then
        Assertions.assertEquals(expectedTotalDataChunkCount, realDataCount);
    }

    @Test
    void findShardTest(){
        //given
        int realShardCount = 3;
        SplitShardedBloomfilter splitShardedBloomfilter = SplitShardedBloomfilter.create(
                "testId", 1000, 0.01, realShardCount
        );

        //when
        SplitBloomfilter readShard = splitShardedBloomfilter.findShard("value");

        //when
        int expectedShardIndex = this.getHashingFunc("value", realShardCount);
        SplitBloomfilter expectedShard = splitShardedBloomfilter.getShards().get(expectedShardIndex);

        //then
        Assertions.assertNotNull(readShard);
        Assertions.assertEquals(expectedShard, readShard);
    }

    private int getHashingFunc(String value, int shardCount){
        return Math.abs(Hashing.murmur3_32_fixed()
                .hashString(value, StandardCharsets.UTF_8)
                .asInt() % shardCount
                )
                ;
    }
}