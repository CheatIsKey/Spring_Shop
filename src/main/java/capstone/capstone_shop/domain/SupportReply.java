package capstone.capstone_shop.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(
        name = "support_reply",
        indexes = {
                @Index(name = "idx_reply_ticket", columnList = "ticket_id"),
                @Index(name = "idx_reply_created_at", columnList = "created_at")
        }
)
public class SupportReply extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private SupportTicket ticket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "content", columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "is_staff_reply", nullable = false)
    private boolean isStaffReply;

    protected SupportReply() {}

    public SupportReply(User author, String content, boolean isStaffReply) {
        this.author = author;
        this.content = content;
        this.isStaffReply = isStaffReply;
    }

    public static SupportReply create(User author, String content) {
        boolean staff = author != null && author.getRole() != null
                && author.getRole().name().equals("ADMIN");
        return new SupportReply(author, content, staff);
    }

    void assignTicket(SupportTicket ticket) {
        this.ticket = ticket;
    }
}
