package com.redis.bottleneck.repository;

import com.redis.bottleneck.model.domain.Article;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@Slf4j
@DataJpaTest //table 생성 = entity
@AutoConfigureTestDatabase(replace = NONE) //H2가 아닌 MySQL 환경을 DB로 그대로 유지.
@ActiveProfiles("test")
class ArticleRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0.42");

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void findAll() {
        //given
        insertTestData();
        long offset = 0L;
        long limit = 5L;

        //stub
        List<Article> result = articleRepository.findAll(1L, offset, limit);

        //then
        assertThat(result).hasSize(5);
        assertThat(result.get(0).getArticleId()).isEqualTo(20); //desc test
    }

    @Test
    void count() {
        //given
        insertTestData();

        //when
        long count = articleRepository.count();

        //then
        assertThat(count).isEqualTo(20L);
    }

    @Test
    void findAllInfiniteScroll() {
        //given
        insertTestData();

        //when
        List<Article> result = articleRepository.findAllInfiniteScroll(1L,5L);

        //then
        assertThat(result.get(0).getArticleId()).isEqualTo(20L);
        assertThat(result.get(4).getArticleId()).isEqualTo(16L);
    }

    @Test
    void testFindAllInfiniteScroll() {
        // given
        insertTestData();

        // when
        List<Article> page1 = articleRepository.findAllInfiniteScroll(1L, 5L, 16L);
        List<Article> page2 = articleRepository.findAllInfiniteScroll(1L, 5L, 11L);

        // then
        assertThat(page1).hasSize(5);
        assertThat(page2).hasSize(5);

        // 페이징 확인1
        assertThat(page1.get(4).getArticleId())
                .isGreaterThan(page2.get(0).getArticleId());

        // 페이징 확인2
        /*
        * 20 19 18 17 16 (null)
        * 15 14 13 12 11 (last = 16L)
        * 10 9 8 7 6 (last = 11L)
        * */
        assertThat(page2.get(0).getArticleId()).isEqualTo(10L);
    }

    private void insertTestData(){
        Long boardId = 1L;
        for (int i = 1; i <= 20; i++) {
            Article article = Article.create(
                    new ArticleCreateRequest((long) i, "test_" + i, boardId)
            );
            testEntityManager.persist(article);
        }
        testEntityManager.flush();
        testEntityManager.clear();
    }
}