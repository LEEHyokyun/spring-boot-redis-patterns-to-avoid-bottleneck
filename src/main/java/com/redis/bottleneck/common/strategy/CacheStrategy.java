package com.redis.bottleneck.common.strategy;

public enum CacheStrategy {
    NONE,
    SPRING_FRAMEWORK_AOP,
    NULL_OBJECT_PATTERN,
    BLOOM_FILTER,
    SPLIT_BLOOM_FILTER,
    SPLIT_SHARDED_BLOOM_FILTER,
    SPLIT_SHARDED_SUB_BLOOM_FILTER,
    Jitter,
    PER,
    REQUEST_COLLAPSING,
}
