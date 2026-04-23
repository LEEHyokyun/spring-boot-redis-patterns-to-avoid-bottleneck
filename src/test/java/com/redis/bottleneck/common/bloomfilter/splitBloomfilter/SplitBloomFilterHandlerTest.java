package com.redis.bottleneck.common.bloomfilter.splitBloomfilter;

import com.redis.bottleneck.utils.RedisTestContainerSupportUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@DataRedisTest
@Import(SplitBloomFilterHandler.class)
class SplitBloomFilterHandlerTest extends RedisTestContainerSupportUtil {

    @Autowired
    private SplitBloomFilterHandler splitBloomfilterHandler;

    @Test
    void mightContainTest(){
        //given
        SplitBloomfilter splitBloomfilter = SplitBloomfilter.create("testId", 1000, 0.01);
        List<String> values = IntStream.range(0, 1000).mapToObj(idx -> "value" + (idx+1)).toList();

        //when
        for(int idx = 0; idx < values.size(); idx++){
            splitBloomfilterHandler.add(splitBloomfilter, values.get(idx));
        }

        //then
        int count = 0;
        for(String value : values){
            boolean result = splitBloomfilterHandler.mightContain(splitBloomfilter, value);

            if(result) count++;
        }

        Assertions.assertEquals(1000, count);
    }
}