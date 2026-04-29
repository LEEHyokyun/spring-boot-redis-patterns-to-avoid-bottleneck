package com.redis.bottleneck.common.detailCaches.per;

import com.redis.bottleneck.common.serde.DataSerializer;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.time.Instant;
import java.util.random.RandomGenerator;

@Getter
@ToString
public class PERModel {
    private String data;
    private long computationTimeMillis;
    private long expiredTimeMillis;

    public static PERModel of(Object data, long computationTimeMillis, Duration ttl) {
        PERModel perModel = new PERModel();

        perModel.data = DataSerializer.serializeOrException(data);
        perModel.computationTimeMillis = computationTimeMillis;
        perModel.expiredTimeMillis = Instant.now().plus(ttl).toEpochMilli();

        return perModel;
    }

    public <T> T deserializeModelData(Class<T> clazz) {
        return DataSerializer.deserializeOrNull(data, clazz);
    }

    public boolean isInChargeOfRecomputation(double beta){
        long nowMillis = Instant.now().toEpochMilli();
        double rand = RandomGenerator.getDefault().nextDouble();

        /*
        * 확률에 기반하여 갱신 책임을 얻고, 갱신 수행
        * */
        return nowMillis - computationTimeMillis * beta * Math.log(rand) >= expiredTimeMillis;
    }
}
