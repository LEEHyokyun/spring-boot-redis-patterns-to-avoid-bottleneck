package com.redis.bottleneck.service.cache;

import com.google.common.base.Verify;
import com.redis.bottleneck.common.cache.handler.CacheNoneHandler;
import com.redis.bottleneck.service.ItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest //aop - spring boot test
class CacheNoneServiceTest {

    @MockitoSpyBean //aop
    private CacheNoneService cacheNoneService;

    @MockitoSpyBean //aop
    private CacheNoneHandler cacheNoneHandler;

    @MockitoSpyBean //mock
    private ItemService itemService;

    @Test
    void readAndAopCalledTest() {

        //given
        long itemId = 1L;

        //when
        cacheNoneService.read(itemId);

        //then
        verify(itemService, times(1)).read(1L);
        verify(cacheNoneHandler, atLeastOnce())
                .fetch(anyString(), any(), any(), any());
    }
}