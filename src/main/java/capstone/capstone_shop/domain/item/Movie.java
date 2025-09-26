package capstone.capstone_shop.domain.item;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("MOVIE")
@Getter @Setter
public class Movie extends Item {

    public Movie(String name, int price, int stockQuantity, String imageUrl) {
        super(name, price, stockQuantity, imageUrl);
    }

    protected Movie() {}
}
