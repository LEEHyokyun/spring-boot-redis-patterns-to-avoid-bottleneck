package com.redis.bottleneck.common.splitBloomfilter;

import com.redis.bottleneck.common.bloomfilter.BloomFilter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SplitBloomfilter {
    private String id;
    private BloomFilter bloomFilter;
    private 
}
