package capstone.capstone_shop.dto;

import capstone.capstone_shop.domain.TicketCategory;
import capstone.capstone_shop.domain.TicketStatus;

import java.time.Instant;
import java.util.List;

public record TicketDetailDto(
        Long id, Long userId, String userName,
        String title, TicketCategory category, boolean isPrivate,
        TicketStatus status, String content,
        Instant createdAt, Instant updatedAt,
        List<AttachmentDto> attachments,
        List<ReplyDto> replies
) {}
