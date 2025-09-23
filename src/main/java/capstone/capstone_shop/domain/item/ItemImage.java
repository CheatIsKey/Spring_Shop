package capstone.capstone_shop.domain.item;

import capstone.capstone_shop.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "item_image")
public class ItemImage extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "url", nullable = false, length = 1024)
    private String url;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public static ItemImage create(Item item, String url, String fileName, int sortOrder) {
        ItemImage img = new ItemImage();
        img.item = item;
        img.url = url;
        img.fileName = fileName;
        img.sortOrder = sortOrder;
        return img;
    }

    public void changeMeta(String url, String fileName) {
        if (url != null && !url.isBlank()) {
            this.url = url;
            this.fileName = fileName;
        }
    }

    public void moveTo(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    void _link(Item item) {
        this.item = item;
    }
}
