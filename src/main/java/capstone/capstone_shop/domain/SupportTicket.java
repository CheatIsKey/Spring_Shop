package capstone.capstone_shop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(
        name = "support_ticket",
        indexes = {
                @Index(name = "idx_ticket_user", columnList = "user_id"),
                @Index(name = "idx_ticket_status", columnList = "status"),
                @Index(name = "idx_ticket_created_at", columnList = "created_at")
        }
)
public class SupportTicket extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketCategory category;

    @Column(name = "content", columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "is_private", nullable = false)
    private boolean isPrivate = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.OPEN;

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt asc")
    private List<SupportReply> replies = new ArrayList<>();

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    protected SupportTicket() {}

    public SupportTicket(User user, String title, TicketCategory category, String content, boolean isPrivate) {
        this.user = user;
        this.title = title;
        this.category = category;
        this.content = content;
        this.isPrivate = isPrivate;
    }

    public void addReply(SupportReply reply) {
        replies.add(reply);
        reply.assignTicket(this);
        if (reply.isStaffReply()) this.status = TicketStatus.ANSWERED;
    }

    public void addAttachment(Attachment att) {
        attachments.add(att);
        att.assignTicket(this);
    }

    public void close() {
        this.status = TicketStatus.CLOSED;
    }

    public boolean manageableBy(User actor) {
        if (actor == null) return false;
        if (actor.getId().equals(this.user.getId())) return true;
        return actor.getRole() != null && actor.getRole().name().equals("ADMIN");
    }
}
