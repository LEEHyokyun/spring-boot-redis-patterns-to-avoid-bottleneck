package com.redis.bottleneck.model.response;

import com.redis.bottleneck.model.domain.Article;

import java.util.List;

public record ArticlePageResponse(
        List<ArticleResponse> articles,
        long count
){
    public static ArticlePageResponse fromResponse(List<ArticleResponse> articles, long count){
        return new ArticlePageResponse(articles, count);
    }

    public static ArticlePageResponse from(List<Article> articles, long count){
        return fromResponse(articles.stream().map(ArticleResponse::from).toList(), count);
    }
}
