package com.redis.bottleneck.common.cache.aop;

import com.redis.bottleneck.common.cache.handler.CacheHandler;
import com.redis.bottleneck.common.cache.utils.KeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {
    private final List<CacheHandler> cacheHandlers;
    private final KeyGenerator keyGenerator;

    /*
    * advice = 메서드 호출 전/후
    * supplier : cache miss 시 지연실행을 위한 supplier 함수 제공
    * returnType : Redis로 부터 cached한 형태를 Object로 casting 하기 위한 목적
    * 포인트컷 = 파라미터 = bounded.
    * */
    @Around("@annotation(cacheable)")
    public Object handleCacheable(ProceedingJoinPoint joinPoint, Cacheable cacheable) {
        CacheHandler cacheHandler = this.getCacheHandler(cacheable.cacheStrategy());
        String key = keyGenerator.generateKey(joinPoint, cacheable.cacheStrategy(), cacheable.cacheName(), cacheable.key());
        Duration ttl = Duration.ofSeconds(cacheable.ttl());

        Supplier<Object> supplier = this.createSupplier(joinPoint);
        Class returnType = this.findReturnType(joinPoint);

        try{
                log.info("[CacheAspect handleCacheable INFO : key = {}]", key);
            return cacheHandler.fetch(
                    key,
                    ttl,
                    supplier,
                    returnType
            );
        } catch (Exception e){
            log.error("[CacheAspect handleCacheable ERROR : key = {}]", key, e);
            return supplier.get();
        }
    }

    /*
     * advice = 메서드 호출 및 반환값 까지 Return 한 후(*컨트롤러 반환 직전)
     * result = method 실행 후 반환 객체(result 파라미터에 그대로 바인딩)
     * 포인트컷 = 파라미터 = bounded.
     * */
    @AfterReturning(pointcut = "@annotation(cachePut)", returning = "result")
    public void handleCachePut(JoinPoint joinPoint, CachePut cachePut, Object result) {
        CacheStrategy cacheStrategy = cachePut.cacheStrategy();
        CacheHandler cacheHandler = this.getCacheHandler(cacheStrategy);
        String key = keyGenerator.generateKey(joinPoint, cacheStrategy, cachePut.cacheName(), cachePut.key());

        log.info("[CacheAspect handleCachePut INFO : key = {}]", key);

        cacheHandler.put(key, Duration.ofSeconds(cachePut.ttl()), result);
    }

    /*
     * advice = 메서드 호출 및 반환값 까지 Return 한 후(*컨트롤러 반환 직전)
     * result = method 실행 후 반환 객체(result 파라미터에 그대로 바인딩)
     * 포인트컷 = 파라미터 = bounded.
     * */
    @AfterReturning(pointcut = "@annotation(cacheEvict)", returning = "result")
    public void handleCacheEvict(JoinPoint joinPoint, CacheEvict cacheEvict, Object result) {
        CacheStrategy cacheStrategy = cacheEvict.cacheStrategy();
        CacheHandler cacheHandler = this.getCacheHandler(cacheStrategy);
        String key = keyGenerator.generateKey(joinPoint, cacheStrategy, cacheEvict.cacheName(), cacheEvict.key());

        log.info("[CacheAspect handleCacheEvict INFO : key = {}]", key);

        cacheHandler.evict(key);
    }

    private CacheHandler getCacheHandler(CacheStrategy cacheStrategy) {
        return cacheHandlers.stream()
                .filter(handler -> handler.supports(cacheStrategy))
                .findFirst()
                .orElseThrow();
    }

    private Supplier<Object> createSupplier(ProceedingJoinPoint joinPoint) {
        return() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Class findReturnType(JoinPoint joinPoint){
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;

        return methodSignature.getReturnType();
    }
}
