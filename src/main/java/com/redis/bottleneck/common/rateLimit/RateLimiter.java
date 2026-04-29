package com.redis.bottleneck.common.rateLimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RateLimiter {
    private final StringRedisTemplate stringRedisTemplate;

    /*
    * TPS 계산하고 limit 초과 시 요청 거부
    * */
    public boolean isAllowed(String id, long limit, long perSeconds){
        String key = this.genKey(id);

        Long count = stringRedisTemplate.opsForValue().increment(key);

        if(count == null) return false; //연산의 정상적 수행이 불가능할 경우

        if(count == 1){
            stringRedisTemplate.expire(key, Duration.ofSeconds(perSeconds));
        }

        if(count <= limit)
            return true;

        //ttl 설정 오류 시를 대비한 방어 로직
        if(count % (limit / 10) == 1 && stringRedisTemplate.getExpire(key) == -1){
            stringRedisTemplate.expire(key, Duration.ofSeconds(perSeconds));
        }

        return false;
    }

    private String genKey(String id){
        return "rate-limit:" + id;
    }
}
