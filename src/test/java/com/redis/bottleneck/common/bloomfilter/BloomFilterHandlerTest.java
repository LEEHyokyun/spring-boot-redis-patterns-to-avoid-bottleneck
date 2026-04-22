package com.redis.bottleneck.common.bloomfilter;

import com.redis.bottleneck.utils.RedisTestContainerSupportUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataRedisTest
@Import(BloomFilterHandler.class)
@Slf4j
class BloomFilterHandlerTest extends RedisTestContainerSupportUtil {

    @Autowired
    private BloomFilterHandler bloomFilterHandler;

    @Test
    void add() {
        //given
        BloomFilter bloomFilter = BloomFilter.create("testId", 1000, 0.01);

        //when
        bloomFilterHandler.add(bloomFilter, "value");

        //then
        List<Long> hashedIndices = bloomFilter.hash("value");
        for(long offset = 0 ; offset < bloomFilter.getBitSize() ; offset++){
            //offset 위치의 index 값
            Boolean result = stringRedisTemplate.opsForValue().getBit("bloom-filter:" + bloomFilter.getId(), offset);
            assertThat(result).isEqualTo(hashedIndices.contains(offset));
        }
    }

    @Test
    void mightContain() {
        //given
        BloomFilter bloomFilter = BloomFilter.create("testId", 1000, 0.01);
        List<String> values = IntStream.range(0, 1000).mapToObj(idx -> "value" + idx).toList(); //1000개 데이터에 대한 bloom filter List

        //when
        for(int i = 0 ; i < values.size() ; i++){
            bloomFilterHandler.add(bloomFilter, values.get(i));
        }

        //then - bloom filter에 추가 후 탐색 시 true
        for(String value : values){
            assertThat(bloomFilterHandler.mightContain(bloomFilter, value)).isTrue();
        }

        /*
        * 추가 test
        * */
        //false positive
        //현재는 데이터 1000개 추가한 상태, 탐색 범위가 넓어지면 false positive 오차 확률 증가
        int realFalseCount = 0;
        for(int i = values.size() ; i < values.size() + 100 ; i++){
            if(bloomFilterHandler.mightContain(bloomFilter, "value" + i)) realFalseCount++;
        }
        log.info("Data Look up 개수가 많아지면 오차 확률은 증가 한다 [False Positive 개수 : {}]", realFalseCount);
    }

    @Test
    void delete() {
        //given
        BloomFilter bloomFilter = BloomFilter.create("testId", 1000, 0.01);
        bloomFilterHandler.add(bloomFilter, "value");

        //when
        bloomFilterHandler.delete(bloomFilter);

        //then
        for(long offset = 0 ; offset < bloomFilter.getBitSize() ; offset++){
            Boolean result = stringRedisTemplate.opsForValue().getBit("bloom-filter:" + bloomFilter.getId(), offset);
            assertThat(result).isEqualTo(false);
        }
    }

    @Test
    void requiredSpentTimeTest(){
        //given (data 4억개)
        BloomFilter bloomFilter = BloomFilter.create("testId", 400_000_000, 0.01);
        List<Long> hashedIndices = bloomFilter.hash("value");
        log.info("사용 bit size : {}", bloomFilter.getBitSize());


        long start = System.nanoTime();
        bloomFilterHandler.add(bloomFilter, "value");

        //when
        long timeMillis = Duration.ofNanos(System.nanoTime() - start).toMillis();
        log.info("소요 시간 : {}", timeMillis);

    }
}