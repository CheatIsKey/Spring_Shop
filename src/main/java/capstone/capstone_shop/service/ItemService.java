package capstone.capstone_shop.service;

import capstone.capstone_shop.domain.item.Item;
import capstone.capstone_shop.dto.ItemDto;
import capstone.capstone_shop.exception.NotFoundException;
import capstone.capstone_shop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다."));
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public List<Item> findByNameContaining(String name) {
        return itemRepository.findByNameContaining(name);
    }


    // 메인 화면 상품 출력 //
    public List<ItemDto> findRandomItems() {
        List<Long> allItemIds = itemRepository.findAllItemIds();

        Collections.shuffle(allItemIds);
        List<Long> showItemIds = allItemIds.stream()
                .limit(3)
                .collect(Collectors.toList());

        List<Item> items = itemRepository.findAllById(showItemIds);

        return items.stream()
                .map(item -> new ItemDto(
                        item.getId(),
                        item.getName(),
                        item.getPrice(),
                        item.getStockQuantity(),
                        item.getImageUrl()
                )).collect(Collectors.toList());
    }

    public List<ItemDto> searchItems(Long categoryId, String q) {
        List<Item> items;

        if (categoryId != null) {
            items = itemRepository.findByCategoryId(categoryId);
        } else {
            items = itemRepository.findAll();
        }

        if (q != null && !q.trim().isEmpty()) {
            String keyword = q.trim().toLowerCase();
            items = items.stream()
                    .filter(i -> i.getName().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
        }

        return items.stream()
                .map(ItemDto::form)
                .collect(Collectors.toList());
    }
}
