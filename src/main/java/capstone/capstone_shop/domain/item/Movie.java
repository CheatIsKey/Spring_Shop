package capstone.capstone_shop.domain.item;

import jakarta.persistence.Entity;

@Entity
public class Movie extends Item {

    public Movie(String name, int price, int stockQuantity, String imageUrl) {
        super(name, price, stockQuantity, imageUrl);
    }

    protected Movie() {}
}
