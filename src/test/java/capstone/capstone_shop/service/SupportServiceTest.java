package capstone.capstone_shop.service;

import capstone.capstone_shop.AuditingTestConfig;
import capstone.capstone_shop.SupportTestConfig;
import capstone.capstone_shop.domain.TicketCategory;
import capstone.capstone_shop.domain.TicketStatus;
import capstone.capstone_shop.domain.User;
import capstone.capstone_shop.domain.UserRole;
import capstone.capstone_shop.dto.ReplyCreateRequest;
import capstone.capstone_shop.dto.TicketCreateRequest;
import capstone.capstone_shop.dto.TicketDetailDto;
import capstone.capstone_shop.repository.SupportTicketRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "file.upload-dir=/tmp/uploads",
        "app.storage=local",
        "gcs.enabled=false",
        "spring.cloud.gcp.storage.bucket=test-bucket"
})
@ActiveProfiles("local")
@Import({SupportTestConfig.class, AuditingTestConfig.class})
@Transactional
class SupportServiceTest {

    @Autowired SupportService service;
    @Autowired SupportTicketRepository ticketRepo;
    @Autowired EntityManager em;

    @Test
    public void 티켓_생성_및_조회() throws Exception {
        // given
        User user = User.createUser("test", "010-1234-5678", "test", "testtest", null, UserRole.CLIENT);
        em.persist(user);

        // when
        Long id = service.createTicket(user, new TicketCreateRequest("제목",
                TicketCategory.ITEM, "내용", false, List.of()));

        em.flush();
        em.clear();

        // then
        TicketDetailDto dto = service.getMyTicketDetail(user.getId(), id);
        assertThat(dto.title()).isEqualTo("제목");
        assertThat(dto.status()).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    public void 관리자_답변시_상태_ANSWERED() throws Exception {
        // given
        User user = User.createUser("test", "010-1234-5678", "test", "testtest", null, UserRole.CLIENT);
        User admin = User.createUser("admin", "010-1234-5678", "admin1", "admin", null, UserRole.ADMIN);
        em.persist(user);
        em.persist(admin);

        Long id = service.createTicket(user, new TicketCreateRequest("A",
                TicketCategory.DELIVERY, "내용", false, List.of()));

        // when
        service.addReplyAsAdmin(admin, id, new ReplyCreateRequest("처리 완료"));

        // then
        assertThat(ticketRepo.findById(id).orElseThrow().getStatus())
                .isEqualTo(TicketStatus.ANSWERED);
    }

    @Test
    public void 상태_변경_CLOSED() throws Exception {
        // given
        User user = User.createUser("test", "010-1234-5678", "test", "testtest", null, UserRole.CLIENT);
        em.persist(user);

        Long id = service.createTicket(user, new TicketCreateRequest("A",
                TicketCategory.DELIVERY, "내용", false, List.of()));

        // when
        service.changeStatus(id, TicketStatus.CLOSED);

        // then
        assertThat(ticketRepo.findById(id).orElseThrow().getStatus())
                .isEqualTo(TicketStatus.CLOSED);
    }
}