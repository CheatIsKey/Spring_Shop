package capstone.capstone_shop.repository;

import capstone.capstone_shop.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 홈/목록 화면: 카테고리 + 하위 Category_Item + Item까지 한 방에
    @Query("""
      select distinct c
      from Category c
      left join fetch c.categoryItems ci
      left join fetch ci.item i
      order by c.id
    """)
    List<Category> findAllWithItems();

    // 단일 카테고리 상세 화면(해당 카테고리의 아이템들까지 포함)
    @Query("""
      select c
      from Category c
      left join fetch c.categoryItems ci
      left join fetch ci.item i
      where c.id = :id
    """)
    Optional<Category> findByIdWithItems(@Param("id") Long id);
}
