package capstone.capstone_shop.service;

import capstone.capstone_shop.domain.Category;
import capstone.capstone_shop.domain.Category_Item;
import capstone.capstone_shop.domain.User;
import capstone.capstone_shop.domain.item.Item;
import capstone.capstone_shop.dto.ItemDto;
import capstone.capstone_shop.exception.NotFoundException;
import capstone.capstone_shop.repository.CategoryItemRepository;
import capstone.capstone_shop.repository.CategoryRepository;
import capstone.capstone_shop.repository.ItemRepository;
import capstone.capstone_shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryItemRepository categoryItemRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long saveItemWithCategory(Item item, Long categoryId) {
        itemRepository.save(item);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다. id=" + categoryId));

        Category_Item link = new Category_Item(category, item);
        categoryItemRepository.save(link);

        return item.getId();
    }

    @Transactional
    public Long saveItemWithCategoryAndOwner(Item item, Long categoryId, Long ownerUserId) {
        User owner = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("등록자 정보를 찾을 수 없습니다."));
        item.assignCreatedBy(owner);
        return saveItemWithCategory(item, categoryId);
    }

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

    @Transactional(readOnly = true)
    public Item findByIdWithCategories(Long id) {
        return itemRepository.findByIdWithCategories(id)
                .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다."));
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

    public List<Item> findMyItems(Long userId) {
        return itemRepository.findAllByCreatedByIdWithCategory(userId);
    }

    @Transactional
    public void updateItemByOwner(Long itemId,
                                  Long ownerUserId,
                                  String newName,
                                  int newPrice,
                                  int newStock,
                                  String newContent,
                                  Long newCategoryId,
                                  String newImageUrlNullable) {
        Item item = findById(itemId);
        if (!item.getCreatedBy().getId().equals(ownerUserId))
            throw new IllegalStateException("본인이 등록한 상품만 수정할 수 있습니다.");

        Category category = categoryRepository.findById(newCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다. id=" + newCategoryId));

        item.changeName(newName);
        item.changePrice(newPrice);
        item.changeStock(newStock);
        item.setContent(newContent);
        if (newImageUrlNullable != null && !newImageUrlNullable.isBlank()) {
            item.changeImageUrl(newImageUrlNullable);
        }

        // 카테고리 재연결(기존 전부 제거 후 하나만 연결)
        item.getCategoryItems().clear();
        Category_Item link = new Category_Item(category, item);
        categoryItemRepository.save(link);
    }

    @Transactional
    public void deleteItemByOwner(Long itemId, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다."));

        if (item.getCreatedBy() == null || !item.getCreatedBy().getId().equals(ownerId)) {
            throw new IllegalStateException("본인이 등록한 상품만 삭제할 수 있습니다.");
        }

        try {
            // 카테고리-상품 연결 먼저 제거
            List<Category_Item> links = new ArrayList<>(item.getCategoryItems());
            for (Category_Item link : links) {
                categoryItemRepository.delete(link);
            }

            // 실제 아이템 삭제 + 즉시 flush로 FK 위반을 '여기서' 터뜨린다
            itemRepository.delete(item);
            itemRepository.flush();  // ★ 핵심

        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // 주문아이템이 참조중일 때 등
            throw new IllegalStateException(
                    "이미 주문에 포함된 상품은 삭제할 수 없습니다. " +
                            "관련 주문을 취소한 뒤 삭제하거나, 대신 판매중지로 전환해 주세요."
            );
        }
    }
}
