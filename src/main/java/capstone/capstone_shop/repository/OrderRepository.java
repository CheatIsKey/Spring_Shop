package capstone.capstone_shop.repository;

import capstone.capstone_shop.domain.Order;
import capstone.capstone_shop.domain.OrderStatus;
//import capstone.capstone_shop.domain.QOrder;
//import capstone.capstone_shop.domain.QUser;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"orderItems", "orderItems.item", "delivery"})
    List<Order> findByUserIdOrderByIdDesc(Long userId);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.item", "delivery"})
    Optional<Order> findWithItemsAndDeliveryById(Long id);

//    private List<Order> findAll(OrderSearch orderSearch) {
//        QOrder order = QOrder.order;
//        QUser user = QUser.user;
//
//        JPAQueryFactory query = new JPAQueryFactory(em);
//
//        return query.select(order)
//                    .from(order)
//                    .join(order.user, user)
//                    .where(statusEq(orderSearch.getOrderStatus()))
//                    .limit(1000)
//                    .fetch();
//    }
//
//    private BooleanExpression statusEq(OrderStatus orderStatus) {
//        if (orderStatus == null)
//            return null;
//        return QOrder.order.status.eq(orderStatus);
//    }


}
