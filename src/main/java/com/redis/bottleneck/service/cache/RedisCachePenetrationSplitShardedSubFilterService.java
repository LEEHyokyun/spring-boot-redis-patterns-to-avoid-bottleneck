package com.redis.bottleneck.service.cache;

import com.redis.bottleneck.common.bloomfilter.splitShardedSubBloomfilter.SplitShardedSubBloomfilter;
import com.redis.bottleneck.common.bloomfilter.splitShardedSubBloomfilter.SplitShardedSubBloomfilterHandler;
import com.redis.bottleneck.common.cache.service.RedisCacheService;
import com.redis.bottleneck.common.cache.strategy.CacheStrategy;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.model.request.ArticleUpdateRequest;
import com.redis.bottleneck.model.response.ArticlePageResponse;
import com.redis.bottleneck.model.response.ArticleResponse;
import com.redis.bottleneck.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisCachePenetrationSplitShardedSubFilterService implements RedisCacheService {

    private final ArticleService articleService;
    private final SplitShardedSubBloomfilterHandler splitShardedSubBloomfilterHandler;

    private static final SplitShardedSubBloomfilter splitShardedSubBloomfilter = SplitShardedSubBloomfilter.create(
            "article-bloom-filter",
            1000,
            0.01,
            4
    );

    @Override
    public ArticleResponse read(Long articleId) {

        Boolean result = splitShardedSubBloomfilterHandler.mightContain(splitShardedSubBloomfilter, String.valueOf(articleId));

        return (result) ? articleService.read(articleId) : null;
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

        splitShardedSubBloomfilterHandler.add(splitShardedSubBloomfilter, String.valueOf(articleCreateRequest.articleId()));

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
        return CacheStrategy.SPLIT_SHARDED_SUB_BLOOM_FILTER == cacheStrategy;
    }
}
