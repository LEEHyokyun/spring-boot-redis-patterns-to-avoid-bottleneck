package com.redis.bottleneck.service.cache;

import com.redis.bottleneck.common.cache.aop.CachePut;
import com.redis.bottleneck.common.cache.aop.CacheStrategy;
import com.redis.bottleneck.common.cache.aop.Cacheable;
import com.redis.bottleneck.common.cache.service.CacheService;
import com.redis.bottleneck.model.request.ItemCreateRequest;
import com.redis.bottleneck.model.request.ItemUpdateRequest;
import com.redis.bottleneck.model.response.ItemPageResponse;
import com.redis.bottleneck.model.response.ItemResponse;
import com.redis.bottleneck.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheNoneService implements CacheService {

    private final ItemService itemService;

    @Override
    @Cacheable(
            cacheStrategy = CacheStrategy.NONE,
            cacheName = "READ",
            key = "#itemId",
            ttl = 5
    )
    public ItemResponse read(Long itemId) {
        return itemService.read(itemId);
    }

    @Override
    @Cacheable(
            cacheStrategy = CacheStrategy.NONE,
            cacheName = "READALL",
            key = "#page + ':' + #pageSize",
            ttl = 5
    )
    public ItemPageResponse readAll(Long page, Long pageSize) {
        return itemService.readAll(page, pageSize);
    }

    @Override
    @Cacheable(
            cacheStrategy = CacheStrategy.NONE,
            cacheName = "READALLINFINITESCROLL",
            key = "#lastItemId + ':' + #pageSize",
            ttl = 5
    )
    public ItemPageResponse readAllInfiniteScroll(Long lastItemId, Long pageSize) {
        return itemService.readAllInfiniteScroll(lastItemId, pageSize);
    }

    @Override
    public ItemResponse create(ItemCreateRequest itemCreateRequest) {
        return itemService.create(itemCreateRequest);
    }

    @Override
    @CachePut(
            cacheStrategy = CacheStrategy.NONE,
            cacheName = "UPDATE",
            key = "#itemId",
            ttl = 5
    )
    public ItemResponse update(Long itemId, ItemUpdateRequest itemUpdateRequest) {
        return itemService.update(itemId, itemUpdateRequest);
    }

    @Override
    @CachePut(
            cacheStrategy = CacheStrategy.NONE,
            cacheName = "DELETE",
            key = "#itemId",
            ttl = 5
    )
    public void delete(long itemId) {
        itemService.delete(itemId);
    }

    @Override
    public long count() {
        return itemService.count();
    }

    @Override
    public boolean supports(CacheStrategy cacheStrategy){
        return cacheStrategy == CacheStrategy.NONE;
    }
}
