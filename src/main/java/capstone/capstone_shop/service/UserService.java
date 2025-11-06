package capstone.capstone_shop.service;

import capstone.capstone_shop.domain.Address;
import capstone.capstone_shop.domain.OrderStatus;
import capstone.capstone_shop.domain.User;
import capstone.capstone_shop.domain.UserRole;
import capstone.capstone_shop.dto.CreateUserRequest;
import capstone.capstone_shop.dto.UpdateUserRequest;
import capstone.capstone_shop.repository.OrderRepository;
import capstone.capstone_shop.repository.SupportTicketRepository;
import capstone.capstone_shop.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final OrderRepository orderRepository;
    private final SupportTicketRepository supportTicketRepository;

    // 회원 가입
    @Transactional
    public Long join(User user) {
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
            userRepository.findByIdUser(newIdUser).ifPresent(u -> {
                if (!u.getId().equals(user.getId())) {
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

    // 회원 탈퇴(본인)
    @Transactional
    public void deleteSelf(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다."));

        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("관리자 계정은 이 경로로 탈퇴할 수 없습니다.");
        }

        if (orderRepository.existsByUser_IdAndStatusNot(user.getId(), OrderStatus.CANCEL)) {
            throw new IllegalStateException("진행 중인 주문이 있어 탈퇴할 수 없습니다. 모든 주문을 취소한 뒤 다시 시도하세요.");
        }

        if (supportTicketRepository.existsByUser_Id(user.getId())) {
            throw new IllegalStateException("고객센터 문의글이 남아 있어 탈퇴할 수 없습니다. 문의글을 모두 삭제한 뒤 다시 시도하세요.");
        }

        try {
            userRepository.delete(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("연관 데이터로 인해 삭제할 수 없습니다.");
        }
    }

    // 관리자에 의한 유저 삭제
    @Transactional
    public void deleteByAdmin(Long targetUserId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("대상 유저를 찾을 수 없습니다."));

        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("관리자 계정은 삭제할 수 없습니다.");
        }

        if (orderRepository.existsByUser_IdAndStatusNot(user.getId(), OrderStatus.CANCEL)) {
            throw new IllegalStateException("해당 유저는 진행 중인 주문이 있어 삭제할 수 없습니다. 모든 주문이 취소된 후 삭제 가능합니다.");
        }

        if (supportTicketRepository.existsByUser_Id(user.getId())) {
            throw new IllegalStateException("고객센터 문의글이 남아 있어 삭제할 수 없습니다. 문의글을 모두 삭제한 뒤 다시 시도하세요.");
        }

        try {
            userRepository.delete(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("연관 데이터로 인해 삭제할 수 없습니다.");
        }
    }

}
