package com.redis.bottleneck.common.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@RequiredArgsConstructor
/*
* Customized AOP Config
* */
public class CacheConfig {
    private final StringRedisTemplate redisTemplate;

    /*
    * Redis connect 시 construct 하기 전에, redis의 모든 데이터를 초기화.
    * 초기화된 상태에서 테스트 진행
    * */
    @PostConstruct
    public void clearRedisOnStartUp(){
        redisTemplate.getConnectionFactory()
                .getConnection()
                .flushDb();
    }
}
