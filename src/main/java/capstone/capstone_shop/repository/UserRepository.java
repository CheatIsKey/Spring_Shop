package capstone.capstone_shop.repository;

import capstone.capstone_shop.domain.User;
import capstone.capstone_shop.dto.UserDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Member;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryQueryDSL {

    List<User> findByName(String name);

    List<User> findByPhone(String phone);

    Optional<User> findByIdUser(String idUser);

    boolean existsByIdUser(String idUser);
}
