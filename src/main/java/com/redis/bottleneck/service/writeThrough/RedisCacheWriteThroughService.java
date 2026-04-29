package com.redis.bottleneck.service.writeThrough;

import com.redis.bottleneck.common.cache.service.CacheService;
import com.redis.bottleneck.common.cache.service.RedisCacheService;
import com.redis.bottleneck.common.strategy.CacheStrategy;
import com.redis.bottleneck.common.writeThrough.WriteThroughRepository;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.model.request.ArticleUpdateRequest;
import com.redis.bottleneck.model.request.ItemCreateRequest;
import com.redis.bottleneck.model.request.ItemUpdateRequest;
import com.redis.bottleneck.model.response.ArticlePageResponse;
import com.redis.bottleneck.model.response.ArticleResponse;
import com.redis.bottleneck.model.response.ItemPageResponse;
import com.redis.bottleneck.model.response.ItemResponse;
import com.redis.bottleneck.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheWriteThroughService implements RedisCacheService {

    private final ArticleService articleService;
    private final WriteThroughRepository writeThroughRepository;

    private static final String LIST_ID = "ARTICLES";
    private static final Duration TIME_TO_LIVE = Duration.ofSeconds(10);

    @Override
    public ArticleResponse read(Long articleId) {

        ArticleResponse articleResponse = writeThroughRepository.read(
                this.genId(articleId),
                ArticleResponse.class
        );

        if(articleResponse != null){
            log.info("캐싱이 진행되었습니다.");
            return articleResponse;
        }

        return articleService.read(articleId);
    }

    @Override
    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize) {

        List<ArticleResponse> list = writeThroughRepository.readAll(
            LIST_ID,
            page,
            pageSize,
            ArticleResponse.class
        );

        if(!list.isEmpty() && list.size() < pageSize){
            return articleService.readAll(boardId, page, pageSize);
        }

        if(list.isEmpty()){
            return articleService.readAll(boardId, page, pageSize);
        }

        return ArticlePageResponse.fromResponse(
                list,
                articleService.count(boardId)
        );
    }

    @Override
    public ArticlePageResponse readAllInfiniteScroll(Long boardId, Long lastArticleId, Long pageSize) {

        List<ArticleResponse> list = writeThroughRepository.readAllInfiniteScroll(
                LIST_ID,
                lastArticleId,
                pageSize,
                ArticleResponse.class
        );

        if(!list.isEmpty() && list.size() < pageSize){
            return articleService.readInfiniteScroll(boardId, lastArticleId, pageSize);
        }

        if(list.isEmpty()){
            return articleService.readInfiniteScroll(boardId, lastArticleId, pageSize);
        }

        return ArticlePageResponse.fromResponse(
                list,
                articleService.count(boardId)
        );
    }

    @Override
    public ArticleResponse create(ArticleCreateRequest articleCreateRequest) {

        ArticleResponse articleResponse = articleService.create(articleCreateRequest);

        writeThroughRepository.add(
                LIST_ID,
                this.genId(articleResponse.articleId()),
                articleResponse,
                TIME_TO_LIVE,
                articleResponse.articleId()
        );

        return articleResponse;
    }

    @Override
    public ArticleResponse update(Long articleId, ArticleUpdateRequest articleUpdateRequest) {

        ArticleResponse articleResponse = articleService.update(articleUpdateRequest.articleId(), articleUpdateRequest);

        writeThroughRepository.add(
                LIST_ID,
                this.genId(articleResponse.articleId()),
                articleResponse,
                TIME_TO_LIVE,
                articleResponse.articleId()
        );

        return articleResponse;
    }

    @Override
    public void delete(long articleId) {
        articleService.delete(articleId);
        writeThroughRepository.delete(LIST_ID, this.genId(articleId));
    }

    @Override
    public long count(long boardId) {
        return articleService.count(boardId);
    }

    @Override
    public boolean supports(CacheStrategy cacheStrategy) {
        return CacheStrategy.WRITE_THROUGH == cacheStrategy;
    }

    private String genId(Long articleId) {
        return "article:" + articleId;
    }
}
