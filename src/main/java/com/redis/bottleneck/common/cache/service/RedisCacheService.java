package com.redis.bottleneck.common.cache.service;

import com.redis.bottleneck.common.cache.aop.CacheStrategy;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.model.request.ArticleUpdateRequest;
import com.redis.bottleneck.model.response.ArticlePageResponse;
import com.redis.bottleneck.model.response.ArticleResponse;

public interface RedisCacheService {

    ArticleResponse read(Long articleId);

    ArticlePageResponse readAll(Long page, Long pageSize);

    public ArticlePageResponse readAllInfiniteScroll(Long lastArticleId, Long pageSize);

    public ArticleResponse create(ArticleCreateRequest articleCreateRequest);

    public ArticleResponse update(Long articleId, ArticleUpdateRequest articleUpdateRequest);

    public void delete(long itemId);

    public long count();

    boolean supports(CacheStrategy cacheStrategy);
}
