package com.redis.bottleneck.common.bloomfilter.splitShardedBloomfilter;

import com.redis.bottleneck.common.bloomfilter.splitBloomfilter.SplitBloomFilterHandler;
import com.redis.bottleneck.common.bloomfilter.splitBloomfilter.SplitBloomfilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SplitShardedBloomfilterHandler {

    /*
    * splitBloomFilterHandler를 controll 한다.
    * */
    private final SplitBloomFilterHandler splitBloomfilterHandler;

    public void init(SplitShardedBloomfilter splitShardedBloomfilter) {
        List<SplitBloomfilter> shards = splitShardedBloomfilter.getShards();
        for(SplitBloomfilter shard : shards) {
            splitBloomfilterHandler.init(shard);
        }
    }

    public void add(SplitShardedBloomfilter splitShardedBloomfilter, String value) {
        SplitBloomfilter shard = splitShardedBloomfilter.findShard(value);
        splitBloomfilterHandler.add(shard, value);
    }

    public boolean mightContain(SplitShardedBloomfilter splitShardedBloomfilter, String value) {
        SplitBloomfilter shard = splitShardedBloomfilter.findShard(value);
        return splitBloomfilterHandler.mightContain(shard, value);
    }

    public void delete(SplitShardedBloomfilter splitShardedBloomfilter, String value) {
        List<SplitBloomfilter> shards = splitShardedBloomfilter.getShards();
        for(SplitBloomfilter shard : shards) {
            splitBloomfilterHandler.delete(shard);
        }
    }
}
