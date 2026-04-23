package com.redis.bottleneck.service.cache;

import com.redis.bottleneck.common.cache.strategy.CacheStrategy;
import com.redis.bottleneck.common.cache.service.RedisCacheService;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.model.request.ArticleUpdateRequest;
import com.redis.bottleneck.model.response.ArticlePageResponse;
import com.redis.bottleneck.model.response.ArticleResponse;
import com.redis.bottleneck.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisCachePenetrationNullObjectPatternService implements RedisCacheService {

    private final ArticleService articleService;

    @Override
    @Cacheable(cacheNames = "article", key = "#articleId")
    public ArticleResponse read(Long articleId) {
        //Null Object pattern
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
    public ArticleResponse create(ArticleCreateRequest articleCreateRequest) {
        return articleService.create(articleCreateRequest);
    }

    @Override
    public ArticleResponse update(Long articleId, ArticleUpdateRequest articleUpdateRequest) {
        return articleService.update(articleId, articleUpdateRequest);
    }

    @Override
    public void delete(long itemId) {
        articleService.delete(itemId);
    }

    @Override
    public long count(long boardId) {
        return articleService.count(boardId);
    }

    @Override
    public boolean supports(CacheStrategy cacheStrategy) {
        return cacheStrategy == CacheStrategy.NULL_OBJECT_PATTERN;
    }
}
