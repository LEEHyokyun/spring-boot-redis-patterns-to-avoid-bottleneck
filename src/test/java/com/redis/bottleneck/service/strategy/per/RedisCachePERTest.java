package com.redis.bottleneck.service.strategy.per;

import com.redis.bottleneck.common.strategy.CacheStrategy;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.model.response.ArticleResponse;
import com.redis.bottleneck.service.ArticleService;
import com.redis.bottleneck.service.per.RedisCachePERService;
import com.redis.bottleneck.utils.ArticleEndPointCallUtil;
import com.redis.bottleneck.utils.MySQLAndRedisIntegrationTestContainerSupportUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class RedisCachePERTest extends MySQLAndRedisIntegrationTestContainerSupportUtil {

    @Autowired
    private RedisCachePERService redisCachePERService;

    @MockitoSpyBean
    private ArticleService articleService;

    @Test
    void PERTest(){
        //gvien
        String cacheStrategy = CacheStrategy.PER.name();
        String cacheName = "article";
        List<String> keys = IntStream.range(0, 100).mapToObj(i -> "PER:article:" + i).toList();
        long boardId = 1L;

        //when
        for(long i = 0 ; i < 50 ; i++){
            redisCachePERService.create(
                    new ArticleCreateRequest(
                            i,
                            "test data" + i,
                            boardId
                    )
            );
        }

        //then
        for(long i = 0 ; i < 10 ; i++){
            for(long j = 0 ; j < 50 ; j++){
                redisCachePERService.read(j);
            }
        }

        verify(articleService, times(50)).read(Mockito.anyLong());

    }

    @Test
    void ApiCallTest(){
        //given
        CacheStrategy cacheStrategy = CacheStrategy.PER;

        //when
        for(long i = 0 ; i < 100 ; i++){
            ArticleResponse articleResponse = ArticleEndPointCallUtil.create(cacheStrategy, new ArticleCreateRequest(
                    i,
                    "test data " + i,
                    1L
            ));
        };

        for(long i = 0 ; i < 10 ; i++){
            for(long j = 0 ; j < 100 ; j++){
                ArticleEndPointCallUtil.read(cacheStrategy, j);
            }
        }

        //then : log 확인
    }
}
