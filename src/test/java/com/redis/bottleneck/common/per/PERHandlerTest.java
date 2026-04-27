package com.redis.bottleneck.common.per;

import com.redis.bottleneck.utils.RedisTestContainerSupportUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@DataRedisTest
@Import(PERHandler.class)
class PERHandlerTest extends RedisTestContainerSupportUtil {

    @Autowired
    private PERHandler perHandler;

    @Test
    void putTest() throws IOException {
        //given
        perHandler.put("testKey", Duration.ofSeconds(10), "data");

        //when
        ObjectMapper objectMapper = new ObjectMapper();
        PERModel perModel = objectMapper.readValue(stringRedisTemplate.opsForValue().get("testKey"), PERModel.class);

        //then
        Assertions.assertEquals("data", perModel.deserializeModelData(String.class));
    }

    @Test
    void deletingTest() throws IOException {
        //given
        perHandler.put("testKey", Duration.ofSeconds(10), "data");

        //when
        ObjectMapper objectMapper = new ObjectMapper();
        PERModel perModel = objectMapper.readValue(stringRedisTemplate.opsForValue().get("testKey"), PERModel.class);

        //then
        Assertions.assertEquals("data", perModel.deserializeModelData(String.class));

        //when
        perHandler.evict("testKey");

        //then
        Assertions.assertNull(stringRedisTemplate.opsForValue().get("testKey"));
    }

    @Test
    void fetchTest() throws IOException {
        //gvien
        String testKey = "testKey";

        //stub
        Supplier<String> supplier = Mockito.mock(Supplier.class);
        when(supplier.get()).thenReturn(String.valueOf("original data"));

        //when
        perHandler.fetch(
                testKey,
                Duration.ofSeconds(10),
                () -> supplier.get(),
                String.class
        );
        perHandler.fetch(
                testKey,
                Duration.ofSeconds(10),
                () -> supplier.get(),
                String.class
        );
        perHandler.fetch(
                testKey,
                Duration.ofSeconds(10),
                () -> supplier.get(),
                String.class
        );

        //then
        ObjectMapper objectMapper = new ObjectMapper();
        PERModel perModel = objectMapper.readValue(stringRedisTemplate.opsForValue().get("testKey"), PERModel.class);

        Assertions.assertEquals("original data", perModel.deserializeModelData(String.class));
        verify(supplier, times(1)).get();
    }
}