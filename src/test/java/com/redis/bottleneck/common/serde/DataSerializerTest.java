package com.redis.bottleneck.common.serde;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class DataSerializerTest {
    @Test
    void serde(){
        RedisTempData data = new RedisTempData("id", "data");
        String serialized = DataSerializer.serializeOrException(data);
        RedisTempData deserialized = DataSerializer.deserializeOrNull(serialized, RedisTempData.class);

        Assertions.assertThat(deserialized).isEqualTo(data);
    }

    record RedisTempData(
        String id,
        String data
    ){
    }
}
