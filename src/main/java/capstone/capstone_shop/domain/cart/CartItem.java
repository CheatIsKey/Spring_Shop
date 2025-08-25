package capstone.capstone_shop.domain.cart;

import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
public class CartItem implements Serializable {

    private Long itemId;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private int quantity;

    public CartItem(Long itemId, String name, String imageUrl, BigDecimal price, int quantity) {
        this.itemId = itemId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public void increaseQuantity(int addQuantity) {
        this.quantity += addQuantity;
    }

    public void changeQuantity(int qty) {
        this.quantity = qty;
    }
}
