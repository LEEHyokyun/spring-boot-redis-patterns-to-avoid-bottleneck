package com.redis.bottleneck.service.strategy.requestCollapsing;

import com.redis.bottleneck.common.strategy.CacheStrategy;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.service.ArticleService;
import com.redis.bottleneck.service.requestCollapsing.RedisCacheRequestCollapsingService;
import com.redis.bottleneck.utils.MySQLAndRedisIntegrationTestContainerSupportUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class RedisCacheRequestCollapsingServiceTest extends MySQLAndRedisIntegrationTestContainerSupportUtil {

    @Autowired
    private RedisCacheRequestCollapsingService redisCacheRequestCollapsingService;

    @MockitoSpyBean
    private ArticleService articleService;

    @Test
    void requestCollapsingTest(){
        //given
        String cacheStrategy = CacheStrategy.REQUEST_COLLAPSING.name();
        String cacheName = "article";
        long boardId = 1L;

        //when
        for(long i = 0 ; i < 10 ; i++){
            redisCacheRequestCollapsingService.create(
                    new ArticleCreateRequest(
                            i,
                            "test data" + i,
                            boardId
                    )
            );
        }

        //then
        for(long i = 0 ; i < 10 ; i++){
            for(long j = 0 ; j < 10 ; j++){
                redisCacheRequestCollapsingService.read(j);
            }
        }

        verify(articleService, times(10)).read(Mockito.anyLong());
    }

}