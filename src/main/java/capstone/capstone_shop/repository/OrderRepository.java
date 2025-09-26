package capstone.capstone_shop.repository;

import capstone.capstone_shop.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 주문 목록: 총액 계산에 orderItems 필요 → 함께 fetch
    @Query("""
      select distinct o
      from Order o
      left join fetch o.delivery
      left join fetch o.orderItems oi
      left join fetch oi.item
      where o.user.id = :userId
      order by o.id desc
    """)
    List<Order> findSummaryByUserId(@Param("userId") Long userId);

    // 주문 상세: 그대로 OK (이미 전부 fetch)
    @Query("""
      select distinct o
      from Order o
      left join fetch o.delivery
      left join fetch o.orderItems oi
      left join fetch oi.item
      where o.id = :id
    """)
    Optional<Order> findDetailById(@Param("id") Long id);
}
