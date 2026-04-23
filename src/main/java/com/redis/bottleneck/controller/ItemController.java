package com.redis.bottleneck.controller;

import com.redis.bottleneck.common.cache.strategy.CacheStrategy;
import com.redis.bottleneck.common.cache.service.CacheService;
import com.redis.bottleneck.model.request.ItemCreateRequest;
import com.redis.bottleneck.model.request.ItemUpdateRequest;
import com.redis.bottleneck.model.response.ItemPageResponse;
import com.redis.bottleneck.model.response.ItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ItemController {
    private final List<CacheService> cacheServices;

    @GetMapping("/cache-strategy/{cacheStrategy}/items/{itemId}")
    public ItemResponse read(
            @PathVariable CacheStrategy cacheStrategy,
            @PathVariable Long itemId
    ){
        return this.getCacheService(cacheStrategy).read(itemId);
    }

    @GetMapping("/cache-strategy/{cacheStrategy}/items")
    public ItemPageResponse readAll(
            @PathVariable CacheStrategy cacheStrategy,
            @RequestParam Long page,
            @RequestParam Long pageSize
    ){
        return this.getCacheService(cacheStrategy).readAll(page, pageSize);
    }

    @GetMapping("/cache-strategy/{cacheStrategy}/items/infinite-scroll")
    public ItemPageResponse readAllInfiniteScroll(
            @PathVariable CacheStrategy cacheStrategy,
            @RequestParam(required = false) Long lastItemId,
            @RequestParam Long pageSize
    ){
        return this.getCacheService(cacheStrategy).readAllInfiniteScroll(lastItemId, pageSize);
    }

    @PostMapping("/cache-strategy/{cacheStrategy}/items")
    public ItemResponse create(
            @PathVariable CacheStrategy cacheStrategy,
            @RequestBody ItemCreateRequest itemCreateRequest
    ){
        return this.getCacheService(cacheStrategy).create(itemCreateRequest);
    }

    @PostMapping("/cache-strategy/{cacheStrategy}/items/{itemId}")
    public ItemResponse update(
            @PathVariable CacheStrategy cacheStrategy,
            @PathVariable Long itemId,
            @RequestBody ItemUpdateRequest itemUpdateRequest
    ){
        return this.getCacheService(cacheStrategy).update(itemId, itemUpdateRequest);
    }

    @DeleteMapping("/cache-strategy/{cacheStrategy}/items/{itemId}")
    public void delete(
            @PathVariable CacheStrategy cacheStrategy,
            @PathVariable Long itemId
    ){
        this.getCacheService(cacheStrategy).delete(itemId);
    }

    private CacheService getCacheService(CacheStrategy cacheStrategy){
        return cacheServices.stream()
                .filter(cacheService -> cacheService.supports(cacheStrategy))
                .findFirst()
                .orElseThrow()
                ;
    }
}
