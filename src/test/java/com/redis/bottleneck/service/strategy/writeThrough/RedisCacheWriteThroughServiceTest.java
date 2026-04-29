package com.redis.bottleneck.service.strategy.writeThrough;

import com.redis.bottleneck.common.strategy.CacheStrategy;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.model.response.ArticlePageResponse;
import com.redis.bottleneck.model.response.ArticleResponse;
import com.redis.bottleneck.service.ArticleService;
import com.redis.bottleneck.service.writeThrough.RedisCacheWriteThroughService;
import com.redis.bottleneck.utils.ArticleEndPointCallUtil;
import com.redis.bottleneck.utils.MySQLAndRedisIntegrationTestContainerSupportUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RedisCacheWriteThroughServiceTest extends ArticleEndPointCallUtil {

//    @Autowired
//    private RedisCacheWriteThroughService redisCacheWriteThroughService;
//
//    @MockitoSpyBean
//    private ArticleService articleService;

    private final CacheStrategy cacheStrategy = CacheStrategy.WRITE_THROUGH;

    @Test
    void integrationTest(){
        //given
        long boardId = 1L;

        for(long i = 0 ; i < 120 ; i++){
            ArticleResponse articleResponse = ArticleEndPointCallUtil.create(
                    cacheStrategy,
                    new ArticleCreateRequest(
                            i,
                            "data " + i,
                            boardId
                    )
            );

            ArticleEndPointCallUtil.read(
                    cacheStrategy,
                    articleResponse.articleId()
            );
        }

        //when / then
        //ArticlePageResponse articlePageResponse1 = ArticleEndPointCallUtil.readAll(cacheStrategy, 1L, 40L);
        //ArticlePageResponse articlePageResponse2 = ArticleEndPointCallUtil.readAll(cacheStrategy, 1L, 40L);
        //ArticlePageResponse articlePageResponse3 = ArticleEndPointCallUtil.readAll(cacheStrategy, 1L, 40L);

    }
}