package com.redis.bottleneck.utils;

import com.redis.bottleneck.common.cache.aop.CacheStrategy;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.model.request.ArticleUpdateRequest;
import com.redis.bottleneck.model.request.ItemCreateRequest;
import com.redis.bottleneck.model.request.ItemUpdateRequest;
import com.redis.bottleneck.model.response.ArticlePageResponse;
import com.redis.bottleneck.model.response.ArticleResponse;
import com.redis.bottleneck.model.response.ItemPageResponse;
import com.redis.bottleneck.model.response.ItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

//for spring boot test
@RequiredArgsConstructor
public class ArticleEndPointCallUtil {

    //client
    private static RestClient restClient = RestClient.create("http://localhost:8080");

    public static ArticleResponse read(CacheStrategy cacheStrategy, Long articleId){
        return restClient.get()
                .uri("/cache-strategy/%s/articles/%s".formatted(cacheStrategy.name(), articleId))
                .retrieve()
                .body(ArticleResponse.class)
                ;
    }

    public static ArticlePageResponse readAll(CacheStrategy cacheStrategy, Long page, Long pageSize){
        return restClient.get()
                .uri("/cache-strategy/%s/articles?boardId=%s&page=%s&pageSize=%s".formatted(cacheStrategy.name(), 1L, page, pageSize))
                .retrieve()
                .body(ArticlePageResponse.class)
                ;
    }

    public static ArticlePageResponse readAllInfiniteScroll(CacheStrategy cacheStrategy, Long lastArticleId, Long pageSize){
        return restClient.get()
                .uri(
                        (lastArticleId) == null ?
                            "/cache-strategy/%s/articles/infinite-scroll?boardId=%s&pageSize=%s".formatted(cacheStrategy.name(), 1L, pageSize)
                            :
                            "/cache-strategy/%s/articles/infinite-scroll?boardId=%s&lastArticleId=%s&pageSize=%s".formatted(cacheStrategy.name(), 1L, lastArticleId, pageSize))
                .retrieve()
                .body(ArticlePageResponse.class)
                ;
    }

    public static ArticleResponse create(CacheStrategy cacheStrategy, ArticleCreateRequest articleCreateRequest){
        return restClient.post()
                .uri("/cache-strategy/%s/articles".formatted(cacheStrategy.name()))
                .body(articleCreateRequest)
                .retrieve()
                .body(ArticleResponse.class)
                ;
    }

    public static ArticleResponse update(CacheStrategy cacheStrategy, Long articleId, ArticleUpdateRequest articleUpdateRequest) {
        return restClient.post()
                .uri("/cache-strategy/%s/articles/%s".formatted(cacheStrategy.name(), articleId))
                .body(articleUpdateRequest)
                .retrieve()
                .body(ArticleResponse.class)
                ;
    }

    public static void delete(CacheStrategy cacheStrategy, Long articleId){
        restClient.delete()
                .uri("/cache-strategy/%s/articles/%s".formatted(cacheStrategy.name(), articleId))
                .retrieve()
                .toBodilessEntity()
                ;
    }

    public static long count(CacheStrategy cacheStrategy, long boardId){
        return restClient.get()
                .uri("cache-strategy/%s/articleCount/%s".formatted(cacheStrategy.name(), boardId))
                .retrieve()
                .body(Long.class)
                ;
    }
}
