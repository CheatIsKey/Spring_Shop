package capstone.capstone_shop.service;

import capstone.capstone_shop.domain.Category;
import capstone.capstone_shop.domain.Category_Item;
import capstone.capstone_shop.domain.item.Item;
import capstone.capstone_shop.dto.ItemDto;
import capstone.capstone_shop.exception.NotFoundException;
import capstone.capstone_shop.repository.CategoryItemRepository;
import capstone.capstone_shop.repository.CategoryRepository;
import capstone.capstone_shop.repository.ItemRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.DiscriminatorValue;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import capstone.capstone_shop.exception.NotEnoughStockException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "file.upload-dir=/tmp/uploads",
        "app.storage=local",
        "gcs.enabled=false",
        "spring.cloud.gcp.storage.bucket=test-bucket"
})
@ActiveProfiles("local")
@Transactional
class ItemServiceTest {

    @Autowired ItemService itemService;
    @Autowired ItemRepository itemRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired CategoryItemRepository categoryItemRepository;
    @Autowired EntityManager em;

    /**
     * 테스트 전용 서브클래스 (SINGLE_TABLE용 구체 타입)
     */
    @Entity
    @DiscriminatorValue("TEST")
    @NoArgsConstructor
    static class TestItem extends Item {
        public TestItem(String name, int price, int stockQuantity, String imageUrl) {
            super(name, price, stockQuantity, imageUrl);
        }
        public TestItem(String name, int price, int stockQuantity, String imageUrl, String content) {
            super(name, price, stockQuantity, imageUrl, content);
        }
    }

    private TestItem item(String name, int price, int stock, String imageUrl) {
        return new TestItem(name, price, stock, imageUrl);
    }
    private Category rootCategory(String name) {
        return Category.createRoot(name);
    }
    private void clearPC() { em.flush(); em.clear(); }


