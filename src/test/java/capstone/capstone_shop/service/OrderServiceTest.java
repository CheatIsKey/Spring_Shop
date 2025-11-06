package capstone.capstone_shop.service;

import capstone.capstone_shop.domain.*;
import capstone.capstone_shop.domain.cart.Cart;
import capstone.capstone_shop.domain.cart.CartItem;
import capstone.capstone_shop.domain.item.Item;
import capstone.capstone_shop.repository.ItemRepository;
import capstone.capstone_shop.repository.OrderRepository;
import capstone.capstone_shop.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OrderService 단위 테스트 (Mockito)
 * - 스프링 컨텍스트 미사용
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock ItemRepository itemRepository;
    @Mock UserRepository userRepository;

    @InjectMocks OrderService orderService;

    /** 테스트 전용 Item 구체 타입 (JPA 미사용, 순수 도메인 객체로 취급) */
    static class TestItem extends Item {
        public TestItem(String name, int price, int stockQuantity, String imageUrl) {
            super(name, price, stockQuantity, imageUrl);
        }
    }

    // ---------- 생성자 ----------
    private TestItem item(long id, String name, int price, int stock) {
        TestItem it = new TestItem(name, price, stock, "/img");
        setId(Item.class, it, "id", id);
        return it;
    }
    private User user(long id) {
        User u = User.createUser("u"+id, "010-0000-"+id, "user"+id, "pw", null, UserRole.CLIENT);
        setId(User.class, u, "id", id);
        return u;
    }
    private Order order(long id, User u, Delivery d, List<OrderItem> items) {
        Order o = Order.create(u, d, items);
        setId(Order.class, o, "id", id);
        return o;
    }
    private void setId(Class<?> type, Object target, String field, long idVal) {
        try {
            Field f = type.getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, idVal);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private Cart cartOf(long itemId, int qty, int price, String name) {
        Cart c = new Cart();
        c.addOrIncrease(new CartItem(itemId, name, "/img", java.math.BigDecimal.valueOf(price), 0), qty);
        return c;
    }

    // ========== placeOrder ==========
    @Nested
    class PlaceOrderTests {

        @Test
        @DisplayName("placeOrder: 사용자 없음 → 실패")
        void placeOrder_user_not_found_fail() {
            // given
            Cart cart = cartOf(1L, 2, 1000, "A");
            Address addr = new Address("서울", "강남", "테헤란로");
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> orderService.placeOrder(999L, cart, addr))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다.");
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("placeOrder: 상품 없음 → 실패")
        void placeOrder_item_not_found_fail() {
            // given
            User u = user(1L);
            Cart cart = cartOf(404L, 1, 1000, "X");
            Address addr = new Address("서울", "강남", "테헤란로");
            when(userRepository.findById(1L)).thenReturn(Optional.of(u));
            when(itemRepository.findById(404L)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> orderService.placeOrder(1L, cart, addr))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다: 404");
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("placeOrder: 재고 부족 → 실패")
        void placeOrder_insufficient_stock_fail() {
            // given
            User u = user(1L);
            TestItem it = item(1L, "A", 1000, 1); // stock=1
            Cart cart = cartOf(1L, 3, 1000, "A"); // qty=3
            Address addr = new Address("서울", "강남", "테헤란로");
            when(userRepository.findById(1L)).thenReturn(Optional.of(u));
            when(itemRepository.findById(1L)).thenReturn(Optional.of(it));

            // when / then
            assertThatThrownBy(() -> orderService.placeOrder(1L, cart, addr))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("재고 부족: A");
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("placeOrder: 정상 생성 → 성공 (ID 반환, save 호출)")
        void placeOrder_success() {
            // given
            User u = user(1L);
            TestItem it = item(1L, "A", 1000, 10); // 충분 재고
            Cart cart = cartOf(1L, 3, 1000, "A");
            Address addr = new Address("서울", "강남", "테헤란로");
            when(userRepository.findById(1L)).thenReturn(Optional.of(u));
            when(itemRepository.findById(1L)).thenReturn(Optional.of(it));

            // save 시, 생성된 Order에 id를 세팅해주는 Answer
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                setId(Order.class, o, "id", 100L);
                return o;
            });

            // when
            Long orderId = orderService.placeOrder(1L, cart, addr);

            // then
            assertThat(orderId).isEqualTo(100L);
            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            Order saved = captor.getValue();
            assertThat(saved.getUser().getId()).isEqualTo(1L);
            assertThat(saved.getOrderItems()).hasSize(1);
            assertThat(saved.totalPrice()).isEqualTo(1000L * 3);
        }
    }

    // ========== getOrderDetail ==========
    @Nested
    class GetOrderDetailTests {

        @Test
        @DisplayName("getOrderDetail: 본인 아님 → AccessDeniedException")
        void getOrderDetail_not_owner_fail() throws Exception {
            // given
            User owner = user(1L);
            Delivery d = Delivery.of(new Address("서울", "강남", "역삼"));
            TestItem it = item(1L, "A", 1000, 5);
            OrderItem oi = OrderItem.create(it, 1000, 1);
            Order o = order(10L, owner, d, List.of(oi));
            when(orderRepository.findDetailById(10L)).thenReturn(Optional.of(o));

            // when & then
            assertThatThrownBy(() -> orderService.getOrderDetail(10L, 2L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("본인 주문만 조회할 수 있습니다.");
        }

        @Test
        @DisplayName("getOrderDetail: 본인 → 성공 반환")
        void getOrderDetail_owner_success() throws Exception {
            // given
            User owner = user(1L);
            Delivery d = Delivery.of(new Address("서울", "강남", "역삼"));
            TestItem it = item(1L, "A", 1000, 5);
            OrderItem oi = OrderItem.create(it, 1000, 1);
            Order o = order(10L, owner, d, List.of(oi));
            when(orderRepository.findDetailById(10L)).thenReturn(Optional.of(o));

            // when
            Order found = orderService.getOrderDetail(10L, 1L);

            // then
            assertThat(found.getId()).isEqualTo(10L);
            assertThat(found.getUser().getId()).isEqualTo(1L);
            assertThat(found.getOrderItems()).hasSize(1);
        }
    }

    // ========== cancel ==========
    @Nested
    class CancelTests {

        @Test
        @DisplayName("cancel: 본인 아님 → AccessDeniedException")
        void cancel_not_owner_fail() {
            // given
            User owner = user(1L);
            Delivery d = Delivery.of(new Address("서울", "강남", "선릉"));
            TestItem it = item(1L, "A", 1000, 5);
            OrderItem oi = OrderItem.create(it, 1000, 2); // 재고 5 -> 3
            Order o = order(20L, owner, d, List.of(oi));
            when(orderRepository.findById(20L)).thenReturn(Optional.of(o));

            // when / then
            assertThatThrownBy(() -> orderService.cancel(20L, 2L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("본인 주문만 취소할 수 있습니다.");
            assertThat(o.getStatus()).isEqualTo(OrderStatus.ORDER);
            assertThat(it.getStockQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("cancel: 배송완료 → IllegalStateException")
        void cancel_delivery_completed_fail() {
            // given
            User owner = user(1L);
            Delivery d = Delivery.of(new Address("서울", "강남", "선릉"));
            d.complete(); // 배송완료
            TestItem it = item(1L, "A", 1000, 5);
            OrderItem oi = OrderItem.create(it, 1000, 1); // 재고 5 -> 4
            Order o = order(21L, owner, d, List.of(oi));
            when(orderRepository.findById(21L)).thenReturn(Optional.of(o));

            // when / then
            assertThatThrownBy(() -> orderService.cancel(21L, 1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 배송이 완료된 상품입니다.");
            assertThat(o.getStatus()).isEqualTo(OrderStatus.ORDER);
            assertThat(it.getStockQuantity()).isEqualTo(4);
        }

        @Test
        @DisplayName("cancel: 본인 & 배송 미완료 → 성공 (상태 CANCEL, 재고 복원)")
        void cancel_owner_success() throws Exception {
            // given
            User owner = user(1L);
            Delivery d = Delivery.of(new Address("서울", "강남", "도곡"));
            TestItem it = item(1L, "A", 1000, 3);
            OrderItem oi = OrderItem.create(it, 1000, 2); // 재고 3 -> 1
            Order o = order(22L, owner, d, List.of(oi));
            when(orderRepository.findById(22L)).thenReturn(Optional.of(o));

            // when
            orderService.cancel(22L, 1L);

            // then
            assertThat(o.getStatus()).isEqualTo(OrderStatus.CANCEL);
            assertThat(it.getStockQuantity()).isEqualTo(3); // 1 -> +2
        }
    }

    // ========== myOrderRows ==========
    @Nested
    class MyOrderRowsTests {

        @Test
        @DisplayName("myOrderRows: fetch된 주문 목록을 DTO로 매핑")
        void myOrderRows_dto_mapping() {
            // given
            User u = user(1L);

            Delivery d1 = Delivery.of(new Address("서울", "강남", "역삼"));
            TestItem i1 = item(101L, "티셔츠", 12000, 10);
            TestItem i2 = item(102L, "슬랙스", 50000, 5);
            OrderItem oi1 = OrderItem.create(i1, 12000, 2); // 24,000
            OrderItem oi2 = OrderItem.create(i2, 50000, 1); // 50,000
            Order o1 = order(31L, u, d1, List.of(oi1, oi2)); // total 74,000

            Delivery d2 = Delivery.of(new Address("서울", "서초", "서초대로"));
            d2.complete();
            TestItem i3 = item(103L, "후드", 30000, 3);
            OrderItem oi3 = OrderItem.create(i3, 30000, 1); // 30,000
            Order o2 = order(32L, u, d2, List.of(oi3));

            when(orderRepository.findSummaryByUserId(1L)).thenReturn(List.of(o2, o1)); // desc 가정

            // when
            List<capstone.capstone_shop.dto.OrderRowDto> rows = orderService.myOrderRows(1L);

            // then
            assertThat(rows).hasSize(2);

            // id 순서
            assertThat(rows).extracting(capstone.capstone_shop.dto.OrderRowDto::id)
                    .containsExactly(32L, 31L);

            // 총액
            assertThat(rows).extracting(capstone.capstone_shop.dto.OrderRowDto::totalPrice)
                    .containsExactly(30000L, 74000L);

            // 주문/배송 상태
            assertThat(rows).extracting(capstone.capstone_shop.dto.OrderRowDto::status)
                    .contains("ORDER");
            assertThat(rows).extracting(capstone.capstone_shop.dto.OrderRowDto::deliveryStatus)
                    .contains("READY", "COMP");

            // 날짜 존재
            assertThat(rows.get(0).orderDate()).isNotNull();
        }
    }
}
