package capstone.capstone_shop.dto;

import capstone.capstone_shop.domain.item.Item;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ItemDto {

    private Long id;

    @NotEmpty
    private String name;

    private int price;
    private int stockQuantity;

    @NotEmpty
    private String imageUrl;

    protected ItemDto() {};

    public ItemDto(Long id, String name, int price, int stockQuantity, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
    }

    public static ItemDto form(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setPrice(item.getPrice());
        dto.setStockQuantity(item.getStockQuantity());
        dto.setImageUrl(item.getImageUrl());
        return dto;
    }
}
