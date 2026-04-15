package com.redis.bottleneck.common.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE) //생성자를 외부에서 접근하지 못하게.
public final class DataSerializer {
    private static final ObjectMapper objcetMapper = new ObjectMapper();

    //직렬화
    public static String serializeOrException(Object data){
        try{
            return objcetMapper.writeValueAsString(data);
        } catch(Exception e){
            log.error("[DataSerializer.serializeOrException ERROR] data : {}, Error : {}", data, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    //역직렬화
    public static <T> T deserializeOrNull(String data, Class<T> clazz){
        try{
            return objcetMapper.readValue(data, clazz);
        }catch(Exception e){
            log.error("[DataSerializer.deserializeOrNull ERROR] data : {}, Error : {}", data, e.getMessage());
            return null;
        }
    }
}
