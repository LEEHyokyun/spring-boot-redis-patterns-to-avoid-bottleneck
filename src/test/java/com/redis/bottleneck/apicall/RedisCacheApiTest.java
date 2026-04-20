package com.redis.bottleneck.apicall;

import com.redis.bottleneck.common.cache.aop.CacheStrategy;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.model.request.ArticleUpdateRequest;
import com.redis.bottleneck.model.response.ArticlePageResponse;
import com.redis.bottleneck.model.response.ArticleResponse;
import com.redis.bottleneck.utils.ArticleEndPointCallUtil;
import com.redis.bottleneck.utils.MySQLIntegrationTestContainerSupportUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class RedisCacheApiTest  {

    static final CacheStrategy cacheStrategy = CacheStrategy.SPRING_FRAMEWORK_AOP;

    @Test
    @Transactional
    void createAndReadAndReadAllAndUpdateAndDeleteTest(){

        long boardId = 1L;

        //given
        ArticleResponse response1 = ArticleEndPointCallUtil.create(cacheStrategy, new ArticleCreateRequest(
                1L, "test data 1", boardId
        ));
        ArticleResponse response2 = ArticleEndPointCallUtil.create(cacheStrategy, new ArticleCreateRequest(
                2L, "test data 2", boardId
        ));
        ArticleResponse response3 = ArticleEndPointCallUtil.create(cacheStrategy, new ArticleCreateRequest(
                3L, "test data 3", boardId
        ));

        //when
        ArticleResponse articleResponse11 = ArticleEndPointCallUtil.read(cacheStrategy, response1.articleId());
        ArticleResponse articleResponse12 = ArticleEndPointCallUtil.read(cacheStrategy, response1.articleId());
        ArticleResponse articleResponse13 = ArticleEndPointCallUtil.read(cacheStrategy, response1.articleId());

        ArticleResponse articleResponse21 = ArticleEndPointCallUtil.read(cacheStrategy, response2.articleId());
        ArticleResponse articleResponse22 = ArticleEndPointCallUtil.read(cacheStrategy, response2.articleId());
        ArticleResponse articleResponse23 = ArticleEndPointCallUtil.read(cacheStrategy, response2.articleId());

        ArticleResponse articleResponse31 = ArticleEndPointCallUtil.read(cacheStrategy, response3.articleId());
        ArticleResponse articleResponse32 = ArticleEndPointCallUtil.read(cacheStrategy, response3.articleId());
        ArticleResponse articleResponse33 = ArticleEndPointCallUtil.read(cacheStrategy, response3.articleId());

        ArticlePageResponse articlePageResponse1 = ArticleEndPointCallUtil.readAll(cacheStrategy, 1L, 2L);
        ArticlePageResponse articlePageResponse2 = ArticleEndPointCallUtil.readAll(cacheStrategy, 2L, 2L);

        ArticlePageResponse articlePageResponseInfiniteScroll1 = ArticleEndPointCallUtil.readAllInfiniteScroll(cacheStrategy, null, 2L);
        ArticlePageResponse articlePageResponseInfiniteScroll2 = ArticleEndPointCallUtil.readAllInfiniteScroll(cacheStrategy, 2L, 2L);



        //then
        Assertions.assertEquals(articleResponse11.articleId(), 1L);
        Assertions.assertEquals(articleResponse12.articleId(), 1);
        Assertions.assertEquals(articleResponse13.articleId(), 1L);

        Assertions.assertEquals(articleResponse21.articleId(), 2L);
        Assertions.assertEquals(articleResponse22.articleId(), 2L);
        Assertions.assertEquals(articleResponse23.articleId(), 2L);

        Assertions.assertEquals(articleResponse31.articleId(), 3L);
        Assertions.assertEquals(articleResponse32.articleId(), 3L);
        Assertions.assertEquals(articleResponse33.articleId(), 3L);

        Assertions.assertEquals(articlePageResponse1.articles().size(), 2L);
        Assertions.assertEquals(articlePageResponse2.articles().size(), 1L);
        Assertions.assertEquals(articlePageResponse1.articles().get(0).articleId(), 3L);
        Assertions.assertEquals(articlePageResponse2.articles().get(0).articleId(), 1L);

        Assertions.assertEquals(articlePageResponseInfiniteScroll1.articles().size(), 2L);
        Assertions.assertEquals(articlePageResponseInfiniteScroll2.articles().size(), 1L);
        Assertions.assertEquals(articlePageResponseInfiniteScroll1.articles().get(0).articleId(), 3L);
        Assertions.assertEquals(articlePageResponseInfiniteScroll2.articles().get(0).articleId(), 1L);

        //given
        ArticleEndPointCallUtil.update(cacheStrategy, response1.articleId(), new ArticleUpdateRequest(1L, "updated test data 1", boardId));
        ArticleResponse articleUpdatedResponse1 = ArticleEndPointCallUtil.read(cacheStrategy, response1.articleId());

        //then
        Assertions.assertEquals(articleUpdatedResponse1.data(), "updated test data 1");

        //given
        ArticleEndPointCallUtil.delete(cacheStrategy, response1.articleId());

        Assertions.assertThrows(
                RuntimeException.class,
                () -> ArticleEndPointCallUtil.read(cacheStrategy, 1L)
        );

    }

}
