package capstone.capstone_shop.repository;

import capstone.capstone_shop.domain.SupportReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportReplyRepository extends JpaRepository<SupportReply, Long> {
    List<SupportReply> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
