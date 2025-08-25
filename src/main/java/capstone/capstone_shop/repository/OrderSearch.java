package capstone.capstone_shop.repository;

import capstone.capstone_shop.domain.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderSearch {

    private String UserName;
    private OrderStatus orderStatus;
}
