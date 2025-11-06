package capstone.capstone_shop.repository;

import capstone.capstone_shop.domain.SupportTicket;
import capstone.capstone_shop.domain.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long>, SupportTicketQRepository {

    @EntityGraph(attributePaths = {"user"})
    Optional<SupportTicket> findById(Long id);

    Page<SupportTicket> findByUserIdOrderByCreatedAtDescIdDesc(Long userId, Pageable page);

    Optional<SupportTicket> findByIdAndUserId(Long id, Long userId);

    long countByStatus(TicketStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update SupportTicket t set t.status = :status where t.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") TicketStatus status);

    boolean existsByUser_Id(Long userId);
}
