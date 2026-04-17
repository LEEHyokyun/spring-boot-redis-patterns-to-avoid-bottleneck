package com.redis.bottleneck.apicall;

import com.redis.bottleneck.common.cache.aop.CacheStrategy;
import com.redis.bottleneck.common.cache.handler.CacheNoneHandler;
import com.redis.bottleneck.model.request.ItemCreateRequest;
import com.redis.bottleneck.model.request.ItemUpdateRequest;
import com.redis.bottleneck.model.response.ItemResponse;
import com.redis.bottleneck.utils.EndPointCallUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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


}
