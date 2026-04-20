package com.redis.bottleneck.service.cache;

import com.redis.bottleneck.common.cache.aop.CacheStrategy;
import com.redis.bottleneck.common.cache.service.RedisCacheService;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.model.request.ArticleUpdateRequest;
import com.redis.bottleneck.model.response.ArticlePageResponse;
import com.redis.bottleneck.model.response.ArticleResponse;
import com.redis.bottleneck.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisCacheArticleService implements RedisCacheService {

    private final ArticleService articleService;

    @Override
    @Cacheable(
            cacheNames = "article",
            key = "#articleId"
    )
    public ArticleResponse read(Long articleId) {
        return articleService.read(articleId);
    }

    @Override
    @Cacheable(
            cacheNames = "articleList",
            key = "#page + ':' + #pageSize"
    )
    public ArticlePageResponse readAll(Long page, Long pageSize) {
        return articleService.readAll(1L, page, pageSize);
    }

    @Override
    @Cacheable(
            cacheNames = "articleListInfiniteScroll",
            key = "#lastArticleId + ':' + #pageSize"
    )
    public ArticlePageResponse readAllInfiniteScroll(Long lastArticleId, Long pageSize) {
        return articleService.readInfiniteScroll(1L, lastArticleId, pageSize);
    }

    @Override
    public ArticleResponse create(ArticleCreateRequest articleCreateRequest) {
        return articleService.create(articleCreateRequest);
    }

    @Override
    @CachePut(
            cacheNames = "article",
            key = "#articleId"
    )
    public ArticleResponse update(Long articleId, ArticleUpdateRequest articleUpdateRequest) {
        return articleService.update(articleId, articleUpdateRequest);
    }

    @Override
    @CacheEvict(
            cacheNames = "article",
            key = "#articleId"
    )
    public void delete(long articleId) {
        articleService.delete(articleId);
    }

    @Override
    public long count() {
        return articleService.count();
    }

    @Override
    public boolean supports(CacheStrategy cacheStrategy) {
        return cacheStrategy == CacheStrategy.SPRING_FRAMEWORK_AOP;
    }
}
