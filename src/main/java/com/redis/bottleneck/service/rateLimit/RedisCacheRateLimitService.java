package com.redis.bottleneck.service.rateLimit;

import com.redis.bottleneck.common.cache.service.RedisCacheService;
import com.redis.bottleneck.common.rateLimit.RateLimiter;
import com.redis.bottleneck.common.strategy.CacheStrategy;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.model.request.ArticleUpdateRequest;
import com.redis.bottleneck.model.response.ArticlePageResponse;
import com.redis.bottleneck.model.response.ArticleResponse;
import com.redis.bottleneck.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisCacheRateLimitService implements RedisCacheService {

    private final ArticleService articleService;
    private final RateLimiter rateLimiter;

    /*
    * 초당 100TPS까지 허용
    * */
    private static final long RATE_LIMIT_COUNT = 100;
    private static final long RATE_LIMIT_PER_SECONDS = 1;

    @Override
    public ArticleResponse read(Long articleId) {

        String id = String.valueOf(articleId);

        if(!rateLimiter.isAllowed(id, RATE_LIMIT_COUNT, RATE_LIMIT_PER_SECONDS)){
            throw new RuntimeException("NOT ALLOWED REQUEST BECAUSE OF RATE LIMIT EXCEEDED");
        }

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
    public void delete(long articleId) {
        articleService.delete(articleId);
    }

    @Override
    public long count(long boardId) {
        return articleService.count(boardId);
    }

    @Override
    public boolean supports(CacheStrategy cacheStrategy) {
        return CacheStrategy.RATE_LIMIT == cacheStrategy;
    }
}
