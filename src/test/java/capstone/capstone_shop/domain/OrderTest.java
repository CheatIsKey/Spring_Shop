package capstone.capstone_shop.domain;

import capstone.capstone_shop.domain.item.Item;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Order 도메인 단위 테스트 (순수 JUnit, 스프링 미사용)
 * - 비즈니스 로직: create, cancel, totalPrice, 양방향 연결 보장
 */
class OrderTest {

    /** 테스트 전용 Item 서브클래스 (JPA 미사용, 순수 도메인 테스트용) */
    static class TestItem extends Item {
        public TestItem(String name, int price, int stockQuantity, String imageUrl) {
            super(name, price, stockQuantity, imageUrl);
        }
    }

    private TestItem item(String name, int price, int stock, String img) {
        return new TestItem(name, price, stock, img);
    }

    private OrderItem oi(Item item, int orderPrice, int count) {
        return OrderItem.create(item, orderPrice, count);
    }

    private User user(String name) {
        return User.createUser(name, "010-0000-0000", name.toLowerCase(), "pw", null, UserRole.CLIENT);
    }

    @Test
    @DisplayName("주문 생성: 상태 ORDER, 양방향 연결, 총액/재고 차감 검증")
    void 주문_생성_기본_동작() {
        // given
        User u = user("kim");
        Delivery d = Delivery.of(new Address("서울", "강남구", "테헤란로"));
        TestItem i1 = item("티셔츠", 12000, 10, "/t");
        TestItem i2 = item("슬랙스", 39000, 5, "/p");
        OrderItem oi1 = oi(i1, i1.getPrice(), 2); // 24,000
        OrderItem oi2 = oi(i2, i2.getPrice(), 1); // 39,000

        // when
        Order order = Order.create(u, d, List.of(oi1, oi2));

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ORDER);
        // 양방향: Delivery ↔ Order
        assertThat(order.getDelivery()).isSameAs(d);
        assertThat(d.getOrder()).isSameAs(order);
        // 양방향: OrderItem ↔ Order
        assertThat(order.getOrderItems()).containsExactlyInAnyOrder(oi1, oi2);
        assertThat(oi1.getOrder()).isSameAs(order);
        assertThat(oi2.getOrder()).isSameAs(order);
        // 총액
        assertThat(order.totalPrice()).isEqualTo(24000 + 39000);
        // 재고 차감 (2, 1)
        assertThat(i1.getStockQuantity()).isEqualTo(10 - 2);
        assertThat(i2.getStockQuantity()).isEqualTo(5 - 1);
    }

    @Test
    @DisplayName("주문 취소: 상태 CANCEL, 각 OrderItem 재고 복원")
    void 주문_취소시_상태변경_및_재고복원() {
        // given
        User u = user("lee");
        Delivery d = Delivery.of(new Address("부산", "해운대구", "센텀대로"));
        TestItem i1 = item("니트", 30000, 3, "/n");
        OrderItem oi1 = oi(i1, 30000, 2); // 재고 3 -> 1
        Order order = Order.create(u, d, List.of(oi1));

        // when
        order.cancel();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCEL);
        assertThat(i1.getStockQuantity()).isEqualTo(3); // 1 -> +2 = 3 복원
    }

    @Test
    @DisplayName("배송완료 주문 취소 시 예외 발생")
    void 배송완료_주문_취소_불가() {
        // given
        User u = user("park");
        Delivery d = Delivery.of(new Address("대구", "수성구", "달구벌대로"));
        TestItem i1 = item("가디건", 45000, 4, "/g");
        OrderItem oi1 = oi(i1, 45000, 1);
        Order order = Order.create(u, d, List.of(oi1));
        d.complete(); // 배송완료 상태로 전환

        // when & then
        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 배송이 완료된 상품입니다.");
        // 상태/재고 변화 없음
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ORDER);
        assertThat(i1.getStockQuantity()).isEqualTo(4 - 1);
    }

    @Test
    @DisplayName("총액 계산: orderPrice * count 합계 반환")
    void totalPrice_계산() {
        // given
        User u = user("choi");
        Delivery d = Delivery.of(new Address("인천", "연수구", "송도동"));
        TestItem i1 = item("셔츠", 20000, 5, "/s");
        TestItem i2 = item("코트", 120000, 2, "/c");
        OrderItem oi1 = oi(i1, 15000, 2);   // 할인 가격 15,000 * 2 = 30,000
        OrderItem oi2 = oi(i2, 110000, 1);  // 110,000
        Order order = Order.create(u, d, List.of(oi1, oi2));

        // when
        long sum = order.totalPrice();

        // then
        assertThat(sum).isEqualTo(30000 + 110000);
    }

    @Test
    @DisplayName("setDelivery: 새로운 배송 연결 시 양방향 일관성 보장")
    void setDelivery_양방향_일관성() {
        // given
        User u = user("han");
        Delivery d1 = Delivery.of(new Address("광주", "북구", "비엔날레로"));
        TestItem i1 = item("블루종", 70000, 3, "/b");
        OrderItem oi1 = oi(i1, 70000, 1);
        Order order = Order.create(u, d1, List.of(oi1));

        Delivery d2 = Delivery.of(new Address("광주", "동구", "충장로"));

        // when
        order.setDelivery(d2);

        // then
        assertThat(order.getDelivery()).isSameAs(d2);
        assertThat(d2.getOrder()).isSameAs(order);
    }

    @Test
    @DisplayName("addOrderItem: 주문 생성 후 라인 추가 시 양방향 연결 및 총액 반영")
    void addOrderItem_추가시_연결_및_총액() {
        // given
        User u = user("jung");
        Delivery d = Delivery.of(new Address("수원", "영통구", "광교호수로"));
        TestItem i1 = item("맨투맨", 25000, 10, "/m");
        OrderItem oi1 = oi(i1, 25000, 1);
        Order order = Order.create(u, d, List.of(oi1));

        TestItem i2 = item("치노팬츠", 45000, 5, "/p");
        OrderItem oi2 = oi(i2, 40000, 2); // 80,000

        // when
        order.addOrderItem(oi2);

        // then
        assertThat(order.getOrderItems()).hasSize(2).contains(oi2);
        assertThat(oi2.getOrder()).isSameAs(order);
        assertThat(order.totalPrice()).isEqualTo(25000 + 80000);
        // 추가 라인의 재고 차감(2)
        assertThat(i2.getStockQuantity()).isEqualTo(5 - 2);
    }
}
