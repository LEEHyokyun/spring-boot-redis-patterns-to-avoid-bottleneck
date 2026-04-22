package com.redis.bottleneck.common.bloomfilter;

@FunctionalInterface
public interface BloomFilterHashFunction {
    long hash(String value);
}
