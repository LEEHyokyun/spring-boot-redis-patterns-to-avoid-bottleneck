package com.redis.bottleneck.service.cache;

import com.redis.bottleneck.common.bloomfilter.splitBloomfilter.SplitBloomFilterHandler;
import com.redis.bottleneck.common.bloomfilter.splitBloomfilter.SplitBloomfilter;
import com.redis.bottleneck.common.bloomfilter.splitShardedBloomfilter.SplitShardedBloomfilter;
import com.redis.bottleneck.common.bloomfilter.splitShardedBloomfilter.SplitShardedBloomfilterHandler;
import com.redis.bottleneck.common.cache.service.RedisCacheService;
import com.redis.bottleneck.common.cache.strategy.CacheStrategy;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.model.request.ArticleUpdateRequest;
import com.redis.bottleneck.model.response.ArticlePageResponse;
import com.redis.bottleneck.model.response.ArticleResponse;
import com.redis.bottleneck.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisCachePenetrationShardedSplitBloomFilterService implements RedisCacheService {

    private final ArticleService articleService;
    private final SplitShardedBloomfilterHandler SplitShardedBloomfilterHandler;

    private static final SplitShardedBloomfilter splitShardedBloomfilter = SplitShardedBloomfilter.create(
            "split-sharded-bloom-filter:article",
            1000,
            0.01,
            4
    );

    @Override
    public ArticleResponse read(Long articleId) {

        boolean result = SplitShardedBloomfilterHandler.mightContain(splitShardedBloomfilter, String.valueOf(articleId));

        return (!result) ? null : articleService.read(articleId);
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

        ArticleResponse articleResponse = articleService.create(articleCreateRequest);
        SplitShardedBloomfilterHandler.add(splitShardedBloomfilter, String.valueOf(articleResponse.articleId()));

        return articleResponse;
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
        return CacheStrategy.SPLIT_BLOOM_FILTER == cacheStrategy;
    }

}
