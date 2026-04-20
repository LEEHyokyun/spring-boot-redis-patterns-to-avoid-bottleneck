package com.redis.bottleneck.model.response;

import com.redis.bottleneck.model.domain.Article;

public record ArticleResponse(
        Long articleId,
        String data,
        Long boardId
) {

    /*
     * for null caching
     * - No data ? privacy ? deleted ?
     * */
    private static final ArticleResponse nullObject = new ArticleResponse(null, null, null);

    public static ArticleResponse from(Article article){

        return (article == null) ? nullObject : new ArticleResponse(article.getArticleId(), article.getData(), article.getBoardId());
    }
}
