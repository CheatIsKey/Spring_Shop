package capstone.capstone_shop.domain.item;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("MUSIC")
@Getter @Setter
public class Music extends Item {

    protected Music() {}

    public Music(String name, int price, int stockQuantity, String imageUrl) {
        super(name, price, stockQuantity, imageUrl);
    }
}
