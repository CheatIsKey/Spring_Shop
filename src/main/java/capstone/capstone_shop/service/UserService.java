package capstone.capstone_shop.service;

import capstone.capstone_shop.domain.Address;
import capstone.capstone_shop.domain.User;
import capstone.capstone_shop.domain.UserRole;
import capstone.capstone_shop.dto.CreateUserRequest;
import capstone.capstone_shop.dto.UpdateUserRequest;
import capstone.capstone_shop.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원 가입
    @Transactional
    public Long join(User user){
        validateDuplicateUser(user);
        userRepository.save(user);
        return user.getId();
    }

    private void validateDuplicateUser(User user) {
        userRepository.findByIdUser(user.getIdUser())
                .ifPresent(u -> {
                    throw new IllegalStateException("이미 존재하는 회원입니다.");
                });
    }

    // 회원 전체 조회
    public List<User> findUsers() {
        return userRepository.findAll();
    }

    // 회원 한 명 조회
    public User findOne(Long userId) {
        return userRepository.findById(userId).get();
    }

    // 회원 가입
    @Transactional
    public Long register(CreateUserRequest request) {
        String name = request.getName().trim();
        String phone = request.getPhone().trim();
        String idUser = request.getIdUser().trim();
        String rawPw = request.getPassword().trim();

        userRepository.findByIdUser(idUser).ifPresent(user -> {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        });

        Address address = new Address(
                request.getState().trim(),
                request.getCity().trim(),
                request.getStreet().trim()
        );

        String encoded = passwordEncoder.encode(rawPw);

        User user = User.createUser(
                name, phone, idUser, encoded, address, UserRole.CLIENT
        );

        userRepository.save(user);
        return user.getId();
    }

    @Transactional
    public void update(Long id, String name) {
        User user = userRepository.findById(id).get();
        user.changeName(name);
    }

    @Transactional
    public User updateUserInfo(Long id, UpdateUserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다."));

        user.changeName(req.getName().trim());
        user.changePhone(req.getPhone().trim());

        String newIdUser = req.getIdUser().trim();
        if (!newIdUser.equals(user.getIdUser())) {
            userRepository.findByIdUser(newIdUser).ifPresent(u ->{
                if (!u.getId().equals(user.getId())){
                    throw new IllegalStateException("이미 존재하는 아이디입니다.");
                }
            });
            user.changeIdUser(newIdUser);
        }

        // newPassword가 비어있지 않은 경우에만 변경
        if (req.getNewPassword() != null && !req.getNewPassword().isBlank()) {
            String encoded = passwordEncoder.encode(req.getNewPassword().trim());
            user.changePassword(encoded);
        }

        Address addr = new Address(
                req.getState().trim(),
                req.getCity().trim(),
                req.getStreet().trim()
        );
        user.changeAddress(addr);

        return user;
    }

    @Transactional
    public User login(String idUser, String password) {
        User user = userRepository.findByIdUser(idUser)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }
        return user;
    }
}
