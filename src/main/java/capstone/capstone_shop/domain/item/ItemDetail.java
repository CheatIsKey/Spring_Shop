package capstone.capstone_shop.domain.item;

import capstone.capstone_shop.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "item_detail")
public class ItemDetail extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false, unique = true)
    private Item item;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private  String description;

    @Lob
    @Column(name = "spec", columnDefinition = "TEXT")
    private String spec;

    public static ItemDetail crate(Item item, String description, String spec) {
        ItemDetail detail = new ItemDetail();
        detail.item = item;
        detail.description = description;
        detail.spec = spec;
        return detail;
    }

    public void change(String description, String spec) {
        this.description = description;
        this.spec = spec;
    }

    void _link(Item item) {
        this.item = item;
    }
}
