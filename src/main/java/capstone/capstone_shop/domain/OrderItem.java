package capstone.capstone_shop.domain;

import capstone.capstone_shop.domain.item.Item;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_item")
@Getter
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private long orderPrice;
    private int count;

    protected OrderItem() {}

    private OrderItem(Item item, long orderPrice, int count) {
        this.item = item;
        this.orderPrice = orderPrice;
        this.count = count;
    }

    // 생성 메서드 //
    public static OrderItem create(Item item, long orderPrice, int count) {
        item.removeStock(count);
        return new OrderItem(item, orderPrice, count);
    }

    void assignOrder(Order order) {
        this.order = order;
    }

    // 비즈니스 로직 //
    public void cancel() {
        getItem().addStock(count);
    }

    // 조회 로직 //
    public long getTotalPrice() {
        return orderPrice * count;
    }

}
