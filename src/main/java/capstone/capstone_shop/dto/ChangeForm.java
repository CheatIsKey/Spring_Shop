package capstone.capstone_shop.dto;

import jakarta.validation.constraints.Min;

public record ChangeForm(Long itemId, @Min(0) int quantity) {}
