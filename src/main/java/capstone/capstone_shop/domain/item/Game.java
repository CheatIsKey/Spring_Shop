package capstone.capstone_shop.domain.item;

import jakarta.persistence.Entity;

//@Entity
public class Game extends Item {

    public Game(String name, int price, int stockQuantity, String imageUrl) {
        super(name, price, stockQuantity, imageUrl);
    }

    protected Game() {};
}
