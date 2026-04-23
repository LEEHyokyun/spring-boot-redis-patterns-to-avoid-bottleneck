package com.redis.bottleneck.common.bloomfilter.splitShardedBloomfilter;

import com.redis.bottleneck.common.bloomfilter.splitBloomfilter.SplitBloomFilterHandler;
import com.redis.bottleneck.utils.RedisTestContainerSupportUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@DataRedisTest
@Import(
        {
            SplitShardedBloomfilterHandler.class,
            SplitBloomFilterHandler.class,
        }
)
class SplitShardedBloomfilterHandlerTest extends RedisTestContainerSupportUtil {

    @Autowired
    private SplitShardedBloomfilterHandler splitShardedBloomfilterHandler;

    @Autowired
    private SplitBloomFilterHandler splitBloomFilterHandler;

    @Test
    void mightContainTest(){
        //given
        int shardCount = 3;
        int totalDataCount = 1000;
        SplitShardedBloomfilter splitShardedBloomfilter = SplitShardedBloomfilter.create(
                "testId",
                totalDataCount,
                0.01,
                shardCount
        );

        //when
        List<String> values = IntStream.range(0, totalDataCount).mapToObj(idx -> "value" + (idx + 1)).toList();
        for(String value : values){
            splitShardedBloomfilterHandler.add(splitShardedBloomfilter, value);
        }

        //then
        int count = 0;
        for(String value : values){
            boolean result = splitShardedBloomfilterHandler.mightContain(splitShardedBloomfilter, value);

            if(result) count++;
        }

        Assertions.assertEquals(1000, count);

    }

}