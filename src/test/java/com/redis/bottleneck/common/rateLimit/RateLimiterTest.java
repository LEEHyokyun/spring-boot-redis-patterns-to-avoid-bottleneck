package com.redis.bottleneck.common.rateLimit;

import com.redis.bottleneck.common.detailCaches.rateLimit.RateLimiter;
import com.redis.bottleneck.utils.RedisTestContainerSupportUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DataRedisTest
@Import(RateLimiter.class)
class RateLimiterTest extends RedisTestContainerSupportUtil {

    @Autowired
    private RateLimiter rateLimiter;

    @Test
    void isAllowedTest(){
        //given
        String id = "testId";
        long limit = 10;

        //when
        long count = 0;
        for(int i = 0 ; i < limit ; i++){
            boolean result = rateLimiter.isAllowed(id, limit, 10);

            if(result) count++;
        }

        long ttl = stringRedisTemplate.getExpire(this.genKey(id));

        //then
        log.info("Remained ttl : " + ttl);
        Assertions.assertEquals(10, count);
        assertThat(rateLimiter.isAllowed(id, limit, 10)).isFalse();

    }

    private String genKey(String id){
        return "rate-limit:" + id;
    }
}