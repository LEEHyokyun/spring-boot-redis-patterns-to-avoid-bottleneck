package com.redis.bottleneck.service;

import com.redis.bottleneck.model.domain.Article;
import com.redis.bottleneck.model.request.ArticleCreateRequest;
import com.redis.bottleneck.model.request.ArticleUpdateRequest;
import com.redis.bottleneck.model.response.ArticlePageResponse;
import com.redis.bottleneck.model.response.ArticleResponse;
import com.redis.bottleneck.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;

    public ArticleResponse read(Long articleId){
        return ArticleResponse.from(
                articleRepository.findById(articleId)
                        .orElseThrow(
                                () -> new RuntimeException("Articles not Found")
                        )
        );
    }

    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize){
        return ArticlePageResponse.from(
                articleRepository.readAll(boardId, (page - 1) * pageSize, pageSize),
                articleRepository.count()
        );
    }

    public ArticlePageResponse readInfiniteScroll(Long boardId, Long lastArticleId, Long pageSize){
        return  (lastArticleId == null) ?
                ArticlePageResponse.from(
                    articleRepository.readAllInfiniteScroll(boardId, pageSize),
                    articleRepository.count()
                )

                :

                ArticlePageResponse.from(
                    articleRepository.readAllInfiniteScroll(boardId, pageSize, lastArticleId),
                    articleRepository.count()
                )
                ;
    }

    @Transactional
    public ArticleResponse create(ArticleCreateRequest articleCreateRequest){
        return ArticleResponse.from(
                articleRepository.save(Article.create(articleCreateRequest))
        );
    }

    @Transactional
    public ArticleResponse update(Long articleId, ArticleUpdateRequest articleUpdateRequest){
        Article article = articleRepository.findById(articleId).orElse(null);

        article.update(articleUpdateRequest);

        return ArticleResponse.from(
                articleRepository.save(article)
        );
    }

    @Transactional
    public void delete(long articleId){
        articleRepository.findById(articleId).ifPresent(
                article -> articleRepository.delete(article)
        );
    }

    public long count(){
        return articleRepository.count();
    }
}
