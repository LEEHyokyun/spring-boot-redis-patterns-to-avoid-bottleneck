package com.redis.bottleneck.service.strategy.applicationShardingReplicationTest;

import com.redis.bottleneck.common.serde.DataSerializer;
import com.redis.bottleneck.common.strategy.CacheStrategy;
import com.redis.bottleneck.model.domain.Article;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.service.ArticleService;
import com.redis.bottleneck.service.applicationShardingReplication.RedisCacheApplicationShardingReplicationService;
import com.redis.bottleneck.utils.MySQLAndRedisIntegrationTestContainerSupportUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class ApplicationShardingReplicationTest extends MySQLAndRedisIntegrationTestContainerSupportUtil {

    @Autowired
    private RedisCacheApplicationShardingReplicationService redisCacheApplicationShardingReplicationService;

    @MockitoSpyBean
    private ArticleService articleService;

    @Test
    void ApplicationShardingAndReplicationPutAndCachingServiceTest(){
        //given
        List<String> keys = IntStream.range(0, 10).mapToObj(idx -> {
            return String.valueOf(idx + 1);
        }).toList();
        String cacheStrategy = CacheStrategy.APPLICATION_HOT_KEY_SHARDING_AND_REPLICATION.name();
        String cacheName = "article";
        int shardCount = 3;
        long boardId = 1L;

        //when
        /*
        * Transactional 필수..DB에 flush가 되어야 그 다음 aop가 동작한다.
        * */
        for(long i = 0 ; i < 10 ; i++){
            redisCacheApplicationShardingReplicationService.create(
                    new ArticleCreateRequest(
                            i + 1,
                            "test data " + (i + 1),
                            boardId
                    )
            );
        }

        //then
        for(long i = 0 ; i < 10 ; i++){
            for(long j = 0 ; j < 10 ; j++){
                /*
                * auto increment -> db에 주입되는 id는 1부터 시작한다.
                * */
                redisCacheApplicationShardingReplicationService.read(j + 1);
                //testable -> .read(articleResponse.getArticleId()) ..
            }
        }

        //checking
        //Set<String> keySets = stringRedisTemplate.keys("*");
        //keySets.forEach(k -> log.info("REDIS KEY = {}", k));

        for(int i = 0 ; i < 10 ; i++){
            for(int j = 0 ; j < shardCount ; j++){
                String key = this.getKey(keys.get(i), j);
                log.info("CHECK FINALLY FORMATTED KEY : {} ", key);

                log.info("CHECK THE CHACHING DATA : {}", stringRedisTemplate.opsForValue().get(key));

                Assertions.assertEquals("test data " + (i + 1), DataSerializer.deserializeOrNull(stringRedisTemplate.opsForValue().get(key), Article.class).getData());
            }
        }

        verify(articleService, times(10)).read(Mockito.anyLong());
    }

    /*
     * APPLICATION_HOT_KEY_SHARDING_AND_REPLICATION:article:{#articleId}:{#shardIndex}
     * */
    private String getKey(String key, int shardIndex){
        return "APPLICATION_HOT_KEY_SHARDING_AND_REPLICATION:article:%s:%s".formatted(key, shardIndex);
    }
}
