package com.redis.bottleneck.repository;

import com.redis.bottleneck.model.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query(
        value = "   select a.article_id, a.data, a.board_id, a.created_at, a.updated_at " +
                "   from article a " +
                "   where a.board_id = :boardId " +
                "   order by a.article_id desc " +
                "   limit :limit offset :offset ",
        nativeQuery = true
    )
    List<Article> findAll(
        @Param("boardId") Long boardId,
        @Param("offset") Long offset,
        @Param("limit") Long limit
    );

    @Query(
            value = "   select count(*) from (" +
                    "       select a.article_id from article a where board_id = :boardId order by article_id desc limit :limit" +
                    "   ) t",
            nativeQuery = true
    )
    Long count(@Param("boardId") Long boardId, @Param("limit") Long limit);

    @Query(
            value = "   select a.article_id, a.data, a.board_id, a.created_at, a.updated_at " +
                    "   from article a "+
                    "   where board_id = :boardId "+
                    "   order by article_id desc " +
                    "   limit :limit ",
            nativeQuery = true
    )
    List<Article> findAllInfiniteScroll(
            @Param("boardId") Long boardId,
            @Param("limit") Long limit
    );

    @Query(
            value = "   select a.article_id, a.data, a.board_id, a.created_at, a.updated_at " +
                    "   from article a"+
                    "   where board_id = :boardId " +
                    "   and article_id < :lastArticleId " +
                    "   order by article_id desc " +
                    "   limit :limit ",
            nativeQuery = true
    )
    List<Article> findAllInfiniteScroll(
            @Param("boardId") Long boardId,
            @Param("limit") Long limit,
            @Param("lastArticleId") Long lastArticleId
    );
}
