package com.redis.bottleneck.common.bloomfilter.splitShardedSubBloomfilter;

import com.redis.bottleneck.common.bloomfilter.splitShardedBloomfilter.SplitShardedBloomfilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SplitShardedSubBloomfilterTest {

    @Test
    void createSubFiltersTest(){
        //when
        SplitShardedSubBloomfilter splitShardedSubBloomfilter = SplitShardedSubBloomfilter.create(
              "testId", 1000, 0.01, 4
        );

        //then
        Assertions.assertEquals("testId", splitShardedSubBloomfilter.getId());
    }

    @Test
    void findSubFilterTest(){
        //given
        SplitShardedSubBloomfilter splitShardedSubBloomfilter = SplitShardedSubBloomfilter.create(
                "testId",
                1000,
                0.01,
                4
        );

        //when
        //sub filter 2개 생성
        SplitShardedBloomfilter splitShardedSubBloomfilter1 = splitShardedSubBloomfilter.findSubFilter(0);
        SplitShardedBloomfilter splitShardedSubBloomfilter2 = splitShardedSubBloomfilter.findSubFilter(1);
        SplitShardedBloomfilter splitShardedSubBloomfilter3 = splitShardedSubBloomfilter.findSubFilter(2);

        //then
        Assertions.assertEquals(splitShardedSubBloomfilter.getId() + ":sub-filter:0", splitShardedSubBloomfilter1.getId());
        Assertions.assertEquals(splitShardedSubBloomfilter.getId() + ":sub-filter:1", splitShardedSubBloomfilter2.getId());
        Assertions.assertEquals(splitShardedSubBloomfilter.getId() + ":sub-filter:2", splitShardedSubBloomfilter3.getId());

        //then
        Assertions.assertEquals(splitShardedSubBloomfilter.getSplitShardedBloomfilter().getDataCount() * 2, splitShardedSubBloomfilter1.getDataCount());
        Assertions.assertEquals(splitShardedSubBloomfilter.getSplitShardedBloomfilter().getDataCount() * 4, splitShardedSubBloomfilter2.getDataCount());
        Assertions.assertEquals(splitShardedSubBloomfilter.getSplitShardedBloomfilter().getDataCount() * 8, splitShardedSubBloomfilter3.getDataCount());

        //then
        Assertions.assertEquals(splitShardedSubBloomfilter.getSplitShardedBloomfilter().getFalsePositiveRate() * 0.5, splitShardedSubBloomfilter1.getFalsePositiveRate());
        Assertions.assertEquals(splitShardedSubBloomfilter.getSplitShardedBloomfilter().getFalsePositiveRate() * 0.25, splitShardedSubBloomfilter2.getFalsePositiveRate());
        Assertions.assertEquals(splitShardedSubBloomfilter.getSplitShardedBloomfilter().getFalsePositiveRate() * 0.125, splitShardedSubBloomfilter3.getFalsePositiveRate());

        //then
        Assertions.assertEquals(splitShardedSubBloomfilter.getSplitShardedBloomfilter().getShardCount(), splitShardedSubBloomfilter1.getShardCount());
        Assertions.assertEquals(splitShardedSubBloomfilter.getSplitShardedBloomfilter().getShardCount(), splitShardedSubBloomfilter2.getShardCount());
        Assertions.assertEquals(splitShardedSubBloomfilter.getSplitShardedBloomfilter().getShardCount(), splitShardedSubBloomfilter3.getShardCount());
    }

    @Test
    void findActivatedFilterForOriginFilterTest(){
        //given
        SplitShardedSubBloomfilter splitShardedSubBloomfilter = SplitShardedSubBloomfilter.create(
                "testId",
                1000,
                0.01,
                4
        );

        //when
        SplitShardedBloomfilter originalFilter = splitShardedSubBloomfilter.findActivatedFilter(0);

        //then
        Assertions.assertEquals(originalFilter.getId(), splitShardedSubBloomfilter.getId());
    }

    @Test
    void findActivatedFilterForSubFilterTest(){
        //given
        SplitShardedSubBloomfilter splitShardedSubBloomfilter = SplitShardedSubBloomfilter.create(
                "testId",
                1000,
                0.01,
                4
        );
        int subFilterCount = 3; //원본 + 3개의 sub filter, 마지막 sub filter에 대한 index.

        //when
        SplitShardedBloomfilter activatedFilter = splitShardedSubBloomfilter.findActivatedFilter(subFilterCount);

        //then
        Assertions.assertEquals(splitShardedSubBloomfilter.getId() + ":sub-filter:2", activatedFilter.getId());
    }

    @Test
    void findAllTest(){
        //given
        SplitShardedSubBloomfilter splitShardedSubBloomfilter = SplitShardedSubBloomfilter.create(
                "testId",
                1000,
                0.01,
                4
        );
        int subFilterCount = 3; //원본 포함 총 4개의 bloom filter가 생성됨

        //when
        List<SplitShardedBloomfilter> splitShardedBloomfilterList = splitShardedSubBloomfilter.findAll(subFilterCount);

        //then
        Assertions.assertEquals(subFilterCount + 1, splitShardedBloomfilterList.size());
        Assertions.assertEquals(splitShardedSubBloomfilter.getId(), splitShardedBloomfilterList.get(0).getId());
        Assertions.assertEquals("testId:sub-filter:0", splitShardedBloomfilterList.get(1).getId());
        Assertions.assertEquals("testId:sub-filter:1", splitShardedBloomfilterList.get(2).getId());
        Assertions.assertEquals("testId:sub-filter:2", splitShardedBloomfilterList.get(3).getId());

        //then
        SplitShardedBloomfilter subFilter = splitShardedBloomfilterList.get(1);
        Assertions.assertEquals(subFilter.getId(), splitShardedSubBloomfilter.findSubFilter(0).getId());

        /*
        * findSubFilter -> sub filter index
        * findAll -> 원본을 포함한 모든 filters
        * */
    }
}