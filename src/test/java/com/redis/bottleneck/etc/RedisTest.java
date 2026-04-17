package com.redis.bottleneck.etc;

import com.redis.bottleneck.utils.RedisTestContainerSupport;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;

@DataRedisTest
@Slf4j
public class RedisTest extends RedisTestContainerSupport {

    @DisplayName("Redis Container Mock Test")
    @Test
    void test1(){
        stringRedisTemplate.opsForValue().set("key","value");

        String value = stringRedisTemplate.opsForValue().get("key");

        Assertions.assertEquals("value", value);
    }

    @DisplayName("Redis Container db flush Test")
    @Test
    void test2(){
        String value = stringRedisTemplate.opsForValue().get("key");

        Assertions.assertEquals(value, null);
    }

    @DisplayName("Redis Container db flush Test")
    @Test
    void test3(){
        String value = stringRedisTemplate.opsForValue().get("key");

        Assertions.assertEquals(value, null);
    }


}