    /**
     * CRUD 테스트
     */
    @Test
    @DisplayName("상품을 카테고리와 함께 저장하면 Category_Item가 생성되고 조회 시 연결된다")
    void save_with_category_creates_link() {
        // given
        Category cat = categoryRepository.save(rootCategory("상의"));
        TestItem it = item("베이직 티셔츠", 12000, 50, "/img/t1.png");

        // when
        Long savedId = itemService.saveItemWithCategory(it, cat.getId());
        clearPC();

        // then
        Item found = itemService.findById(savedId);
        assertThat(found.getName()).isEqualTo("베이직 티셔츠");

        // 연관관계까지 로딩하는 레포지토리 메서드로 검증
        List<Item> byCat = itemRepository.findByCategoryId(cat.getId());
        assertThat(byCat).extracting(Item::getId).contains(savedId);

        // 링크 테이블에도 1건 생겼는지 대략 검증
        List<Category_Item> links = cat.getCategoryItems();
        assertThat(links).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리와 함께 저장 시 IllegalArgumentException 발생")
    void save_with_not_exist_category_throws() {
        // given
        TestItem it = item("슬랙스", 39000, 20, "/img/p1.png");
        Long notExistCatId = -1L;

        // when & then
        assertThatThrownBy(() -> itemService.saveItemWithCategory(it, notExistCatId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 카테고리");
    }

    @Test
    @DisplayName("상품 단독 저장 및 단건 조회")
    void save_and_findById() {
        // given
        TestItem it = item("오버핏 셔츠", 29000, 30, "/img/s1.png");

        // when
        itemService.saveItem(it);
        Long id = it.getId();
        clearPC();

        // then
        Item found = itemService.findById(id);
        assertThat(found.getPrice()).isEqualTo(29000);
        assertThat(found.getStockQuantity()).isEqualTo(30);
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회 시 NotFoundException")
    void findById_not_found() {
        // given
        Long notExistId = 999_999L;

        // when & then
        assertThatThrownBy(() -> itemService.findById(notExistId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("findByNameContaining: 부분 일치 검색 (DB collation에 따라 대소문자 민감할 수 있음)")
    void findByNameContaining_partial_match() {
        // given
        itemService.saveItem(item("Air Max 90", 129000, 5, "/img/a1.png"));
        itemService.saveItem(item("Air Force 1", 119000, 3, "/img/a2.png"));
        itemService.saveItem(item("Stan Smith", 99000, 7, "/img/a3.png"));
        clearPC();

        // when
        List<Item> air = itemService.findByNameContaining("Air");

        // then (대소문자 민감도는 DB 설정 의존 → 'Air'로 검증)
        assertThat(air).extracting(Item::getName)
                .containsExactlyInAnyOrder("Air Max 90", "Air Force 1");
    }

    @Test
    @DisplayName("메인 화면 랜덤 상품: 최대 3개 반환 및 유효 ID 보장")
    void findRandomItems_returns_up_to_three() {
        // given
        for (int i = 1; i <= 6; i++) {
            itemService.saveItem(item("ITEM-" + i, 1000 * i, 10 + i, "/img/" + i + ".png"));
        }
        clearPC();

        // when
        List<ItemDto> random = itemService.findRandomItems();

        // then
        assertThat(random.size()).isBetween(0, 3);
        List<Long> allIds = itemRepository.findAllItemIds();

        assertThat(random).extracting("id").allMatch(allIds::contains);
    }

    @Test
    @DisplayName("searchItems: 카테고리별 + 키워드(대소문자 무시) 필터링")
    void searchItems_category_and_keyword() {
        // given
        Category top = categoryRepository.save(rootCategory("상의"));
        Category bottom = categoryRepository.save(rootCategory("하의"));

        itemService.saveItemWithCategory(item("오버핏 셔츠", 29000, 30, "/img/s1.png"), top.getId());
        itemService.saveItemWithCategory(item("린넨 셔츠", 39000, 15, "/img/s2.png"), top.getId());
        itemService.saveItemWithCategory(item("슬림 슬랙스", 49000, 12, "/img/p1.png"), bottom.getId());
        clearPC();

        // when: 카테고리 = 상의, 키워드 = "셔츠"
        List<ItemDto> topShirts = itemService.searchItems(top.getId(), "셔츠");
        // then
        assertThat(topShirts).extracting("name")
                .containsExactlyInAnyOrder("오버핏 셔츠", "린넨 셔츠");

        // when: 카테고리 = null(전체), 키워드 = "슬랙스"
        List<ItemDto> slacks = itemService.searchItems(null, "슬랙스");
        // then
        assertThat(slacks).extracting("name").containsExactly("슬림 슬랙스");

        // when: 공백 키워드 → 카테고리 필터만 적용
        List<ItemDto> onlyTop = itemService.searchItems(top.getId(), "   ");
        // then
        assertThat(onlyTop).extracting("name")
                .containsExactlyInAnyOrder("오버핏 셔츠", "린넨 셔츠");
    }

    /**
     * 재고 변경(Update)과 삭제(Delete) 테스트
     * 업데이트 -> 더티체킹
     * 삭제 -> 레포지토리
     */
    @Test
    @DisplayName("재고 증가/감소 업데이트: 더티체킹으로 반영되고, 부족 시 NotEnoughStockException 발생")
    void 재고_증가_감소_및_부족_예외() {
        // given
        TestItem it = item("캐시미어 니트", 79000, 10, "/img/k1.png");
        itemService.saveItem(it);
        Long id = it.getId();
        clearPC();

        // when: 서비스로 로드 → 엔티티 변경(업데이트)
        Item managed = itemService.findById(id);
        managed.addStock(5);     // 10 -> 15
        managed.removeStock(3);  // 15 -> 12
        clearPC();

        // then: 변경감지로 DB 반영되었는지 확인
        Item after = itemService.findById(id);
        assertThat(after.getStockQuantity()).isEqualTo(12);

        // when & then: 재고 부족 예외
        assertThatThrownBy(() -> {
            Item again = itemService.findById(id);
            again.removeStock(9999);
            clearPC();
        }).isInstanceOf(NotEnoughStockException.class)
                .hasMessageContaining("재고가 부족");
    }

    @Test
    @DisplayName("아이템 삭제: 삭제 후 조회하면 NotFoundException")
    void 아이템_삭제_후_조회실패() {
        // given
        TestItem it = item("울코트", 189000, 7, "/img/c1.png");
        itemService.saveItem(it);
        Long id = it.getId();
        clearPC();

        // when: 서비스에 삭제 메서드가 없으므로 레포지토리로 삭제
        itemRepository.deleteById(id);
        clearPC();

        // then
        assertThatThrownBy(() -> itemService.findById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("카테고리 재할당(추가 링크): 다른 카테고리에 연결되면 해당 카테고리 조회에 포함된다")
    void 카테고리_재할당_추가링크() {
        // given
        Category top = categoryRepository.save(rootCategory("상의"));
        Category outer = categoryRepository.save(rootCategory("아우터"));
        Long itemId = itemService.saveItemWithCategory(item("데님 셔켓", 59000, 20, "/img/j1.png"), top.getId());
        clearPC();

        // when: 기존 top 링크 유지 + outer에 추가 링크 생성
        Item found = itemService.findById(itemId);
        Category outerManaged = categoryRepository.findById(outer.getId()).orElseThrow();
        categoryItemRepository.save(new Category_Item(outerManaged, found));
        clearPC();

        // then: 새 카테고리로 조회하면 포함됨
        List<Item> outerItems = itemRepository.findByCategoryId(outer.getId());
        assertThat(outerItems).extracting(Item::getId).contains(itemId);

        // searchItems(카테고리 필터)로도 검증
        var result = itemService.searchItems(outer.getId(), "셔켓");
        assertThat(result).extracting("name").contains("데님 셔켓");
    }

}
