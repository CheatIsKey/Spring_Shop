package capstone.capstone_shop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "category")
    private List<Category_Item> categoryItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>();

    protected Category() {}

    private Category(String name) {
        this.name = validateName(name);
    }

    private String validateName(String name) {
        String text = (name == null) ? "" : name.trim();

        if (text.isEmpty()) throw new IllegalArgumentException("카테고리 이름은 필수입니다.");
        return text;
    }

    public static Category createRoot(String name) {
        return new Category(name);
    }

    public static Category createChild(String name, Category parent) {
        Category c = new Category(name);
        if (parent != null) {
            parent.addChildCategory(c);
        }
        return c;
    }

    protected void setParent(Category parent){
        this.parent = parent;
    }

    public void addChildCategory(Category child) {
        this.child.add(child);
        child.setParent(this);
    }
}
