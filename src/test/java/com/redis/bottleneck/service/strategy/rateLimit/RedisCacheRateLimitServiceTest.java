package com.redis.bottleneck.service.strategy.rateLimit;

import com.redis.bottleneck.common.strategy.CacheStrategy;
import com.redis.bottleneck.service.ArticleService;
import com.redis.bottleneck.service.rateLimit.RedisCacheRateLimitService;
import com.redis.bottleneck.utils.MySQLAndRedisIntegrationTestContainerSupportUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class RedisCacheRateLimitServiceTest extends MySQLAndRedisIntegrationTestContainerSupportUtil {

    @Autowired
    private RedisCacheRateLimitService redisCacheRateLimitService;

    @MockitoSpyBean
    private ArticleService articleService;

    @Test
    void RateLimitTest() throws InterruptedException {
        //given
        String cacheStrategy = CacheStrategy.RATE_LIMIT.name();
        long boardId = 1L;

        ExecutorService executorService = Executors.newFixedThreadPool(100);
        AtomicInteger atomicInteger1 = new AtomicInteger(0);
        AtomicInteger atomicInteger2 = new AtomicInteger(0);
        CountDownLatch countDownLatch = new CountDownLatch(200);

        //when
        for(int i = 0 ; i < 200 ; i++){
            executorService.execute(()->{
                try{
                    //TPS 이상이면 그대로 진행
                    redisCacheRateLimitService.read(1L);
                    atomicInteger1.incrementAndGet();
                } catch (Exception e){
                    //TPS 초과이면 거부
                    atomicInteger2.incrementAndGet();
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        Assertions.assertEquals(100, atomicInteger1.get());
        Assertions.assertEquals(100, atomicInteger2.get());
    }

    private String genKey(String id){
        return "rate-limit:" + id;
    }
}