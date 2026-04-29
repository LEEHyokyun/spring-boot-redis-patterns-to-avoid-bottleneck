package com.redis.bottleneck.common.jitter;

import com.redis.bottleneck.common.detailCaches.jitter.JitterHandler;
import com.redis.bottleneck.utils.RedisTestContainerSupportUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataRedisTest
@Import(JitterHandler.class)
@Slf4j
class JitterHandlerTest extends RedisTestContainerSupportUtil {

    @Autowired
    private JitterHandler jitterHandler;

    @Test
    void putTest(){
        //given / when
        jitterHandler.put(
                "testKey1",
                Duration.ofSeconds(10),
                String.class
        );
        jitterHandler.put(
                "testKey2",
                Duration.ofSeconds(10),
                String.class
        );

        //then
        assertThat(stringRedisTemplate.getExpire("testKey1", TimeUnit.SECONDS)).isGreaterThan(7);
        assertThat( stringRedisTemplate.getExpire("testKey2", TimeUnit.SECONDS)).isLessThan(13);
    }

    @Test
    void throwIllegalArgumentExceptionTest(){
        //given / when / then
        assertThrows(IllegalArgumentException.class, () -> {
            jitterHandler.put(
                    "testKey",
                    Duration.ofSeconds(2),
                    String.class
            );
        });
    }

    @Test
    void cachingEvictTest(){
        //given / when
        jitterHandler.put(
                "testKey"
                ,Duration.ofSeconds(4),
                String.class
        );

        //then
        Assertions.assertNotNull(stringRedisTemplate.opsForValue().get("testKey"));

        //given / when
        jitterHandler.evict("testKey");

        //then
        Assertions.assertNull(stringRedisTemplate.opsForValue().get("testKey"));
    }

    @Test
    void FetchTest() throws JsonProcessingException {
        //given
        String testKey = "testKey";

        //stub
        Supplier<String> supplier = Mockito.mock(Supplier.class);
        when(supplier.get()).thenReturn(String.valueOf("original data"));

        //when
        jitterHandler.fetch(
                "testKey",
                Duration.ofSeconds(10),
                () -> supplier.get(),
                String.class
        );
        jitterHandler.fetch(
                "testKey",
                Duration.ofSeconds(10),
                () -> supplier.get(),
                String.class
        );
        jitterHandler.fetch(
                "testKey",
                Duration.ofSeconds(10),
                () -> supplier.get(),
                String.class
        );

        //then
        Assertions.assertEquals("\"original data\"", stringRedisTemplate.opsForValue().get("testKey"));

        //when
        verify(supplier, times(1)).get();

    }
}