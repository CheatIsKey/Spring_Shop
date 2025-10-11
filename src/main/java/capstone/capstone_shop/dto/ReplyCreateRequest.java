package capstone.capstone_shop.dto;

import jakarta.validation.constraints.NotBlank;

public record ReplyCreateRequest(
        @NotBlank String content
) {}
