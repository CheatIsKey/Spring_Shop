package capstone.capstone_shop.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ItemForm {

    @NotEmpty
    private String name;

    @Min(0)
    private int price;

    @Min(1)
    private int stockQuantity;

    @NotNull
    private MultipartFile image;

    @NotNull
    private Long categoryId;

    @NotBlank
    @Size(max = 20000)
    private String content;
}
