package capstone.capstone_shop.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(
        name = "attachment",
        indexes = @Index(name = "idx_attachment_ticket", columnList = "ticket_id")
)
public class Attachment extends BaseTimeEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private SupportTicket ticket;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Lob @Column(nullable = false)
    private String url;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(nullable = false)
    private Long size;

    protected Attachment() {}

    public Attachment(String fileName, String url, String mimeType, Long size) {
        this.fileName = fileName;
        this.url = url;
        this.mimeType = mimeType;
        this.size = size;
    }

    public static Attachment of(String fileName, String url, String mimeType, Long size) {
        return new Attachment(fileName, url, mimeType, size);
    }

    void assignTicket(SupportTicket ticket) {
        this.ticket = ticket;
    }
}
