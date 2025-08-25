package capstone.capstone_shop.domain.item;

import jakarta.persistence.Entity;

@Entity
public class Music extends Item {

    protected Music() {}

    public Music(String name, int price, int stockQuantity, String imageUrl) {
        super(name, price, stockQuantity, imageUrl);
    }
}
