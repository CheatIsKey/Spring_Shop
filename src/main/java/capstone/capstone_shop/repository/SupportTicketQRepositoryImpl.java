package capstone.capstone_shop.repository;

import capstone.capstone_shop.domain.SupportTicket;
import capstone.capstone_shop.domain.TicketCategory;
import capstone.capstone_shop.domain.TicketStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static capstone.capstone_shop.domain.QSupportTicket.supportTicket;
import static capstone.capstone_shop.domain.QUser.user;

@RequiredArgsConstructor
public class SupportTicketQRepositoryImpl implements SupportTicketQRepository {

    private final JPAQueryFactory query;

    @Override
    public Page<SupportTicket> searchForAdmin(TicketStatus status, TicketCategory category, String keyword, Pageable pageable) {

        BooleanBuilder where = new BooleanBuilder();
        if (status != null) where.and(supportTicket.status.eq(status));
        if (category != null) where.and(supportTicket.category.eq(category));
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim() + "%";
            where.and(
                    supportTicket.title.likeIgnoreCase(like)
                            .or(supportTicket.content.likeIgnoreCase(like))
                            .or(supportTicket.user.idUser.likeIgnoreCase(like))
                            .or(supportTicket.user.name.likeIgnoreCase(like))
            );
        }

        List<SupportTicket> content = query
                .selectFrom(supportTicket)
                .join(supportTicket.user, user).fetchJoin()
                .where(where)
                .orderBy(supportTicket.createdAt.desc(), supportTicket.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = query
                .select(supportTicket.count())
                .from(supportTicket)
                .join(supportTicket.user, user)
                .where(where)
                .fetchOne();

        long totalCount = (total == null ? 0L : total);
        return new PageImpl<>(content, pageable, totalCount);
    }
}
