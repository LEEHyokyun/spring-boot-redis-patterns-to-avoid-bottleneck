package com.redis.bottleneck.apicall;

import com.redis.bottleneck.common.cache.strategy.CacheStrategy;
import com.redis.bottleneck.common.cache.handler.CacheNoneHandler;
import com.redis.bottleneck.model.request.ItemCreateRequest;
import com.redis.bottleneck.model.request.ItemUpdateRequest;
import com.redis.bottleneck.model.response.ItemPageResponse;
import com.redis.bottleneck.model.response.ItemResponse;
import com.redis.bottleneck.utils.EndPointCallUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.ArgumentMatchers.any;

//E2E
@SpringBootTest
public class NoneStrategyApiTest {

    @MockitoSpyBean
    private CacheNoneHandler cacheNoneHandler;

    @DisplayName("API CALL MOCKING TEST")
    @Test
    void apiCallTest(){
        ItemResponse created = EndPointCallUtil.create(
                CacheStrategy.NONE, new ItemCreateRequest("test data")
        );

        ItemResponse readBeforeUpdating = EndPointCallUtil.read(
                CacheStrategy.NONE, created.itemId()
        );

        Assertions.assertEquals("test data", readBeforeUpdating.data());

        ItemResponse updated = EndPointCallUtil.update(
                CacheStrategy.NONE, created.itemId(), new ItemUpdateRequest("updated test data")
        );

        ItemResponse readAfterUpdating = EndPointCallUtil.read(
                CacheStrategy.NONE, updated.itemId()
        );

        Assertions.assertEquals("updated test data", readAfterUpdating.data());

        EndPointCallUtil.delete(CacheStrategy.NONE, created.itemId());

        ItemResponse readAfterDeleting = EndPointCallUtil.read(
                CacheStrategy.NONE, created.itemId()
        );

        Assertions.assertNull(readAfterDeleting);
    }

    @DisplayName("PAGING E2E TEST")
    @Test
    void pagingCallTest(){
        for(int i = 0 ; i < 3 ; i++){
            EndPointCallUtil.create(CacheStrategy.NONE, new ItemCreateRequest("test data" + i));
        }

        ItemPageResponse itemPage1 = EndPointCallUtil.readAll(CacheStrategy.NONE, 1L, 2L);
        Assertions.assertEquals(2, itemPage1.items().size());

        ItemPageResponse itemPage2 = EndPointCallUtil.readAll(CacheStrategy.NONE, 2L, 2L);
        Assertions.assertEquals(1, itemPage2.items().size());
    }

    @DisplayName("INFINITE SCROLL E2E TEST")
    @Test
    void infiniteScrollingTest(){
        for(int i = 0 ; i < 3 ; i++){
            EndPointCallUtil.create(CacheStrategy.NONE, new ItemCreateRequest("test data" + i));
        }

        ItemPageResponse itemPage1 = EndPointCallUtil.readAllInfiniteScroll(CacheStrategy.NONE, null, 2L);
        Assertions.assertEquals(2, itemPage1.items().size());

        ItemPageResponse itemPage2 = EndPointCallUtil.readAllInfiniteScroll(CacheStrategy.NONE, itemPage1.items().getLast().itemId(), 2L);
        Assertions.assertEquals(1, itemPage2.items().size());
    }
}
