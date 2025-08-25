package capstone.capstone_shop.repository;

import capstone.capstone_shop.domain.UserRole;
import capstone.capstone_shop.dto.AdminUserDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static capstone.capstone_shop.domain.QUser.user;

@Repository
public class UserRepositoryImpl implements UserRepositoryQueryDSL {

    private final JPAQueryFactory queryFactory;

    public UserRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<AdminUserDto> search(String keyword, UserRole role, Pageable pageable) {
        BooleanBuilder where = new BooleanBuilder();

        if (role != null) {
            where.and(user.role.eq(role));
        }
        if (keyword != null && !keyword.isBlank()) {
            String word = keyword.trim();
            where.and(
                    user.name.containsIgnoreCase(word)
                            .or(user.idUser.containsIgnoreCase(word))
                            .or(user.phone.containsIgnoreCase(word))
            );
        }

        List<AdminUserDto> content = queryFactory
                .select(Projections.constructor(
                        AdminUserDto.class,
                        user.id,
                        user.idUser,
                        user.name,
                        user.phone,
                        user.role,
                        user.address.state,
                        user.address.city,
                        user.address.street
                ))
                .from(user)
                .where(where)
                .orderBy(user.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(user.count())
                .from(user)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
