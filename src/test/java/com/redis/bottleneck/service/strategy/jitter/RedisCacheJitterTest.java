package com.redis.bottleneck.service.strategy.jitter;


import com.redis.bottleneck.common.strategy.CacheStrategy;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.service.ArticleService;
import com.redis.bottleneck.service.jitter.RedisCacheJitterService;
import com.redis.bottleneck.utils.MySQLAndRedisIntegrationTestContainerSupportUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class RedisCacheJitterTest extends MySQLAndRedisIntegrationTestContainerSupportUtil {

    @Autowired
    private RedisCacheJitterService redisCacheJitterService;

    @MockitoSpyBean
    private ArticleService articleService;

    @Test
    void jitterTest(){
        //given
        String cacheStrategy = CacheStrategy.Jitter.name();
        String cacheName = "article";
        List<String> keys = IntStream.range(0, 10).mapToObj(i -> "Jitter:article:" + i).toList();
        long boardId = 1L;

        //when
        for(long i = 0 ; i < 10 ; i++){
            redisCacheJitterService.create(
                    new ArticleCreateRequest(
                            i,
                            "test data",
                            boardId
                    )
            );
        }

        //then(10번 서비스 호출)
        for(long i = 0 ; i < 10 ; i++){
            redisCacheJitterService.read(i); //10번
        }
        redisCacheJitterService.read((long) 0);

        //then
        List<Long> ttls = keys.stream()
                .map(key -> stringRedisTemplate.getExpire(key, TimeUnit.SECONDS))
                .toList();

        log.info("CHECK TTL : {}", ttls);

        //then
        verify(articleService, times(10)).read(Mockito.anyLong());
        assertThat(new HashSet<>(ttls).size()).isGreaterThan(2);
        //assertThat(ttls).allMatch(ttl -> ttl >= 7 && ttl <= 13);
    }
}
