package capstone.capstone_shop.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Delivery {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "delivery")
    private Order order;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    protected Delivery() {}

    private Delivery (Address address) {
        this.address = address;
        this.status = DeliveryStatus.READY;
    }

    public static Delivery of(Address address) {
        return new Delivery(address);
    }

    void assignOrder(Order order) {
        this.order = order;
    }

    public void complete() {
        this.status = DeliveryStatus.COMP;
    }
}
