package com.redis.bottleneck.common.requestCollapsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.bottleneck.common.distributedLock.DistributedLockProvider;
import com.redis.bottleneck.utils.RedisTestContainerSupportUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@DataRedisTest
@Import(
        {
                RequestCollapsingHandler.class,
                DistributedLockProvider.class
        }
    )
class RequestCollapsingHandlerTest extends RedisTestContainerSupportUtil {

    @Autowired
    private RequestCollapsingHandler requestCollapsingHandler;

    @Test
    void putTest() throws JsonProcessingException {
        //given
        requestCollapsingHandler.put("testKey", Duration.ofSeconds(10), "data");

        //when
        ObjectMapper objectMapper = new ObjectMapper();
        String data = objectMapper.readValue(stringRedisTemplate.opsForValue().get("testKey"), String.class);

        //then
        Assertions.assertEquals("data", data);
    }

    @Test
    void evictTest(){
        //given
        requestCollapsingHandler.put("testKey", Duration.ofSeconds(10), "data");

        //when
        requestCollapsingHandler.evict("testKey");

        //then
        Assertions.assertNull(stringRedisTemplate.opsForValue().get("testKey"));
    }

    @Test
    void fetchAndCachingTest() throws JsonProcessingException, InterruptedException {
        //given
        String testKey = "testKey";
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(10);
        AtomicInteger atomicInteger = new AtomicInteger(0);

        //stub
        Supplier<String> supplier = Mockito.mock(Supplier.class);
        when(supplier.get()).thenAnswer(invocation -> {
            atomicInteger.incrementAndGet();
            return "original data";
        });

        //when / then
        for(int i = 0 ; i < 10 ; i++){
            executorService.execute(() -> {
                 String result1 = requestCollapsingHandler.fetch(
                        testKey,
                        Duration.ofSeconds(10),
                        () -> supplier.get(),
                        String.class
                );
                 Assertions.assertEquals("original data", result1);
                 countDownLatch.countDown();

            });

        }

        countDownLatch.await();

        //then
        ObjectMapper objectMapper = new ObjectMapper();
        String result2 = objectMapper.readValue(stringRedisTemplate.opsForValue().get("testKey"), String.class);

        assertThat(atomicInteger.get()).isEqualTo(1);
        Assertions.assertEquals("original data", result2);
        verify(supplier, times(1)).get();
    }

    @Test
    void fetchAndCachingFailAndCallSupplierTest() throws JsonProcessingException, InterruptedException {
        //given
        String testKey = "testKey";
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(10);
        AtomicInteger atomicInteger = new AtomicInteger(0);

        //stub
        Supplier<String> supplier = Mockito.mock(Supplier.class);
        when(supplier.get()).thenAnswer(invocation -> {
            TimeUnit.SECONDS.sleep(3);
            atomicInteger.incrementAndGet();
            return "original data";
        });

        //when / then
        for(int i = 0 ; i < 10 ; i++){
            executorService.execute(() -> {
                String result1 = requestCollapsingHandler.fetch(
                        testKey,
                        Duration.ofSeconds(10),
                        () -> supplier.get(),
                        String.class
                );
                Assertions.assertEquals("original data", result1);
                countDownLatch.countDown();

            });

        }

        countDownLatch.await();

        //then
        ObjectMapper objectMapper = new ObjectMapper();
        String result2 = objectMapper.readValue(stringRedisTemplate.opsForValue().get("testKey"), String.class);

        assertThat(atomicInteger.get()).isEqualTo(10);
        Assertions.assertEquals("original data", result2);
        verify(supplier, times(10)).get();
    }
}