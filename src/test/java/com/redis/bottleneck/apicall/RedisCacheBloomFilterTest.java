package com.redis.bottleneck.apicall;

import com.redis.bottleneck.common.cache.strategy.CacheStrategy;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.service.ArticleService;
import com.redis.bottleneck.service.cache.RedisCachePenetrationBloomFilterService;
import com.redis.bottleneck.utils.MySQLAndRedisIntegrationTestContainerSupportUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest //MySQL / Redis 모두 필요한 통합 테스트
//@Import(
//        {
//                RedisCachePenetrationBloomFilterService.class,
//                ArticleService.class,
//                BloomFilterHandler.class
//        }
//)
@ActiveProfiles("test")
public class RedisCacheBloomFilterTest extends MySQLAndRedisIntegrationTestContainerSupportUtil {

    @Autowired
    private RedisCachePenetrationBloomFilterService redisCachePenetrationBloomFilterService;

    @MockitoSpyBean
    private ArticleService articleService;

    static final CacheStrategy cacheStrategy = CacheStrategy.BLOOM_FILTER;

    @Test
    void bloomFilteringTest(){

        //given
        long boardId = 1L;

        //when
        for(long i = 0 ; i < 1000 ; i++){
            //ArticleEndPointCallUtil.create(cacheStrategy, new ArticleCreateRequest( i + 1, "test data " + i, boardId));
            redisCachePenetrationBloomFilterService.create(new ArticleCreateRequest(i + 1, "test data " + i, boardId));
        }

        /*
        * 있는 데이터에 대해서는 모두 service 호출
        * */
        //when / then
        for(long articleId = 0 ; articleId < 1000 ; articleId++){

            redisCachePenetrationBloomFilterService.read(articleId + 1);

        }

        verify(articleService, times(1000)).read(anyLong());

        /*
        * 없어도 오차율에 따라 적어도 한번은 service 호출
        * */
        //when / then
        for(long articleId = 1000 ; articleId < 2000 ; articleId++){
            //ArticleEndPointCallUtil.read(cacheStrategy, articleId);
            redisCachePenetrationBloomFilterService.read(articleId);
        }

        verify(articleService, atLeastOnce()).read(anyLong());
    }

}
