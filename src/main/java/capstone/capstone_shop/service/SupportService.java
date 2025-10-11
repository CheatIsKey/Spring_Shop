package capstone.capstone_shop.service;

import capstone.capstone_shop.domain.*;
import capstone.capstone_shop.dto.*;
import capstone.capstone_shop.repository.AttachmentRepository;
import capstone.capstone_shop.repository.SupportReplyRepository;
import capstone.capstone_shop.repository.SupportTicketRepository;
import capstone.capstone_shop.service.storage.ImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupportService {

    private final SupportTicketRepository ticketRepo;
    private final SupportReplyRepository replyRepo;
    private final AttachmentRepository attRepo;

    // ✅ GCS 전용 빈 대신 공통 인터페이스 사용
    private final ImageStorage imageStorage;

    public Long createTicket(User user, TicketCreateRequest req) {
        SupportTicket ticket = new SupportTicket(user, req.title(), req.category(), req.content(), req.isPrivate());

        if (req.files() != null) {
            for (MultipartFile file : req.files()) {
                if (file == null || file.isEmpty()) continue;
                try {
                    // ✅ 로컬/클라우드와 무관하게 동일 호출
                    String url = imageStorage.upload(file);

                    Attachment att = Attachment.of(
                            file.getOriginalFilename(),
                            url,
                            file.getContentType(),
                            file.getSize()
                    );
                    ticket.addAttachment(att);
                } catch (RuntimeException e) {
                    throw new RuntimeException("파일 업로드 실패: " + file.getOriginalFilename(), e);
                }
            }
        }
        ticketRepo.save(ticket);
        return ticket.getId();
    }

    @Transactional(readOnly = true)
    public Page<TicketSummaryDto> getMyTickets(Long userId, Pageable pageable) {
        return ticketRepo.findByUserIdOrderByCreatedAtDescIdDesc(userId, pageable)
                .map(t -> new TicketSummaryDto(
                        t.getId(), t.getTitle(), t.getCategory(), t.isPrivate(),
                        t.getStatus(), t.getCreatedAt()
                ));
    }

    @Transactional(readOnly = true)
    public TicketDetailDto getMyTicketDetail(Long userId, Long ticketId) {
        SupportTicket ticket = ticketRepo.findByIdAndUserId(ticketId, userId)
                .orElseThrow(() -> new IllegalArgumentException("티켓을 찾을 수 없거나 접근 권한이 없습니다."));
        return toDetailDto(ticket);
    }

    @Transactional(readOnly = true)
    public Page<TicketSummaryDto> searchForAdmin(TicketSearchCond cond, Pageable pageable) {
        return ticketRepo.searchForAdmin(cond.status(), cond.category(), cond.keyword(), pageable)
                .map(t -> new TicketSummaryDto(
                        t.getId(), t.getTitle(), t.getCategory(), t.isPrivate(),
                        t.getStatus(), t.getCreatedAt()
                ));
    }

    @Transactional(readOnly = true)
    public TicketDetailDto getAdminTicketDetail(Long ticketId) {
        SupportTicket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("티켓을 찾을 수 없습니다."));
        return toDetailDto(ticket);
    }

    public Long addReplyAsAdmin(User admin, Long ticketId, ReplyCreateRequest request) {
        SupportTicket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("티켓을 찾을 수 없습니다."));
        SupportReply reply = SupportReply.create(admin, request.content());
        ticket.addReply(reply);
        replyRepo.save(reply);
        return reply.getId();
    }

    public void changeStatus(Long ticketId, TicketStatus status) {
        int updated = ticketRepo.updateStatus(ticketId, status);
        if (updated == 0) throw new IllegalArgumentException("상태 변경 대상이 없습니다.");
    }

    private TicketDetailDto toDetailDto(SupportTicket ticket) {
        List<AttachmentDto> atts = attRepo.findByTicketId(ticket.getId()).stream()
                .map(a -> new AttachmentDto(a.getId(), a.getFileName(), a.getUrl(), a.getMimeType(), a.getSize()))
                .toList();

        List<ReplyDto> replies = replyRepo.findByTicketIdOrderByCreatedAtAsc(ticket.getId()).stream()
                .map(r -> new ReplyDto(
                        r.getId(),
                        r.getAuthor().getId(),
                        r.getAuthor().getName(),
                        r.isStaffReply(),
                        r.getContent(),
                        r.getCreatedAt(),
                        r.getUpdatedAt()
                )).toList();

        return new TicketDetailDto(
                ticket.getId(), ticket.getUser().getId(), ticket.getUser().getName(),
                ticket.getTitle(), ticket.getCategory(), ticket.isPrivate(),
                ticket.getStatus(), ticket.getContent(), ticket.getCreatedAt(),
                ticket.getUpdatedAt(), atts, replies
        );
    }
}
