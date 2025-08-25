package capstone.capstone_shop.repository;

import capstone.capstone_shop.domain.item.Item;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository_old {

    private final EntityManager em;

    public void save(Item item){
        if (item.getId() == null){
            em.persist(item);
        } else {
            em.merge(item);
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select I from Item I", Item.class)
                .getResultList();
    }

    public List<Item> findByName(String name) {
        return em.createQuery("select I from Item I where I.name = :name", Item.class)
                .setParameter("name", name)
                .getResultList();
    }
}
