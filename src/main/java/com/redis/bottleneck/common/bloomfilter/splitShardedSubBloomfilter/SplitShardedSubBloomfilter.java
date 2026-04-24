package com.redis.bottleneck.common.bloomfilter.splitShardedSubBloomfilter;

import com.redis.bottleneck.common.bloomfilter.splitShardedBloomfilter.SplitShardedBloomfilter;
import jakarta.persistence.Access;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SplitShardedSubBloomfilter {
    private String id;
    private SplitShardedBloomfilter splitShardedBloomfilter;

    public static SplitShardedSubBloomfilter create(String id, long dataCount, double falsePositive, int shardCount){
        SplitShardedSubBloomfilter splitShardedSubBloomfilter = new SplitShardedSubBloomfilter();

        splitShardedSubBloomfilter.id = id;
        splitShardedSubBloomfilter.splitShardedBloomfilter = SplitShardedBloomfilter.create(
                id, dataCount, falsePositive, shardCount
        );

        return splitShardedSubBloomfilter;
    }

    public SplitShardedBloomfilter findSubFilter(int subFilterIndex){
        return SplitShardedBloomfilter.create(
                id + ":sub-filter:" + subFilterIndex,
                splitShardedBloomfilter.getDataCount() * (1L << (subFilterIndex + 1)), //기존 filter의 2배
                splitShardedBloomfilter.getFalsePositiveRate() / (1L << (subFilterIndex + 1)), //오차율 0.5
                splitShardedBloomfilter.getShardCount()
        );
    }

    public SplitShardedBloomfilter findActivatedFilter(int subFilterCount){
        if(subFilterCount == 0) return this.splitShardedBloomfilter;
        else {
            int activatedFilterIndex = subFilterCount - 1;
            return this.findSubFilter(activatedFilterIndex);
        }
    }

    public List<SplitShardedBloomfilter> findAll(int subFilterCount){
        List<SplitShardedBloomfilter> splitShardedBloomfilters = new ArrayList<>();

        /*
        * 원본 + 나머지
        * */
        splitShardedBloomfilters.add(this.splitShardedBloomfilter);
        for(int i = 0 ; i < subFilterCount; i++){
            splitShardedBloomfilters.add(this.findActivatedFilter(i+1));
        }

        return splitShardedBloomfilters;
    }

}
