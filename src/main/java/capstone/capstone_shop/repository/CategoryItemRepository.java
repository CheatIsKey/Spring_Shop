package capstone.capstone_shop.repository;

import capstone.capstone_shop.domain.Category_Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryItemRepository extends JpaRepository<Category_Item, Long> {

}
