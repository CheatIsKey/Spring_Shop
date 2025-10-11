package capstone.capstone_shop.dto;

import capstone.capstone_shop.domain.TicketCategory;
import capstone.capstone_shop.domain.TicketStatus;

import java.time.Instant;

public record TicketSummaryDto(
        Long id, String title, TicketCategory category, boolean isPrivate,
        TicketStatus status, Instant createdAt
) {}
