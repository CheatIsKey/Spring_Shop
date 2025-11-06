package capstone.capstone_shop.service;

import capstone.capstone_shop.domain.cart.Cart;
import capstone.capstone_shop.domain.item.Item;
import capstone.capstone_shop.repository.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock ItemRepository itemRepository;
    @InjectMocks CartService cartService;

    /** 테스트 전용 Item 서브클래스 (단위테스트용, JPA 미사용) */
    @Entity
    @DiscriminatorValue("TEST_CART")
    @NoArgsConstructor
    static class TestItem extends Item {
        public TestItem(String name, int price, int stockQuantity, String imageUrl) {
            super(name, price, stockQuantity, imageUrl);
        }
    }

    private TestItem item(String name, int price, int stock, String img) {
        return new TestItem(name, price, stock, img);
    }

    /** 리플렉션으로 Item.id 주입 (테스트 편의) */
    private void setId(Item it, long id) {
        try {
            Field f = Item.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(it, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("getCart: 세션에 Cart가 없으면 생성하고, 있으면 재사용한다")
    void getCart_initialize_and_reuse() {
        // given
        MockHttpSession session = new MockHttpSession();

        // when
        Cart c1 = cartService.getCart(session);
        Cart c2 = cartService.getCart(session);

        // then
        assertThat(c1).isNotNull();
        assertThat(c1).isSameAs(c2);
    }

    @Nested
    class AddTests {

        @Test
        @DisplayName("add: 정상 추가 (허용 수량 이내)")
        void add_ok() {
            // given
            MockHttpSession session = new MockHttpSession();
            TestItem it = item("A", 1000, 10, "/a");
            setId(it, 1L);
            when(itemRepository.findById(1L)).thenReturn(Optional.of(it));

            // when
            cartService.add(session, 1L, 3);

            // then
            Cart cart = cartService.getCart(session);
            assertThat(cart.contains(1L)).isTrue();
            assertThat(cart.getQuantity(1L)).isEqualTo(3);
            assertThat(cart.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(3000));
        }

        @Test
        @DisplayName("add: qty <= 0 이면 IllegalArgumentException")
        void add_qty_invalid() {
            // given
            MockHttpSession session = new MockHttpSession();

            // when / then
            assertThatThrownBy(() -> cartService.add(session, 1L, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("수량은 1 이상");
        }

        @Test
        @DisplayName("add: 존재하지 않는 상품이면 IllegalArgumentException")
        void add_item_not_found() {
            // given
            MockHttpSession session = new MockHttpSession();
            when(itemRepository.findById(1L)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> cartService.add(session, 1L, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("상품이 존재하지 않습니다");
        }

        @Test
        @DisplayName("add: 품절(재고 0)이면 IllegalStateException")
        void add_sold_out() {
            // given
            MockHttpSession session = new MockHttpSession();
            TestItem it = item("A", 1000, 0, "/a");
            setId(it, 1L);
            when(itemRepository.findById(1L)).thenReturn(Optional.of(it));

            // when / then
            assertThatThrownBy(() -> cartService.add(session, 1L, 1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("품절된 상품");
        }

        @Test
        @DisplayName("add: 이미 카트에 최대치 담겨 있으면 IllegalStateException")
        void add_already_at_max() {
            // given
            MockHttpSession session = new MockHttpSession();
            TestItem it = item("A", 1000, 5, "/a");
            setId(it, 1L);
            when(itemRepository.findById(1L)).thenReturn(Optional.of(it));

            // when
            cartService.add(session, 1L, 5); // 현재 5(=stock 최대)

            // then
            assertThatThrownBy(() -> cartService.add(session, 1L, 1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 최대 5개까지 담겨 있습니다");
        }

        @Test
        @DisplayName("add: 허용 수량 초과 요청 시, 허용치까지만 추가된다")
        void add_cap_to_allowed() {
            // given
            MockHttpSession session = new MockHttpSession();
            TestItem it = item("A", 1000, 10, "/a");
            setId(it, 4L);
            when(itemRepository.findById(4L)).thenReturn(Optional.of(it));

            // when
            cartService.add(session, 4L, 7); // 현재 7
            cartService.add(session, 4L, 5); // allowed=3 → 총 10

            // then
            Cart cart = cartService.getCart(session);
            assertThat(cart.getQuantity(4L)).isEqualTo(10);
        }
    }

    @Nested
    class ChangeQuantityTests {

        @Test
        @DisplayName("changeQuantity: 정상 변경")
        void change_ok() {
            // given
            MockHttpSession session = new MockHttpSession();
            TestItem it = item("B", 2000, 10, "/b");
            setId(it, 5L);
            when(itemRepository.findById(5L)).thenReturn(Optional.of(it));

            cartService.add(session, 5L, 2);

            // when
            cartService.changeQuantity(session, 5L, 6);

            // then
            Cart cart = cartService.getCart(session);
            assertThat(cart.getQuantity(5L)).isEqualTo(6);
            assertThat(cart.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(2000L * 6));
        }

        @Test
        @DisplayName("changeQuantity: qty <= 0 이면 해당 아이템 제거")
        void change_to_zero_removes() {
            // given
            MockHttpSession session = new MockHttpSession();
            TestItem it = item("C", 3000, 10, "/c");
            setId(it, 6L);
            when(itemRepository.findById(6L)).thenReturn(Optional.of(it));

            cartService.add(session, 6L, 2);

            // when
            cartService.changeQuantity(session, 6L, 0);

            // then
            Cart cart = cartService.getCart(session);
            assertThat(cart.contains(6L)).isFalse();
        }

        @Test
        @DisplayName("changeQuantity: 상품 미존재 시 IllegalArgumentException")
        void change_item_not_found() {
            // given
            MockHttpSession session = new MockHttpSession();
            when(itemRepository.findById(7L)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> cartService.changeQuantity(session, 7L, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("상품이 존재하지 않습니다");
        }

        @Test
        @DisplayName("changeQuantity: 품절이면 카트에서 제거되고 예외 발생")
        void change_sold_out_removes_and_throws() {
            // given
            MockHttpSession session = new MockHttpSession();
            TestItem inStock = item("D", 4000, 5, "/d");   // add 시점
            TestItem soldOut = item("D", 4000, 0, "/d");   // change 시점
            setId(inStock, 8L);
            setId(soldOut, 8L);
            // 연속 반환: add() 때는 재고 있음 → change() 때는 품절
            when(itemRepository.findById(8L)).thenReturn(Optional.of(inStock), Optional.of(soldOut));

            cartService.add(session, 8L, 2);

            // when / then
            assertThatThrownBy(() -> cartService.changeQuantity(session, 8L, 5))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("품절되어 장바구니에서 제거");

            Cart cart = cartService.getCart(session);
            assertThat(cart.contains(8L)).isFalse();
        }

        @Test
        @DisplayName("changeQuantity: 요청 수량이 재고보다 크면 재고로 조정되고 예외 발생")
        void change_over_stock_adjusts_and_throws() {
            // given
            MockHttpSession session = new MockHttpSession();
            TestItem it = item("E", 5000, 4, "/e"); // 재고 4
            setId(it, 9L);
            when(itemRepository.findById(9L)).thenReturn(Optional.of(it));

            cartService.add(session, 9L, 2);

            // when / then
            assertThatThrownBy(() -> cartService.changeQuantity(session, 9L, 10))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("최대 4개로 조정되었습니다.");

            // then
            Cart cart = cartService.getCart(session);
            assertThat(cart.getQuantity(9L)).isEqualTo(4);
        }
    }

    @Nested
    class RemoveAndClearTests {

        @Test
        @DisplayName("remove: 해당 아이템 라인을 제거한다")
        void remove_item() {
            // given
            MockHttpSession session = new MockHttpSession();
            TestItem it = item("F", 1500, 5, "/f");
            setId(it, 10L);
            when(itemRepository.findById(10L)).thenReturn(Optional.of(it));

            cartService.add(session, 10L, 2);

            // when
            cartService.remove(session, 10L);

            // then
            Cart cart = cartService.getCart(session);
            assertThat(cart.contains(10L)).isFalse();
        }

        @Test
        @DisplayName("clear: 장바구니를 비운다")
        void clear_cart() {
            // given
            MockHttpSession session = new MockHttpSession();
            TestItem it1 = item("G", 1000, 10, "/g");
            TestItem it2 = item("H", 2500, 10, "/h");
            setId(it1, 11L);
            setId(it2, 12L);
            when(itemRepository.findById(11L)).thenReturn(Optional.of(it1));
            when(itemRepository.findById(12L)).thenReturn(Optional.of(it2));

            cartService.add(session, 11L, 2);
            cartService.add(session, 12L, 1);

            // when
            cartService.clear(session);

            // then
            Cart cart = cartService.getCart(session);
            assertThat(cart.getItemCount()).isEqualTo(0);
            assertThat(cart.getTotalQuantity()).isEqualTo(0);
            assertThat(cart.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("총합 계산(getTotal): 모든 라인의 (price * qty) 합계를 반환한다")
        void total_sum() {
            // given
            MockHttpSession session = new MockHttpSession();
            TestItem it1 = item("I", 1000, 10, "/i");
            TestItem it2 = item("J", 2500, 10, "/j");
            setId(it1, 13L);
            setId(it2, 14L);
            when(itemRepository.findById(13L)).thenReturn(Optional.of(it1));
            when(itemRepository.findById(14L)).thenReturn(Optional.of(it2));

            cartService.add(session, 13L, 2); // 2000
            cartService.add(session, 14L, 1); // 2500

            // when
            BigDecimal total = cartService.getCart(session).getTotal();

            // then
            assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(4500));
        }
    }
}
