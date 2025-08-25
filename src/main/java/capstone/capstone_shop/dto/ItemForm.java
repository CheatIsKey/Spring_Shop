package capstone.capstone_shop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ItemForm {

    @NotEmpty
    private String name;

    @Min(0)
    private int price;

    @Min(0)
    private int stockQuantity;

    @NotNull
    private MultipartFile image;

    @NotNull
    private Long categoryId;
}
