package com.redis.bottleneck.common.detailCaches.bloomfilter;

@FunctionalInterface
public interface BloomFilterHashFunction {
    long hash(String value);
}
