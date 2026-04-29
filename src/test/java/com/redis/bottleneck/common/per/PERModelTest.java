package com.redis.bottleneck.common.per;

import com.redis.bottleneck.common.detailCaches.per.PERModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class PERModelTest {

    @Test
    void parsePERModelTest(){

        //given / when
        PERModel perModel = PERModel.of(
                1234L,
                1000L,
                Duration.ofSeconds(10)
        );

        //then
        Assertions.assertEquals("1234", perModel.getData());
        Assertions.assertEquals(1234L, perModel.deserializeModelData(Long.class));
    }

    @Test
    void recomputationTest() throws InterruptedException {
        //given / when
        PERModel perModel = PERModel.of(
                1234L,
                1000L,
                Duration.ofSeconds(3)
        );

        //then
        int count = 0;
        for(int i = 0 ; i < 30 ; i++){
            boolean result = perModel.isInChargeOfRecomputation(1);

            TimeUnit.MILLISECONDS.sleep(100);
            log.info("result = " + result);

            if(result) count++;
        }

        assertThat(count).isGreaterThan(3);
    }
}