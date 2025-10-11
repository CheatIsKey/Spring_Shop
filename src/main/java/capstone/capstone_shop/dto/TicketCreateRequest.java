package capstone.capstone_shop.dto;

import capstone.capstone_shop.domain.TicketCategory;
import capstone.capstone_shop.domain.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

public record TicketCreateRequest(
        @NotBlank String title,
        @NotNull TicketCategory category,
        @NotBlank String content,
        boolean isPrivate,
        List<MultipartFile> files // nullable 허용
) {
    public boolean getIsPrivate() {
        return isPrivate;
    }
}

