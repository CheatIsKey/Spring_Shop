package capstone.capstone_shop.dto;

import java.time.Instant;

public record ReplyDto(
        Long id, Long authorId, String authorName, boolean isStaffReply,
        String content, Instant createdAt, Instant updatedAt
) {}
