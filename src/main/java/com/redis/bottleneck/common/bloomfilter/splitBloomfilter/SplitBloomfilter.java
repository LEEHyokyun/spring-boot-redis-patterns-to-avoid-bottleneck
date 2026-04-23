package com.redis.bottleneck.common.bloomfilter.splitBloomfilter;

import com.redis.bottleneck.common.bloomfilter.BloomFilter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SplitBloomfilter {
    private String id;
    private BloomFilter bloomFilter;
    private long splitCount;

    public static final long BIT_SPLIT_SIZE = 1L << 10; // 2^10(1024)개, 본래 32bit 가능하지만 축소하여 운용

    public static SplitBloomfilter create(String id, long dataCount, double falsePositive){
        BloomFilter bloomFilter = BloomFilter.create(id, dataCount, falsePositive);

        /*
        * split No.
        * 1024 비트 인덱스 -> split 1
        * 1025 ~ 2049 비트 인덱스 - > split 2
        * */
        long splitCount = (bloomFilter.getBitSize() - 1) /  BIT_SPLIT_SIZE  + 1;

        SplitBloomfilter splitBloomfilter = new SplitBloomfilter();
        splitBloomfilter.id = id;
        splitBloomfilter.bloomFilter = bloomFilter;
        splitBloomfilter.splitCount = splitCount;

        return splitBloomfilter;
    }

    /*
    * split index 구하기(*전체 bit size = 1000개 데이터 규모일 경우 9585개)
    * - 0 ~ 1024 -> split #1
    * - 1025 ~ 2049 -> split #2
     */
    public long findSplitIndex(Long hashedIndex){
        if(hashedIndex >= this.bloomFilter.getBitSize()) {
            throw new IllegalArgumentException("hashedIndex out of range");
        }

        return hashedIndex / BIT_SPLIT_SIZE;
    }

    /*
    * split index가 몇개의 bit size를 보유하고 있는가
    * - bitSize = 1025개
    * - 마지막 split일 경우 전체 bit size에서 나머지 bit size를 제외한 잔여량
    * - 그렇지 않다면 단위 bit size
    * */
    public long findBitSizeOfSplitIndex(long splitIndex){
        if(splitIndex == splitCount - 1){
            return this.bloomFilter.getBitSize() - (BIT_SPLIT_SIZE * splitIndex);
        }

        return BIT_SPLIT_SIZE;
    }
}
