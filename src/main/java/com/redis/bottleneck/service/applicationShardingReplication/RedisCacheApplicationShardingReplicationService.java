package com.redis.bottleneck.service.applicationShardingReplication;

import com.redis.bottleneck.common.cache.aop.CacheEvict;
import com.redis.bottleneck.common.cache.aop.CachePut;
import com.redis.bottleneck.common.cache.aop.Cacheable;
import com.redis.bottleneck.common.cache.service.RedisCacheService;
import com.redis.bottleneck.common.detailCaches.applicationShardingReplication.ApplicationShardingReplicationHandler;
import com.redis.bottleneck.common.strategy.CacheStrategy;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.model.request.ArticleUpdateRequest;
import com.redis.bottleneck.model.response.ArticlePageResponse;
import com.redis.bottleneck.model.response.ArticleResponse;
import com.redis.bottleneck.service.ArticleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisCacheApplicationShardingReplicationService implements RedisCacheService {

    private final ArticleService articleService;

    @Override
    @Cacheable(
            cacheStrategy = CacheStrategy.APPLICATION_HOT_KEY_SHARDING_AND_REPLICATION,
            cacheName = "article",
            key = "#articleId",
            ttl = 10
    )
    public ArticleResponse read(Long articleId) {
        return articleService.read(articleId);
    }

    @Override
    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize) {
        return articleService.readAll(boardId, page, pageSize);
    }

    @Override
    public ArticlePageResponse readAllInfiniteScroll(Long boardId, Long lastArticleId, Long pageSize) {
        return articleService.readInfiniteScroll(boardId, lastArticleId, pageSize);
    }

    @Override
    @Transactional
    public ArticleResponse create(ArticleCreateRequest articleCreateRequest) {
        return articleService.create(articleCreateRequest);
    }

    @Override
    @CachePut(
            cacheStrategy = CacheStrategy.APPLICATION_HOT_KEY_SHARDING_AND_REPLICATION,
            cacheName = "article",
            key = "#articleId",
            ttl = 10
    )
    @Transactional
    public ArticleResponse update(Long articleId, ArticleUpdateRequest articleUpdateRequest) {
        return articleService.update(articleId, articleUpdateRequest);
    }

    @Override
    @CacheEvict(
            cacheStrategy = CacheStrategy.APPLICATION_HOT_KEY_SHARDING_AND_REPLICATION,
            cacheName = "article",
            key = "#articleId"
    )
    @Transactional
    public void delete(long articleId) {
        articleService.delete(articleId);
    }

    @Override
    public long count(long boardId) {
        return articleService.count(boardId);
    }

    @Override
    public boolean supports(CacheStrategy cacheStrategy) {
        return CacheStrategy.APPLICATION_HOT_KEY_SHARDING_AND_REPLICATION == cacheStrategy;
    }
}
