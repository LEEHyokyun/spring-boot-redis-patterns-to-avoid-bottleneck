package com.redis.bottleneck.common.bloomfilter.splitShardedSubBloomfilter;

import com.redis.bottleneck.common.bloomfilter.splitBloomfilter.SplitBloomFilterHandler;
import com.redis.bottleneck.common.bloomfilter.splitShardedBloomfilter.SplitShardedBloomfilter;
import com.redis.bottleneck.common.bloomfilter.splitShardedBloomfilter.SplitShardedBloomfilterHandler;
import com.redis.bottleneck.common.bloomfilter.splitShardedSubBloomfilter.distributedLock.DistributedLockProvider;
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
@Import(
        {
            SplitShardedSubBloomfilterHandler.class,
            SplitShardedBloomfilterHandler.class,
            SplitBloomFilterHandler.class,
            DistributedLockProvider.class,
        }
)
class SplitShardedSubBloomfilterHandlerTest extends RedisTestContainerSupportUtil {

    @Autowired
    private SplitShardedSubBloomfilterHandler splitShardedSubBloomfilterHandler;

    @Autowired
    private SplitShardedBloomfilterHandler splitShardedBloomfilterHandler;

    @Autowired
    private SplitBloomFilterHandler splitBloomFilterHandler;

    @Autowired
    private DistributedLockProvider distributedLockProvider;

    @Test
    void filterAddTest(){
        //given
        SplitShardedSubBloomfilter splitShardedSubBloomfilter = SplitShardedSubBloomfilter.create(
                "testId",
                1000,
                0.01,
                4
        );

        //when
        //원본에 1개 생성
        splitShardedSubBloomfilterHandler.add(splitShardedSubBloomfilter, "value");

        //then
        //sub filter 0개, 원본 필터의 반영 데이터 개수는 1개
        Assertions.assertEquals(0, getSubFilterCount(splitShardedSubBloomfilter));
        Assertions.assertEquals(1, getDataCountOfFilter(splitShardedSubBloomfilter.findActivatedFilter(0)));
    }

    @Test
    void addDataAtSubFilterWhenFilterIsFull(){
        //given
        SplitShardedSubBloomfilter splitShardedSubBloomfilter = SplitShardedSubBloomfilter.create(
                "testId",
                1000,
                0.01,
                4
        );

        //when
        int count = 1000 - 1;
        for(int i = 0; i < count; i++){
            splitShardedSubBloomfilterHandler.add(splitShardedSubBloomfilter, "value" + (i + 1));
        }

        //then
        //아직 원본
        Assertions.assertEquals(0, getSubFilterCount(splitShardedSubBloomfilter));
        Assertions.assertEquals(999, getDataCountOfFilter(splitShardedSubBloomfilter.findActivatedFilter(0)));

        //when
        splitShardedSubBloomfilterHandler.add(splitShardedSubBloomfilter, "value" + (count + 1));

        //then
        //데이터 임계치 도달하여 sub filter를 생성하였음
        Assertions.assertEquals(1, getSubFilterCount(splitShardedSubBloomfilter));
        Assertions.assertEquals(1000, getDataCountOfFilter( splitShardedSubBloomfilter.findActivatedFilter(0)));
        Assertions.assertEquals(0, getDataCountOfFilter( splitShardedSubBloomfilter.findActivatedFilter(1)));
    }

    @Test
    void subfilterCountReachedLimitTest(){
        //given
        SplitShardedSubBloomfilter splitShardedSubBloomfilter = SplitShardedSubBloomfilter.create(
                "testId",
                1000,
                0.01,
                4
        );

        //when
        int count = 1000 + 2000 + 4000 - 1; //원본 > 0번째 > 1번째까지 생성(subFilter = 2, limit 도달)
        for(int i = 0; i < count; i++){
            splitShardedSubBloomfilterHandler.add(splitShardedSubBloomfilter, "value" + (i + 1));
        }

        //then
        Assertions.assertEquals(2, getSubFilterCount(splitShardedSubBloomfilter));
        Assertions.assertEquals(1000, getDataCountOfFilter(splitShardedSubBloomfilter.findActivatedFilter(0)));
        Assertions.assertEquals(2000, getDataCountOfFilter(splitShardedSubBloomfilter.findActivatedFilter(1)));
        Assertions.assertEquals(3999, getDataCountOfFilter(splitShardedSubBloomfilter.findActivatedFilter(2)));

        //when
        splitShardedSubBloomfilterHandler.add(splitShardedSubBloomfilter, "value" + (4000));

        //then
        //sub filter 수는 그대로 2개로 유지, 4000개 데이터 적재 반영됨
        Assertions.assertEquals(2, getSubFilterCount(splitShardedSubBloomfilter));
        Assertions.assertEquals(4000, getDataCountOfFilter(splitShardedSubBloomfilter.findActivatedFilter(2)));
    }

    private long getDataCountOfFilter(SplitShardedBloomfilter splitShardedBloomfilter) {
        String result = stringRedisTemplate.opsForValue().get("split-sharded-original-bloom-filter:data-count:%s".formatted(splitShardedBloomfilter.getId()));

        return (result == null) ? 0 : Long.parseLong(result);
    }

    @Test
    void mightContainTest(){
        //given
        SplitShardedSubBloomfilter splitShardedSubBloomfilter = SplitShardedSubBloomfilter.create(
                "testId",
                1000,
                0.01,
                4
        );

        //when
        int count = 1000 + 5; //원본 > 0번째 생성
        List<String> values = IntStream.range(0, count).mapToObj(i -> "value" + (i + 1)).toList();

        for(String value : values){
            splitShardedSubBloomfilterHandler.add(splitShardedSubBloomfilter, value);
        }

        //then
        int resultCount = 0;
        for(String value : values){
            boolean result = splitShardedSubBloomfilterHandler.mightContain(splitShardedSubBloomfilter, value);

            if(result) resultCount++;
        }

        Assertions.assertEquals(1, getSubFilterCount(splitShardedSubBloomfilter));
        Assertions.assertEquals(1005, resultCount);
    }

    private int getSubFilterCount(SplitShardedSubBloomfilter splitShardedSubBloomfilter) {
        String result = stringRedisTemplate.opsForValue().get("split-sharded-sub-bloom-filter-count:%s".formatted(splitShardedSubBloomfilter.getId()));

        return (result == null) ? 0 : Integer.parseInt(result);
    }
}