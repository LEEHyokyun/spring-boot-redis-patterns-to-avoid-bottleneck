package com.redis.bottleneck.cache.penetration;

import com.redis.bottleneck.common.cache.strategy.CacheStrategy;
import com.redis.bottleneck.service.ArticleService;
import com.redis.bottleneck.service.cache.RedisCachePenetrationNullObjectPatternService;
import com.redis.bottleneck.utils.ArticleEndPointCallUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/*
* 빠른 test를 위해 e2e 시 spring boot test하지 않고,
* end point만 호출하는 방식으로 테스트 가능.
* */
@SpringBootTest
public class CachePenetrationTest {

    @Autowired
    private RedisCachePenetrationNullObjectPatternService redisCachePenetrationNullObjectPatternService;

    @MockitoSpyBean
    private ArticleService articleService;

    @DisplayName("Cache Penetration Env")
    @Test
    void CachePenetrationByreadingNullData(){

        for(int i = 0 ; i < 3 ; i++){
            Assertions.assertEquals(
                    null
                    ,ArticleEndPointCallUtil.read(CacheStrategy.NULL_OBJECT_PATTERN, 9999999999999L)
            );
            ;
        }
    }

    @DisplayName("Cache Penetration Null Object Pattern")
    @Test
    void CahePenetrationNullObjectPattern(){
        //given
        Long articleId = 99999999L;

        //when
        redisCachePenetrationNullObjectPatternService.read(articleId);
        redisCachePenetrationNullObjectPatternService.read(articleId);
        redisCachePenetrationNullObjectPatternService.read(articleId);

        //then(실제 원본 호출은 1번만 진행하는지 확인)
        Mockito.verify(articleService, Mockito.times(1)).read(99999999L);

    }
}
