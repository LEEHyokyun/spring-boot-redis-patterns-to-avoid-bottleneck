package com.redis.bottleneck.model.request;

public record ArticleCreateRequest(Long articleId, String data, Long boardId) {
}
