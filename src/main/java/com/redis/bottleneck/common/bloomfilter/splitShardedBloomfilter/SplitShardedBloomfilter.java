package com.redis.bottleneck.common.bloomfilter.splitShardedBloomfilter;

import com.google.common.hash.Hashing;
import com.redis.bottleneck.common.bloomfilter.splitBloomfilter.SplitBloomfilter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SplitShardedBloomfilter {
    private String id;
    private long dataCount;
    private double falsePositiveRate;
    private List<SplitBloomfilter> shards;
    private int shardCount;

    public static SplitShardedBloomfilter create(String id, long dataCount, double falsePositiveRate, int shardCount) {
        SplitShardedBloomfilter splitShardedBloomfilter = new SplitShardedBloomfilter();

        splitShardedBloomfilter.id = id;
        splitShardedBloomfilter.dataCount = dataCount;
        splitShardedBloomfilter.falsePositiveRate = falsePositiveRate;
        splitShardedBloomfilter.shards = createShards(id, dataCount, falsePositiveRate, shardCount);
        splitShardedBloomfilter.shardCount = shardCount;

        return splitShardedBloomfilter;
    }

    private static List<SplitBloomfilter> createShards(String id, long dataCount, double falsePositiveRate, int shardCount) {

        /*
         * 각 shard에 몇개의 data를 넣겠는가
         * dataCount = 1000개,
         * shard Count = 3개라면,
         * 각 샤드에 들어가는 데이터 수는 1000/3 = 333개, 나머지 1개.
         *
         * shard Index = 0 -> id:shard:0으로 생성, 333개 + 1
         * shard Index = 1 -> id:shard:1으로 생성, 333개 + 0
         * shard Index = 2 -> id:shard:2으로 생성, 333개 + 0
        */
        long dataChunkCount = dataCount / shardCount;
        long remainder = dataCount % shardCount;

        //shard create
        List<SplitBloomfilter> shards = new ArrayList<>();
        for(int shardIndex = 0 ; shardIndex < shardCount ; shardIndex++){
            SplitBloomfilter splitBloomfilter = SplitBloomfilter.create(
                    id + ":shard:" + shardIndex,
                    dataChunkCount + ((shardIndex < remainder) ? 1 : 0), //remainder를 하나씩 분배하여 각 shard에 dataCount 적용
                    falsePositiveRate
            );
            shards.add(splitBloomfilter);
        }
        return shards;
    }

    /*
    * value -> 어떤 shard?
    * */
    public SplitBloomfilter findShard(String value){
        return shards.get(this.findShardIndex(value));
    }

    /*
    * findShardIndex
    * */
    private int findShardIndex(String value){
        return Math.abs(Hashing.murmur3_32_fixed()
                .hashString(value, StandardCharsets.UTF_8)
                .asInt() % shardCount
        )
        ;
    }
}
