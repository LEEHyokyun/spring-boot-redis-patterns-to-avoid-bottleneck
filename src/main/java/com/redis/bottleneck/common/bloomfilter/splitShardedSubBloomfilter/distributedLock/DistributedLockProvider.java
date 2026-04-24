package com.redis.bottleneck.common.bloomfilter.splitShardedSubBloomfilter.distributedLock;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class DistributedLockProvider {
    private final StringRedisTemplate stringRedisTemplate;

    public boolean lock(String id, Duration ttl){
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(this.genKey(id), "", ttl);

        return result != null && result;
    }

    public void unlock(String id){
        stringRedisTemplate.delete(this.genKey(id));
    }

    private String genKey(String id){
        return "distributed-lock:" + id;
    }
}
