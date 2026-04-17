package com.redis.bottleneck.common.cache.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
* Method에 Runtime 시점 적용
* */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable {
    /*
    * 캐싱 어노테이션 속성 정의
    * - 속성 및 속성에 대한 형태를 정의
    * - 리플렉션 매핑 및 type safe한 entry 생성 목적
    * */
    CacheStrategy cacheStrategy();
    String cacheName();
    String key();
    long ttl();
}
