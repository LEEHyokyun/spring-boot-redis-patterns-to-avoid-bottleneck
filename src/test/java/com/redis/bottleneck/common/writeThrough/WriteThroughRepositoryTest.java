package com.redis.bottleneck.common.writeThrough;

import com.redis.bottleneck.common.detailCaches.writeThrough.WriteThroughRepository;
import com.redis.bottleneck.utils.RedisTestContainerSupportUtil;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.util.List;
import java.util.stream.LongStream;

@DataRedisTest
@Import(WriteThroughRepository.class)
class WriteThroughRepositoryTest extends RedisTestContainerSupportUtil {

    @Autowired
    private WriteThroughRepository writeThroughRepository;

    @Test
    void addAndDelFuncTest(){
        //given
        long articleId = 1L;
        long boardId = 1L;
        String articleListId = "article";

        TempEntity tempEntity = TempEntity.create(
                articleId,
                "data 1",
                boardId
        );


        writeThroughRepository.add(
            articleListId,
            String.valueOf(articleId),
            tempEntity,
            Duration.ofSeconds(10),
            articleId
        );

        TempEntity readed = writeThroughRepository.read(String.valueOf(tempEntity.getArticleId()), TempEntity.class);

        Assertions.assertEquals(articleId, readed.getArticleId());
        Assertions.assertEquals(boardId, readed.getBoardId());
        Assertions.assertEquals("data 1", readed.getData());

        writeThroughRepository.delete(articleListId, String.valueOf(readed.getArticleId()));

        TempEntity readedAfterDel =  writeThroughRepository.read(String.valueOf(tempEntity.getArticleId()), TempEntity.class);

        Assertions.assertNull(readedAfterDel);
    }

    @Test
    void readAllTest(){
        //given
        long boardId = 1L;
        String tempListId = "tempList";

        List<TempEntity> list = LongStream.range(0, 120)
                .mapToObj(i -> TempEntity.create(
                        i,
                        "data " + i,
                        boardId
                ))
                .toList();

        for(TempEntity tempEntity : list){
            writeThroughRepository.add(
                    tempListId,
                    String.valueOf(tempEntity.getArticleId()),
                    tempEntity,
                    Duration.ofSeconds(10),
                    tempEntity.getArticleId()
            );
        }

        //when
        List<TempEntity> list1 = writeThroughRepository.readAll(tempListId, 1, 40, TempEntity.class);
        List<TempEntity> list2 = writeThroughRepository.readAll(tempListId, 2, 40, TempEntity.class);
        List<TempEntity> list3 = writeThroughRepository.readAll(tempListId, 3, 40, TempEntity.class);

        //then
        Assertions.assertEquals(40, list1.size());
        Assertions.assertEquals(40, list2.size());
        Assertions.assertEquals(20, list3.size());

        Assertions.assertEquals(119, list1.getFirst().getArticleId());
        Assertions.assertEquals(79, list2.getFirst().getArticleId());
        Assertions.assertEquals(39, list3.getFirst().getArticleId());

    }

    @Test
    void readAllInfiniteScrollTest(){
        //given
        long boardId = 1L;
        String tempListId = "tempList";

        List<TempEntity> list = LongStream.range(0, 120)
                .mapToObj(i -> TempEntity.create(
                        i,
                        "data " + i,
                        boardId
                ))
                .toList();

        for(TempEntity tempEntity : list){
            writeThroughRepository.add(
                    tempListId,
                    String.valueOf(tempEntity.getArticleId()),
                    tempEntity,
                    Duration.ofSeconds(10),
                    tempEntity.getArticleId()
            );
        }

        //when
        List<TempEntity> list1 = writeThroughRepository.readAllInfiniteScroll(tempListId, null, 40, TempEntity.class);
        List<TempEntity> list2 = writeThroughRepository.readAllInfiniteScroll(tempListId, list1.getLast().getArticleId(), 40, TempEntity.class);
        List<TempEntity> list3 = writeThroughRepository.readAllInfiniteScroll(tempListId, list2.getLast().getArticleId(), 40, TempEntity.class);

        //then
        Assertions.assertEquals(40, list1.size());
        Assertions.assertEquals(40, list2.size());
        Assertions.assertEquals(20, list3.size());

        Assertions.assertEquals(119, list1.getFirst().getArticleId());
        Assertions.assertEquals(79, list2.getFirst().getArticleId());
        Assertions.assertEquals(39, list3.getFirst().getArticleId());

    }

    private String genKey(String id){
        return "WRITE_THROUGH_:" + id;
    }

    private String genListKey(String listId){
        return "WRITE_THROUGH_LIST_:" + listId;
    }

    @Getter
    private static class TempEntity {
        private Long articleId;
        private String data;
        private Long boardId;

        public static TempEntity create(long articleId, String data, long boardId) {

            TempEntity entity = new TempEntity();

            entity.articleId = articleId;
            entity.data = data;
            entity.boardId = boardId;

            return entity;
        }
    }
}