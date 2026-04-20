package com.redis.bottleneck.model.domain;

import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.model.request.ArticleUpdateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Table(name = "article")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long articleId;
    private String data;
    private Long boardId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Article create(ArticleCreateRequest articleCreateRequest) {
        Article article = new Article();

        article.data = articleCreateRequest.data();
        article.boardId = articleCreateRequest.boardId();
        article.createdAt = LocalDateTime.now();
        article.updatedAt = LocalDateTime.now();

        return article;
    }

    public void update(ArticleUpdateRequest articleUpdateRequest) {
        this.data = articleUpdateRequest.data();
        this.boardId = articleUpdateRequest.boardId();
    }
}
