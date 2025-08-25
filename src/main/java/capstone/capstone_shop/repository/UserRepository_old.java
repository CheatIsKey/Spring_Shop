package capstone.capstone_shop.repository;

import capstone.capstone_shop.domain.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepository_old {

    private final EntityManager em;

    public void save(User user){
        em.persist(user);
    }

    public User findOne(Long id){
        return em.find(User.class, id);
    }

    public List<User> findAll(){
        return em.createQuery("select u from User u", User.class)
                .getResultList();
    }

    public List<User> findByIdUser(String idUser){
        return em.createQuery("select u from User u where u.idUser = :idUser", User.class)
                .setParameter("idUser", idUser)
                .getResultList();
    }
}
