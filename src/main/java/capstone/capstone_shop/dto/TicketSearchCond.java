package capstone.capstone_shop.dto;

import capstone.capstone_shop.domain.TicketCategory;
import capstone.capstone_shop.domain.TicketStatus;

public record TicketSearchCond(
        TicketStatus status,
        TicketCategory category,
        String keyword
) {}
