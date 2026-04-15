package com.redis.bottleneck.repository;

import com.redis.bottleneck.model.domain.Item;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

@Slf4j
@Repository
/*
* Repository  :
* - Component 하위 개념 / Bean 등록
* - Spring의 DataAccessException 계열로 변환, DB바뀌면 catch 로직을 바꾸는 등의 번거로움 제거.
*   - Exception을 추상화하여 처리할 수 있도록 구성.
* */
public class ItemRepository {
    //key 기준 내림차순 정렬
    private final ConcurrentSkipListMap<Long, Item> tempDataBase = new ConcurrentSkipListMap<>(Comparator.reverseOrder());

    public Optional<Item> readById(Long itemId) {
        log.info("[ItemRepository.readById] itemId={}]", itemId);
        return Optional.ofNullable(tempDataBase.get(itemId));
    }

    public List<Item> readAll(Long page, Long pageSize) {
        log.info("[ItemRepository.readAll] page={} pageSize={}]", page, pageSize);
        return tempDataBase
                .values().stream()
                .skip((page - 1) * pageSize)
                .limit(pageSize)
                .toList();
    }

    public List<Item> readInfiniteScroll(Long lastItemId, Long pageSize){
        log.info("[ItemRepository.readAll] lastItemId={} pageSize={}]", lastItemId, pageSize);
        if(lastItemId == null){
            return tempDataBase
                    .values().stream()
                    .limit(pageSize)
                    .toList(); //첫페이지
        }else{
            return tempDataBase
                    .tailMap(lastItemId, false)
                    .values().stream()
                    .toList(); //첫페이지가 아닐 경우
        }
    }

    public Item create(Item item){
        log.info("[ItemRepository.create] item={}", item);
        tempDataBase.put(item.getItemId(), item);
        return item;
    }

    public Item update(Item item){
        log.info("[ItemRepository.update] item={}", item);
        tempDataBase.put(item.getItemId(), item);
        return item;
    }

    public void delete(Long itemId){
        log.info("[ItemRepository.delete] itemId={}", itemId);
        tempDataBase.remove(itemId);
    }

    public long count(){
        log.info("[ItemRepository.count] itemId={}", tempDataBase.size());
        return tempDataBase.size();
    }
}
