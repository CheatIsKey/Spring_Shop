package capstone.capstone_shop.dto;

import jakarta.validation.constraints.Min;

public record AddForm(Long itemId, @Min(0) int quantity) {}
