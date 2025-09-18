package capstone.capstone_shop.domain;

import capstone.capstone_shop.domain.item.Item;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "category_item")
@Getter
public class Category_Item {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    protected Category_Item() {};

    public Category_Item(Category category, Item item) {
        this.category = category;
        this.item = item;
        category.getCategoryItems().add(this);
        item.getCategoryItems().add(this);
    }


}
