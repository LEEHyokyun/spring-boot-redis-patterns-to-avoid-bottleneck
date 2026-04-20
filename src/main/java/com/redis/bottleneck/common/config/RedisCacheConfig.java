package com.redis.bottleneck.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

/*
* with
* Redis Cache Config from framework.
* */
@EnableCaching
@Configuration
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultRedisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        )
                )
                ;

        return RedisCacheManager.builder(redisConnectionFactory)
                .withInitialCacheConfigurations(
                        Map.of(
                                "article", defaultRedisCacheConfiguration.entryTtl(Duration.ofSeconds(1)),
                                "articleList", defaultRedisCacheConfiguration.entryTtl(Duration.ofSeconds(1)),
                                "articleListInfiniteScroll", defaultRedisCacheConfiguration.entryTtl(Duration.ofSeconds(1))
                        )
                )
                .build()
                ;
    }
}
