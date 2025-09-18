package capstone.capstone_shop.domain.item;

import capstone.capstone_shop.domain.Category_Item;
import capstone.capstone_shop.exception.NotEnoughStockException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "DTYPE")
public abstract class Item {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(name = "stock", nullable = false)
    private int stockQuantity;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "item")
    private List<Category_Item> categoryItems = new ArrayList<>();

    protected Item() {};

    public Item(String name, int price, int stockQuantity, String imageUrl) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
    }

    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new NotEnoughStockException("재고가 부족합니다.");
        }
        this.stockQuantity -= quantity;
    }

}
