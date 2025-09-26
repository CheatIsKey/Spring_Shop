package capstone.capstone_shop.repository;

import capstone.capstone_shop.domain.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select i.id from Item i")
    List<Long> findAllItemIds();

    // 특정 카테고리의 아이템 목록을 화면에서 곧바로 쓰려면 연관까지 미리 로딩
    @Query("""
      select distinct i
      from Item i
      join fetch i.categoryItems ci
      join fetch ci.category c
      where c.id = :catId
      order by i.id desc
    """)
    List<Item> findByCategoryId(@Param("catId") Long categoryId);

    List<Item> findByNameContaining(String name);

    // 아이템 전체 목록 화면(아이템 + 소속 카테고리들까지)
    @Query("""
      select distinct i
      from Item i
      left join fetch i.categoryItems ci
      left join fetch ci.category c
      order by i.id desc
    """)
    List<Item> findAllWithCategory();
}
