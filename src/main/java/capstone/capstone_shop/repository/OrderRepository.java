package capstone.capstone_shop.repository;

import capstone.capstone_shop.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 주문 목록: 배송 상태만 필요 → delivery만 fetch
    @Query("""
      select distinct o
      from Order o
      left join fetch o.delivery
      where o.user.id = :userId
      order by o.id desc
    """)
    List<Order> findSummaryByUserId(@Param("userId") Long userId);

    // 주문 상세: delivery + orderItems + item 모두 fetch
    @Query("""
      select distinct o
      from Order o
      left join fetch o.delivery
      left join fetch o.orderItems oi
      left join fetch oi.item i
      where o.id = :id
    """)
    Optional<Order> findDetailById(@Param("id") Long id);
}
