package capstone.capstone_shop.dto;

public record AttachmentDto(
        Long id, String fileName, String url, String mimeType, Long size
) {}
