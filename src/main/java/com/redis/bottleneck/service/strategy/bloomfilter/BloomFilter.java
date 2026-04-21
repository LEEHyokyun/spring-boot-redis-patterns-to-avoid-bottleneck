package com.redis.bottleneck.service.strategy.bloomfilter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.testcontainers.shaded.com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BloomFilter {
    private String id;
    private long dataCount;
    private double falsePositiveRate;
    private long bitSize;
    private int hashFunctionCount;
    private List<BloomFilterHashFunction> hashFunctions;

    /*
    * BloomFilter 구현
    * */
    public static BloomFilter create(String id, long dataCount, double falsePositiveRate){
        if(dataCount <= 0){
            throw new IllegalArgumentException("dataCount more than 0");
        }

        if(falsePositiveRate <= 0.0 || falsePositiveRate >= 1.0){
            throw new IllegalArgumentException("Invalid False positive rate");
        }

        long bitSize = calculateBitSize(dataCount, falsePositiveRate);
        int hashFunctionCount = calculateHashFunctionCount(dataCount, bitSize);

        /*
        * 나중에 실행될 hash 함수에 대한 정의
        * */
        //List<Long> result = bloomFilter.hashFunctions.hash("user:123");
        List<BloomFilterHashFunction> hashFunctionsList = IntStream.range(0, hashFunctionCount)
                .mapToObj(seed ->
                        (BloomFilterHashFunction) key -> Math.abs(Hashing.murmur3_128(seed)
                                .hashString(key, StandardCharsets.UTF_8)
                                .asLong() % bitSize
                        )
                )
                .toList()
                ;

        /*
        * key to hashing List
        * */

        BloomFilter bloomFilter = new BloomFilter();
        bloomFilter.id = id;
        bloomFilter.dataCount = dataCount;
        bloomFilter.falsePositiveRate = falsePositiveRate;
        bloomFilter.bitSize = bitSize;
        bloomFilter.hashFunctionCount = hashFunctionCount;
        bloomFilter.hashFunctions = hashFunctionsList;

        return bloomFilter;
    }

    private static long calculateBitSize(long dataCount, double falsePositiveRate){
        return (long) Math.ceil(
                -(dataCount * Math.log(falsePositiveRate)) / (Math.pow(Math.log(2), 2))
        );
    }

    private static int calculateHashFunctionCount(long dataCount, long bitSize){
        return (int) Math.ceil(
                (bitSize / (double) dataCount) * Math.log(2)
        );
    }

    public List<Long> hash(String key){
        return hashFunctions.stream()
                .map(hashFunction -> hashFunction.hash(key))
                .toList()
                ;
    }
}
