package com.redis.bottleneck.service;

import com.redis.bottleneck.model.domain.Item;
import com.redis.bottleneck.model.request.ItemCreateRequest;
import com.redis.bottleneck.model.request.ItemUpdateRequest;
import com.redis.bottleneck.model.response.ItemPageResponse;
import com.redis.bottleneck.model.response.ItemResponse;
import com.redis.bottleneck.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemResponse read(Long itemId){
        return itemRepository.readById(itemId)
                .map(ItemResponse::from)
                .orElse(null);
    }

    public ItemPageResponse readAll(Long page, Long pageSize){
        return ItemPageResponse.from(
                itemRepository.readAll(page, pageSize),
                itemRepository.count()
        );
    }

    public ItemPageResponse readAllInfiniteScroll(Long lastItemId, Long pageSize){
        return ItemPageResponse.from(
                itemRepository.readInfiniteScroll(lastItemId, pageSize),
                itemRepository.count()

        );
    }

    public ItemResponse create(ItemCreateRequest itemCreateRequest){
        return ItemResponse.from(
                itemRepository.create(Item.create(itemCreateRequest))
        );
    }

    public ItemResponse update(Long itemId, ItemUpdateRequest itemUpdateRequest){
        Item item = itemRepository.readById(itemId).orElse(null);
        item.update(itemUpdateRequest);

        return ItemResponse.from(
                itemRepository.update(item)
        );
    }

    public void delete(long itemId){
        itemRepository.readById(itemId).ifPresent(
                item -> itemRepository.delete(item.getItemId())
        );
    }

    public long count(){
        return itemRepository.count();
    }
}
