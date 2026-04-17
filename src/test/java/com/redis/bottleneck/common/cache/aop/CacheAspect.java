package com.redis.bottleneck.common.cache;

import com.redis.bottleneck.common.cache.utils.KeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {
    private final List<CacheHandler> cacheHandlers;
    private final KeyGenerator keyGenerator;

    @Around("@annotation(Cache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

    }
}
