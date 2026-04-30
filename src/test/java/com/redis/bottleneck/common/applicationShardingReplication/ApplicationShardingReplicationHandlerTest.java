package com.redis.bottleneck.common.applicationShardingReplication;

import com.redis.bottleneck.common.detailCaches.applicationShardingReplication.ApplicationShardingReplicationHandler;
import com.redis.bottleneck.common.detailCaches.applicationShardingReplication.ShardedKeyGenerator;
import com.redis.bottleneck.common.serde.DataSerializer;
import com.redis.bottleneck.utils.RedisTestContainerSupportUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DataRedisTest
@Import(
        {
                ApplicationShardingReplicationHandler.class,
                ShardedKeyGenerator.class
        }
)
public class ApplicationShardingReplicationHandlerTest extends RedisTestContainerSupportUtil {

    @Autowired
    private ApplicationShardingReplicationHandler applicationShardingReplicationHandler;

    @Autowired
    private  ShardedKeyGenerator shardedKeyGenerator;

    @Test
    void putTest(){
        //given
        String key = "testKey";
        String data = "test data";
        int shardCount = 3;

        //when
        applicationShardingReplicationHandler.put(key, Duration.ofSeconds(10), data);

        //then
        Assertions.assertEquals("test data", DataSerializer.deserializeOrNull(stringRedisTemplate.opsForValue().get(key + ":" + 0), String.class));
        Assertions.assertEquals("test data", DataSerializer.deserializeOrNull(stringRedisTemplate.opsForValue().get(key + ":" + 1), String.class));
        Assertions.assertEquals("test data", DataSerializer.deserializeOrNull(stringRedisTemplate.opsForValue().get(key + ":" + 2), String.class));
        Assertions.assertEquals("test data", DataSerializer.deserializeOrNull(stringRedisTemplate.opsForValue().get(shardedKeyGenerator.findRandomShardedKey(key, shardCount)), String.class));
    }

    @Test
    void evictTest(){
        //given
        String key = "testKey";
        String data = "test data";
        int shardCount = 3;

        //when
        applicationShardingReplicationHandler.put(key, Duration.ofSeconds(10), data);

        //then
        Assertions.assertEquals("test data", DataSerializer.deserializeOrNull(stringRedisTemplate.opsForValue().get(key + ":" + 0), String.class));
        Assertions.assertEquals("test data", DataSerializer.deserializeOrNull(stringRedisTemplate.opsForValue().get(key + ":" + 1), String.class));
        Assertions.assertEquals("test data", DataSerializer.deserializeOrNull(stringRedisTemplate.opsForValue().get(key + ":" + 2), String.class));
        Assertions.assertEquals("test data", DataSerializer.deserializeOrNull(stringRedisTemplate.opsForValue().get(shardedKeyGenerator.findRandomShardedKey(key, shardCount)), String.class));

        //when
        applicationShardingReplicationHandler.evict(key);
        Assertions.assertNull(DataSerializer.deserializeOrNull(stringRedisTemplate.opsForValue().get(key + ":" + 0), String.class));
        Assertions.assertNull(DataSerializer.deserializeOrNull(stringRedisTemplate.opsForValue().get(key + ":" + 1), String.class));
        Assertions.assertNull(DataSerializer.deserializeOrNull(stringRedisTemplate.opsForValue().get(key + ":" + 2), String.class));
        Assertions.assertNull(DataSerializer.deserializeOrNull(stringRedisTemplate.opsForValue().get(shardedKeyGenerator.findRandomShardedKey(key, shardCount)), String.class));
    }

    @Test
    void fetchTest() throws InterruptedException {
        //given
        String key = "testKey";
        int shardCount = 3;

        //stub
        AtomicInteger count = new AtomicInteger(0);
        Supplier<String> supplier = Mockito.mock(Supplier.class);
        when(supplier.get()).thenAnswer(invocation -> {
            count.incrementAndGet();
            return "original data";
        });

        //when
        for(int i = 0 ; i < 10 ; i++){
                String result = applicationShardingReplicationHandler.fetch(
                        key,
                        Duration.ofSeconds(10),
                        () -> supplier.get(),
                        String.class

                );
                Assertions.assertEquals("original data", result);
        };

        //then
        for(int i = 0 ; i < shardCount ; i++){
            String result2 = DataSerializer.deserializeOrNull(stringRedisTemplate.opsForValue().get(key + ":" + i), String.class);
            Assertions.assertEquals("original data", result2);
        }

        assertThat(count.get()).isEqualTo(1);
        verify(supplier, times(1)).get();

    }
}
