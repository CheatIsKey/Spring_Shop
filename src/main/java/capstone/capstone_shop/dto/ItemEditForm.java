package capstone.capstone_shop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ItemEditForm {
    @NotBlank
    private String name;

    @Min(0)
    private int price;

    @Min(1)
    private int stockQuantity;

    @NotBlank
    private String content;

    @NotNull
    private Long categoryId;

    // 선택 업로드 (비워두면 기존 이미지 유지)
    private MultipartFile image;
}
