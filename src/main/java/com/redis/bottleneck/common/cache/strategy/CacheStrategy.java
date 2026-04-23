package com.redis.bottleneck.common.cache.strategy;

public enum CacheStrategy {
    NONE,
    SPRING_FRAMEWORK_AOP,
    NULL_OBJECT_PATTERN,
    BLOOM_FILTER,
    SPLIT_BLOOM_FILTER,
    SPLIT_SHARDED_BLOOM_FILTER,
}
