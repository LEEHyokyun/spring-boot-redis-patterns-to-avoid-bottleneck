package com.redis.bottleneck.common.bloomfilter.splitShardedSubBloomfilter;

import com.redis.bottleneck.common.bloomfilter.splitShardedBloomfilter.SplitShardedBloomfilter;
import com.redis.bottleneck.common.bloomfilter.splitShardedBloomfilter.SplitShardedBloomfilterHandler;
import com.redis.bottleneck.common.bloomfilter.splitShardedSubBloomfilter.distributedLock.DistributedLockProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SplitShardedSubBloomfilterHandler {
    private final StringRedisTemplate stringRedisTemplate;
    private final DistributedLockProvider distributedLockProvider;
    private final SplitShardedBloomfilterHandler splitShardedBloomfilterHandler;

    public static final int MAX_SUB_FILTER_COUNT = 2;
    private final RedisTemplate<Object, Object> redisTemplate;

    //원본 필터 초기화
    public void init(SplitShardedSubBloomfilter splitShardedSubBloomfilter) {
        SplitShardedBloomfilter splitShardedBloomfilter = splitShardedSubBloomfilter.getSplitShardedBloomfilter();
        splitShardedBloomfilterHandler.init(splitShardedBloomfilter);
    }

    //데이터 반영 + 데이터 임계치 도달 시 sub filter 추가 생성 및 filter에서 관리 중인 dat count 관리
    public void add(SplitShardedSubBloomfilter splitShardedSubBloomfilter, String value) {
        int subFilterCount = this.findSubFilterCount(splitShardedSubBloomfilter);

        SplitShardedBloomfilter activatedFilter = splitShardedSubBloomfilter.findActivatedFilter(subFilterCount);
        splitShardedBloomfilterHandler.add(activatedFilter, value);

        Long dataCount = stringRedisTemplate.opsForValue().increment(this.genFilterDataCountKey(activatedFilter));

        appendSubFilterWhenFilterIsFull(splitShardedSubBloomfilter, activatedFilter, dataCount);
    }

    //생성된 sub filter의 갯수
    private int findSubFilterCount(SplitShardedSubBloomfilter splitShardedSubBloomfilter) {
        String result = stringRedisTemplate.opsForValue().get(this.genSubFilterCountKey(splitShardedSubBloomfilter));

        if(!StringUtils.hasText(result)){
            return 0;
        }

        return Integer.parseInt(result);
    }

    //각 filter가 full하였을때 새로운 sub filter를 생성(findSubFilter).
    private void appendSubFilterWhenFilterIsFull(SplitShardedSubBloomfilter splitShardedSubBloomfilter, SplitShardedBloomfilter activatedSubFilter, Long dataCount) {

        if(!isFilterDataCountIsFull(activatedSubFilter, dataCount)){
            return;
        }

        String distributedKey = this.genDistributedLockKeyForFilterInitOnlyOnce(splitShardedSubBloomfilter);

        if(!distributedLockProvider.lock(distributedKey, Duration.ofMinutes(1))){
            return;
        }

        try{
            //sub filter 생성
            int subFilterCount = this.findSubFilterCount(splitShardedSubBloomfilter);

            if(subFilterCount >= MAX_SUB_FILTER_COUNT){
                log.error("subFilterCount limit has been reached : {} / {} for id : {}", subFilterCount, MAX_SUB_FILTER_COUNT, splitShardedSubBloomfilter.getId());
                return;
            }

            //초기화
            splitShardedBloomfilterHandler.init(splitShardedSubBloomfilter.findSubFilter(subFilterCount));

            //서브 필터 수 반영
            stringRedisTemplate.opsForValue().increment(this.genSubFilterCountKey(splitShardedSubBloomfilter));

        } finally {
            distributedLockProvider.unlock(distributedKey);
        }

    }

    //각 필터별로 관리중인 데이터 갯수의 임계치 초과여부에 대해 판별
    private boolean isFilterDataCountIsFull(SplitShardedBloomfilter activatedFilter, Long dataCount) {
        return dataCount != null && activatedFilter.getDataCount() <= dataCount; //반영된 dataCount가 bit 개수보다 이상이면 true
    }

    //false positive
    public boolean mightContain(SplitShardedSubBloomfilter splitShardedSubBloomfilter, String value) {
        int subFilterCount = this.findSubFilterCount(splitShardedSubBloomfilter);

        return splitShardedSubBloomfilter.findAll(subFilterCount).stream()
                .anyMatch(filter -> splitShardedBloomfilterHandler.mightContain(filter, value));

        //모든 sub filter를 순회하여 하나라도 true -> false positive,
        //모두 입력이 안되어있다면 null data.
    }

    //delete
    public void delete(SplitShardedSubBloomfilter splitShardedSubBloomfilter) {
        int subFilterCount = this.findSubFilterCount(splitShardedSubBloomfilter);

        List<SplitShardedBloomfilter> list = splitShardedSubBloomfilter.findAll(subFilterCount);

        for(SplitShardedBloomfilter splitShardedBloomfilter : list){
            splitShardedBloomfilterHandler.delete(splitShardedBloomfilter);
            stringRedisTemplate.delete(this.genFilterDataCountKey(splitShardedBloomfilter));
        }

        stringRedisTemplate.delete(this.genSubFilterCountKey(splitShardedSubBloomfilter));
    }

    /*
    * 각 원본 및 sub filter에 대해 원자적으로 관리하는
    * - 적재(반영)된 데이터의 수
    * - 생성된 sub filter의 수
    * */

    //filter마다 관리(반영) 중인 데이터의 갯수
    private String genFilterDataCountKey(SplitShardedBloomfilter splitShardedBloomfilter) {
        return "split-sharded-original-bloom-filter:data-count:%s".formatted(splitShardedBloomfilter.getId());
    }

    //sub filter 갯수
    private String genSubFilterCountKey(SplitShardedSubBloomfilter splitShardedSubBloomfilter) {
        return "split-sharded-sub-bloom-filter-count:%s".formatted(splitShardedSubBloomfilter.getId());
    }

    //분산락
    private String genDistributedLockKeyForFilterInitOnlyOnce(SplitShardedSubBloomfilter splitShardedSubBloomfilter) {
        return "split-sharded-bloom-filter:data-counting-distributed-lock:%s"
                .formatted(splitShardedSubBloomfilter.getId());
    }
}
