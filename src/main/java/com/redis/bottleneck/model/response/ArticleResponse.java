package com.redis.bottleneck.model.response;

import com.redis.bottleneck.model.domain.Article;

public record ArticleResponse(
        Long itemId,
        String data,
        Long boardId
) {
    public static ArticleResponse from(Article article){
        return new ArticleResponse(article.getArticleId(), article.getData(), article.getBoardId());
    }
}
