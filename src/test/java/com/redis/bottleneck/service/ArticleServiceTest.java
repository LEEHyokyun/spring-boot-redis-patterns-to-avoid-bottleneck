package com.redis.bottleneck.service;

import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.model.response.ArticlePageResponse;
import com.redis.bottleneck.utils.MySQLIntegrationTestContainerSupportUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ArticleServiceTest extends MySQLIntegrationTestContainerSupportUtil {

    @Autowired
    private ArticleService articleService;

    @Test
    @Transactional
    void createAndReadTest() {
        //given
        long boardId = 1L;
        long offset = 6L;
        long limit = 5L;

        //when
        articleService.create(new ArticleCreateRequest(1L, "test 1", boardId));
        articleService.create(new ArticleCreateRequest(2L, "test 2", boardId));
        articleService.create(new ArticleCreateRequest(3L, "test 3", boardId));
        articleService.create(new ArticleCreateRequest(4L, "test 4", boardId));
        articleService.create(new ArticleCreateRequest(5L, "test 5", boardId));
        articleService.create(new ArticleCreateRequest(6L, "test 6", boardId));
        articleService.create(new ArticleCreateRequest(7L, "test 7", boardId));
        articleService.create(new ArticleCreateRequest(8L, "test 8", boardId));
        articleService.create(new ArticleCreateRequest(9L, "test 9", boardId));
        articleService.create(new ArticleCreateRequest(10L, "test 10", boardId));

        ArticlePageResponse response1 =  articleService.readInfiniteScroll(
                boardId, null, limit
        );
        ArticlePageResponse response2 =  articleService.readInfiniteScroll(
                boardId, offset, limit
        );

        //then

        //size
        Assertions.assertThat(response1.articles().size()).isEqualTo(5);
        Assertions.assertThat(response2.articles().size()).isEqualTo(5);

        //result
        Assertions.assertThat(response1.articles().get(0).articleId()).isEqualTo(10L);
        Assertions.assertThat(response2.articles().get(0).articleId()).isEqualTo(5L);
    }
}