package capstone.capstone_shop.repository;

import capstone.capstone_shop.domain.SupportTicket;
import capstone.capstone_shop.domain.TicketCategory;
import capstone.capstone_shop.domain.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupportTicketQRepository {

    Page<SupportTicket> searchForAdmin(TicketStatus status, TicketCategory category,
                                       String keyword, Pageable pageable);
}
