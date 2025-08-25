package capstone.capstone_shop.dto;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

import java.time.LocalDateTime;

public record OrderRowDto(
        Long id,

        @NumberFormat(pattern = "#,###")
        Long totalPrice,

        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
        LocalDateTime orderDate,

        String status,
        String deliveryStatus) {}
