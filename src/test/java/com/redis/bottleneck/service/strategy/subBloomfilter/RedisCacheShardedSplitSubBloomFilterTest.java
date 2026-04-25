package com.redis.bottleneck.service.strategy.subBloomfilter;

import com.redis.bottleneck.common.bloomfilter.splitShardedBloomfilter.SplitShardedBloomfilter;
import com.redis.bottleneck.common.bloomfilter.splitShardedSubBloomfilter.SplitShardedSubBloomfilter;
import com.redis.bottleneck.common.bloomfilter.splitShardedSubBloomfilter.SplitShardedSubBloomfilterHandler;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.service.ArticleService;
import com.redis.bottleneck.service.cache.RedisCachePenetrationSplitShardedSubFilterService;
import com.redis.bottleneck.utils.MySQLAndRedisIntegrationTestContainerSupportUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class RedisCacheShardedSplitSubBloomFilterTest extends MySQLAndRedisIntegrationTestContainerSupportUtil {

    @Autowired
    private RedisCachePenetrationSplitShardedSubFilterService redisCachePenetrationSplitShardedSubFilterService;

    @MockitoSpyBean
    private ArticleService articleService;

    @MockitoSpyBean
    private SplitShardedSubBloomfilterHandler splitShardedSubBloomfilterHandler;

    @Test
    void subBloomFilteringTest(){
        //given
        long boardId = 1L;

        //when
        for(long i = 0 ; i < 1000 ; i ++){
            redisCachePenetrationSplitShardedSubFilterService.create(
                    new ArticleCreateRequest( i + 1, "test data " + (i + 1), boardId)
            );
        }

        //then
        for(long articleId = 0 ; articleId < 1000 ; articleId++){
            redisCachePenetrationSplitShardedSubFilterService.read(articleId + 1);
        }

        verify(articleService, times(1000)).read(anyLong());

    }


    //over testing

//    private long getDataCountOfFilter(SplitShardedBloomfilter splitShardedBloomfilter) {
//        String result = stringRedisTemplate.opsForValue().get("split-sharded-original-bloom-filter:data-count:%s".formatted(splitShardedBloomfilter.getId()));
//
//        return (result == null) ? 0 : Long.parseLong(result);
//    }
//
//    private int getSubFilterCount(SplitShardedSubBloomfilter splitShardedSubBloomfilter) {
//        String result = stringRedisTemplate.opsForValue().get("split-sharded-sub-bloom-filter-count:%s".formatted(splitShardedSubBloomfilter.getId()));
//
//        return (result == null) ? 0 : Integer.parseInt(result);
//    }
}
