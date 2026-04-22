package com.redis.bottleneck.service.cache;

import com.redis.bottleneck.common.bloomfilter.BloomFilter;
import com.redis.bottleneck.common.bloomfilter.BloomFilterHandler;
import com.redis.bottleneck.common.cache.aop.CacheStrategy;
import com.redis.bottleneck.common.cache.service.RedisCacheService;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.model.request.ArticleUpdateRequest;
import com.redis.bottleneck.model.response.ArticlePageResponse;
import com.redis.bottleneck.model.response.ArticleResponse;
import com.redis.bottleneck.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisCachePenetrationBloomFilterService implements RedisCacheService {

    private final ArticleService articleService;

    private final BloomFilterHandler bloomFilterHandler;
    private static final BloomFilter bloomFilter = BloomFilter.create(
            "article", 1000, 0.01
    );

    @Override
    public ArticleResponse read(Long articleId) {

        boolean result = bloomFilterHandler.mightContain(bloomFilter, String.valueOf(articleId));

        if(!result){
            return null;
        }

        //bloom filter false positive -> service 호출
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

        bloomFilterHandler.add(bloomFilter, String.valueOf(articleCreateRequest.articleId()));

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
        return CacheStrategy.BLOOM_FILTER == cacheStrategy;
    }
}
