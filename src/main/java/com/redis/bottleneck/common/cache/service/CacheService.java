package com.redis.bottleneck.common.cache.service;

import com.redis.bottleneck.common.cache.strategy.CacheStrategy;
import com.redis.bottleneck.model.request.ItemCreateRequest;
import com.redis.bottleneck.model.request.ItemUpdateRequest;
import com.redis.bottleneck.model.response.ItemPageResponse;
import com.redis.bottleneck.model.response.ItemResponse;

public interface CacheService {
    ItemResponse read(Long itemId);

    ItemPageResponse readAll(Long page, Long pageSize);

    public ItemPageResponse readAllInfiniteScroll(Long lastItemId, Long pageSize);

    public ItemResponse create(ItemCreateRequest itemCreateRequest);

    public ItemResponse update(Long itemId, ItemUpdateRequest itemUpdateRequest);

    public void delete(long itemId);

    public long count();

    boolean supports(CacheStrategy cacheStrategy);
}
