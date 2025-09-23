package capstone.capstone_shop.domain;

import capstone.capstone_shop.repository.UserRepository;
import capstone.capstone_shop.repository.UserRepository_old;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String phone;

    @Column(name = "id_user", nullable = false, unique = true)
    private String idUser;

    private String password;

    @OneToMany(mappedBy = "user")
    private List<Order> orders = new ArrayList<>();

    @Embedded
    private Address address;

    @Enumerated(value = EnumType.STRING)
    private UserRole role;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "favorite_cart",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "favorite")
    private Set<String> favorite = new HashSet<>();

    public static User createUser(String name, String phone, String idUser, String password,
                                  Address address, UserRole role) {

        User user = new User();
        user.name = name;
        user.phone = phone;
        user.idUser = idUser;
        user.password = password;
        user.address = address;
        user.role = role;

        return user;
    }

    public void changeName(String name){
        this.name = name;
    }
    public void changePhone(String phone){
        this.phone = phone;
    }
    public void changeIdUser(String idUser){
        this.idUser = idUser;
    }
    public void changePassword(String password){
        this.password = password;
    }
    public void changeAddress(Address address){
        this.address = address;
    }

}
