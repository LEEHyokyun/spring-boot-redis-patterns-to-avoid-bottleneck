package com.redis.bottleneck.model.response;

import com.redis.bottleneck.model.domain.Item;

public record ItemResponse(
        Long itemId,
        String data
) {
    public static ItemResponse from(Item item){
        return new ItemResponse(item.getItemId(), item.getData());
    }
}
