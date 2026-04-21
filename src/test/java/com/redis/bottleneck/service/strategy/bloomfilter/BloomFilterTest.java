package com.redis.bottleneck.service.strategy.bloomfilter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloomFilterTest {

    @Test
    void create1() {

        //given / when
        BloomFilter bloomFilter1 = BloomFilter.create("testId1", 1000, 0.01);

        //then
        Assertions.assertEquals(bloomFilter1.getId(), "testId1");
        Assertions.assertEquals(bloomFilter1.getDataCount(), 1000);
        Assertions.assertEquals(bloomFilter1.getFalsePositiveRate(), 0.01);
        Assertions.assertEquals(bloomFilter1.getBitSize(), 9586);
        Assertions.assertEquals(bloomFilter1.getHashFunctionCount(), 7);

    }

    @Test
    void create2() {

        //given / when
        BloomFilter bloomFilter1 = BloomFilter.create("testId1", 100_000_000, 0.01);

        //then
        Assertions.assertEquals(bloomFilter1.getId(), "testId1");
        Assertions.assertEquals(bloomFilter1.getDataCount(), 100_000_000);
        Assertions.assertEquals(bloomFilter1.getFalsePositiveRate(), 0.01);
        Assertions.assertEquals(bloomFilter1.getBitSize(), 958_505_838);
        Assertions.assertEquals(bloomFilter1.getHashFunctionCount(), 7);

    }

    @Test
    void hash() {
        //given
        String key = "testId";

        BloomFilter bloomFilter1 = BloomFilter.create("testId", 1000, 0.01);

        for(int i = 0; i < 100; i++) {
            List<Long> hashedIndices = bloomFilter1.hash(key + i);
            Assertions.assertEquals(hashedIndices.size(), bloomFilter1.getHashFunctionCount());

            for(Long hashedIndex : hashedIndices) {
                assertThat(hashedIndex).isGreaterThanOrEqualTo(0);
                assertThat(hashedIndex).isLessThan(bloomFilter1.getBitSize());
            }
        }
    }
}