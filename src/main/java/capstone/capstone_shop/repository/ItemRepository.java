package capstone.capstone_shop.repository;

import capstone.capstone_shop.domain.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select i.id from Item i ")
    List<Long> findAllItemIds();

    @Query("select distinct i from Item i join i.categoryItems ci " +
            "where ci.category.id = :catId")
    List<Item> findByCategoryId(@Param("catId") Long categoryId);

    List<Item> findByNameContaining(String name);

      // 상품 검색 구버전 //
//    @Query("select i from Item i where i.name Like :name")
//    List<Item> findByContainingName(@Param("name") String name);


}
