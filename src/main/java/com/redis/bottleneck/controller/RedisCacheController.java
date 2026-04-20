package com.redis.bottleneck.controller;

import com.redis.bottleneck.common.cache.aop.CacheStrategy;
import com.redis.bottleneck.common.cache.service.CacheService;
import com.redis.bottleneck.common.cache.service.RedisCacheService;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.model.request.ArticleUpdateRequest;
import com.redis.bottleneck.model.request.ItemCreateRequest;
import com.redis.bottleneck.model.request.ItemUpdateRequest;
import com.redis.bottleneck.model.response.ArticlePageResponse;
import com.redis.bottleneck.model.response.ArticleResponse;
import com.redis.bottleneck.model.response.ItemPageResponse;
import com.redis.bottleneck.model.response.ItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RedisCacheController {
    private final List<RedisCacheService> cacheServices;

    @GetMapping("/cache-strategy/{cacheStrategy}/articles/{articleId}")
    public ArticleResponse read(
            @PathVariable CacheStrategy cacheStrategy,
            @PathVariable Long articleId
    ){
        return this.getCacheService(cacheStrategy).read(articleId);
    }

    @GetMapping("/cache-strategy/{cacheStrategy}/articles")
    public ArticlePageResponse readAll(
            @PathVariable CacheStrategy cacheStrategy,
            @RequestParam Long boardId,
            @RequestParam Long page,
            @RequestParam Long pageSize
    ){
        return this.getCacheService(cacheStrategy).readAll(boardId, page, pageSize);
    }

    @GetMapping("/cache-strategy/{cacheStrategy}/articles/infinite-scroll")
    public ArticlePageResponse readAllInfiniteScroll(
            @PathVariable CacheStrategy cacheStrategy,
            @RequestParam Long boardId,
            @RequestParam(required = false) Long lastArticleId,
            @RequestParam Long pageSize
    ){
        return this.getCacheService(cacheStrategy).readAllInfiniteScroll(boardId, lastArticleId, pageSize);
    }

    @PostMapping("/cache-strategy/{cacheStrategy}/articles")
    public ArticleResponse create(
            @PathVariable CacheStrategy cacheStrategy,
            @RequestBody ArticleCreateRequest articleCreateRequest
    ){
        return this.getCacheService(cacheStrategy).create(articleCreateRequest);
    }

    @PostMapping("/cache-strategy/{cacheStrategy}/articles/{articleId}")
    public ArticleResponse update(
            @PathVariable CacheStrategy cacheStrategy,
            @PathVariable Long articleId,
            @RequestBody ArticleUpdateRequest articleUpdateRequest
    ){
        return this.getCacheService(cacheStrategy).update(articleId, articleUpdateRequest);
    }

    @DeleteMapping("/cache-strategy/{cacheStrategy}/articles/{articleId}")
    public void delete(
            @PathVariable CacheStrategy cacheStrategy,
            @PathVariable Long articleId
    ){
        this.getCacheService(cacheStrategy).delete(articleId);
    }

    @GetMapping("/cache-strategy/{cacheStrategy}/articleCount/{boardId}")
    public long count(
            @PathVariable CacheStrategy cacheStrategy,
            @PathVariable Long boardId
    ){
        return this.getCacheService(cacheStrategy).count(boardId);
    }

    private RedisCacheService getCacheService(CacheStrategy cacheStrategy){
        return cacheServices.stream()
                .filter(cacheService -> cacheService.supports(cacheStrategy))
                .findFirst()
                .orElseThrow()
                ;
    }
}
