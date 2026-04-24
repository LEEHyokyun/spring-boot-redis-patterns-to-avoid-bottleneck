package com.redis.bottleneck.common.bloomfilter.splitShardedSubBloomfilter.distributedLock;

import com.redis.bottleneck.utils.RedisTestContainerSupportUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DataRedisTest
@Import(DistributedLockProvider.class)
class DistributedLockProviderTest extends RedisTestContainerSupportUtil {

    @Autowired
    private DistributedLockProvider distributedLockProvider;

    @Test
    void lockTest() throws InterruptedException {
        Assertions.assertTrue(distributedLockProvider.lock("testId", Duration.ofSeconds(1)));
        Assertions.assertFalse(distributedLockProvider.lock("testId", Duration.ofSeconds(1)));

        TimeUnit.SECONDS.sleep(2);

        Assertions.assertTrue(distributedLockProvider.lock("testId", Duration.ofSeconds(1)));
    }

    @Test
    void DistributedLockTestForRaceCondition() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10); //1WAS, 10개의 동시 요청을 가정
        CountDownLatch latch = new CountDownLatch(10);
        AtomicInteger lockCounted = new AtomicInteger(0);

        for(int i = 0 ; i < 10 ; i++){
            executorService.execute(()->{ //10개의 동시 요청 = 분산락 획득에 대한 경쟁 상태를 가정
                Boolean isLocked = distributedLockProvider.lock("testId", Duration.ofSeconds(10));
                if(isLocked){
                    lockCounted.incrementAndGet();
                }
                latch.countDown(); //count = 0 -> pass(기준 : 모든 스레드가 latch countDown을 호출).
            });
        }

        latch.await(); //모든 thread의 처리가 완료될때까지 await.

        Assertions.assertEquals(1, lockCounted.get());
    }

    @Test
    void unLockTest(){
        //given
        distributedLockProvider.lock("testId", Duration.ofSeconds(1));

        //then
        Assertions.assertFalse(distributedLockProvider.lock("testId", Duration.ofSeconds(1)));

        //when
        distributedLockProvider.unlock("testId");

        //then
        Assertions.assertTrue(distributedLockProvider.lock("testId", Duration.ofSeconds(1)));
    }
}