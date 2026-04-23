package com.redis.bottleneck.common.bloomfilter.splitBloomfilter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SplitBloomfilterTest {

    @Test
    void createSplitBloomFilterAndFunctionalTest(){
        //given
        SplitBloomfilter splitBloomfilter = SplitBloomfilter.create("testId", 1000, 0.01);
        long bitSize = splitBloomfilter.getBloomFilter().getBitSize();

        //then
        Assertions.assertEquals(9586, bitSize);

        //given
        long splitCount = splitBloomfilter.getSplitCount();
        Assertions.assertEquals(10, splitCount);  //9586 / 1024 = 9.~~
    }

    @Test
    void findSplitIndexTest(){
        //given
        SplitBloomfilter splitBloomfilter = SplitBloomfilter.create("testId", 1000, 0.01);

        //when/then
        Assertions.assertEquals(0L, splitBloomfilter.findSplitIndex(0L));
        Assertions.assertEquals(0L, splitBloomfilter.findSplitIndex(1023L));
        Assertions.assertEquals(1L, splitBloomfilter.findSplitIndex(1024L));
        Assertions.assertEquals(9L, splitBloomfilter.findSplitIndex(9585L));
    }

    @Test
    void throwingExceptionWhenHashedIndexisOverThanBitSize(){
        //given
        SplitBloomfilter splitBloomfilter = SplitBloomfilter.create("testId", 1000, 0.01);

        //when / then
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> splitBloomfilter.findSplitIndex(9586L)
        );
    }

    @Test
    void findBitSizeOfSplitTest(){
        //given
        SplitBloomfilter splitBloomfilter = SplitBloomfilter.create("testId", 1000, 0.01);

        //when / then (마지막을 제외한 split bloom filter index)
        long splitIndex0 = 0L;
        long splitIndex1 = 1L;

        Assertions.assertEquals(splitBloomfilter.BIT_SPLIT_SIZE, splitBloomfilter.findBitSizeOfSplitIndex(splitIndex0));
        Assertions.assertEquals(splitBloomfilter.BIT_SPLIT_SIZE, splitBloomfilter.findBitSizeOfSplitIndex(splitIndex1));

        //when / then (마지막 split bloom filter index)
        long splitCount = splitBloomfilter.getSplitCount();
        long bitSize = splitBloomfilter.getBloomFilter().getBitSize();
        Assertions.assertEquals(bitSize - splitBloomfilter.BIT_SPLIT_SIZE * (splitCount - 1) , splitBloomfilter.findBitSizeOfSplitIndex(splitCount - 1));
    }
}