package com.redis.bottleneck.model.request;

public record ArticleUpdateRequest(Long articleId, String data, Long boardId) {
}
