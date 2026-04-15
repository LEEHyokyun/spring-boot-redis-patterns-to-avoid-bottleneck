package com.redis.bottleneck.model.domain;

import com.redis.bottleneck.model.request.ItemCreateRequest;
import com.redis.bottleneck.model.request.ItemUpdateRequest;
import lombok.Getter;
import lombok.ToString;

import java.util.concurrent.atomic.AtomicLong;

//domain
@Getter
@ToString
public class Item {
    private Long itemId;
    private String data;

    private static final AtomicLong NEXT_ID = new AtomicLong(0);

    public static Item create(ItemCreateRequest itemCreateRequest) {
        Item item = new Item();
        item.itemId = NEXT_ID.getAndIncrement();
        item.data = itemCreateRequest.data();

        return item;
    }

    public void update(ItemUpdateRequest itemUpdateRequest) {
        this.data = itemUpdateRequest.data();
    }
}
