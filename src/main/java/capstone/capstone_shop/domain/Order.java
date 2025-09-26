package capstone.capstone_shop.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "orders")
@Getter
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    protected Order() {}

    private Order(User user, Delivery delivery, List<OrderItem> items) {
        this.user = user;
        this.status = OrderStatus.ORDER;
        this.orderDate = LocalDateTime.now();

        setDelivery(delivery);
        for (OrderItem oi : items) addOrderItem(oi);
    }

    public static Order create(User user, Delivery delivery, List<OrderItem> items) {
        return new Order(user, delivery, items);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.assignOrder(this);
    }

    // 비즈니스 로직 //
    public void cancel() {
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송이 완료된 상품입니다.");
        }

        this.status = OrderStatus.CANCEL;
        for (OrderItem oi : orderItems) {
            oi.cancel();
        }
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        if (delivery != null && delivery.getOrder() != this) {
            delivery.assignOrder(this);
        }
    }

    // 조회 로직 //
    public long totalPrice() {
        long sum = 0;
        for (OrderItem oi : orderItems) {
            sum += oi.getTotalPrice();
        }
        return sum;
    }
}
