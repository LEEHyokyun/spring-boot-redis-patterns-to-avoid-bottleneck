package com.redis.bottleneck.repository;

import com.redis.bottleneck.model.domain.Item;
import com.redis.bottleneck.model.request.ItemCreateRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ItemRepositoryTest {

    ItemRepository itemRepository = new ItemRepository();

    @Test
    void readById() {
    }

    @Test
    void readAll() {
        //given
        List<Item> items = IntStream.range(0, 3)
                .mapToObj(idx -> itemRepository.create(Item.create(new ItemCreateRequest("data" + idx))))
                .toList();

        //when
        List<Item> firstPage = itemRepository.readAll(1L, 2L);
        List<Item> secondPage = itemRepository.readAll(2L, 2L);

        //then
        Assertions.assertThat(firstPage).hasSize(2);
        Assertions.assertThat(firstPage.get(0).getItemId()).isEqualTo(items.get(2).getItemId());
        Assertions.assertThat(firstPage.get(1).getItemId()).isEqualTo(items.get(1).getItemId());

        Assertions.assertThat(secondPage).hasSize(1);
        Assertions.assertThat(secondPage.get(0).getItemId()).isEqualTo(items.get(0).getItemId());
    }

    @Test
    void readInfiniteScroll() {
        //given
        List<Item> items = IntStream.range(0, 3)
                .mapToObj(idx -> itemRepository.create(Item.create(new ItemCreateRequest("data" + idx))))
                .toList();

        //when
        List<Item> firstPage = itemRepository.readInfiniteScroll(null, 2L);
        List<Item> secondPage = itemRepository.readInfiniteScroll(firstPage.getLast().getItemId(), 2L);

        //then
        Assertions.assertThat(firstPage).hasSize(2);
        Assertions.assertThat(firstPage.get(0).getItemId()).isEqualTo(items.get(2).getItemId());
        Assertions.assertThat(firstPage.get(1).getItemId()).isEqualTo(items.get(1).getItemId());

        Assertions.assertThat(secondPage).hasSize(1);
        Assertions.assertThat(secondPage.get(0).getItemId()).isEqualTo(items.get(0).getItemId());
    }

    @Test
    void create() {
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }

    @Test
    void count() {
    }
}