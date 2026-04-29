package com.redis.bottleneck.common.writeThrough;

import com.redis.bottleneck.common.serde.DataSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class WriteThroughRepository {
    private final StringRedisTemplate stringRedisTemplate;
    private static final long LIST_LIMIT = 100;

    public void add(String listId, String id, Object data, Duration ttl, long score){
        stringRedisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection con = (StringRedisConnection) action;

            String key = this.genKey(id);
            String listKey = this.genListKey(listId);

            /*
            * 단건 삽입 후 리스트 삽입
            * 단건 캐싱 후에 리스트 반영하여 정합성 맞추기
            * */

            /*
            * 단건 데이터에 대한 caching (upsert)
            * 항상 최신화
            * */
            con.set(
                    key,
                    DataSerializer.serializeOrException(data),
                    Expiration.from(ttl),
                    RedisStringCommands.SetOption.UPSERT
            );

            /*
            * hot data에 대한 caching 및 최신 상태 유지/정렬
            * */
            con.zAdd(listKey, score, id);
            con.zRemRange(listKey, 0, -LIST_LIMIT - 1); //최신 100건의 hot data에 대해서만 유지

            return null;
        });
    }

    public void delete(String listId, String id){
        stringRedisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection con = (StringRedisConnection) action;

            /*
            * 정합성을 위해 리스트 삭제 후 단건 삭제
            * */
            con.zRem(this.genListKey(listId), id);
            con.del(this.genKey(id));

            return null;
        });
    }

    public <T> T read(String id, Class<T> clazz){
        String result = stringRedisTemplate.opsForValue().get(this.genKey(id));

        if(result == null) return null;

        return DataSerializer.deserializeOrNull(result, clazz);
    }

    public <T> List<T> readAll(String listId, long page, long pageSize, Class<T> clazz){
        long offset = (page - 1) * pageSize;

        /*
        * sorted Set 역순 검색
        * */
        return stringRedisTemplate.opsForZSet()
                .reverseRange(this.genListKey(listId), offset, offset + pageSize - 1).stream()
                .map(id -> read(id, clazz)) //sorted set의 id list를 역직렬화된 data list로 formatting.
                .toList();
    }

    public <T> List<T> readAllInfiniteScroll(String listId, Long lastScore, long pageSize, Class<T> clazz){
        /*
        * Redis 무한 스크롤 = Cursor 기반의 동작
        * - lastArticleId = lastScore = 마지막으로 탐색한 내역 중 가장 작은 항목이 cursor 지점.
        * - 첫 요청 : max ~ min : pageSize 만큼.
        * - 이후 요청 : lastScore -1 ~ min : pageSize 만큼.
        * */
        double min = Double.NEGATIVE_INFINITY;
        double max = (lastScore != null) ? lastScore - 1 : Double.POSITIVE_INFINITY;

        return stringRedisTemplate.opsForZSet()
                .reverseRangeByScore(this.genListKey(listId), min, max, 0, pageSize).stream()
                .map(id -> read(id, clazz))
                .toList();
    }

    private String genKey(String id){
        return "WRITE_THROUGH_:" + id;
    }

    private String genListKey(String listId){
        return "WRITE_THROUGH_LIST_:" + listId;
    }
}
