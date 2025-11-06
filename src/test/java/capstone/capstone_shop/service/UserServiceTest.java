package capstone.capstone_shop.service;

import capstone.capstone_shop.domain.Address;
import capstone.capstone_shop.domain.User;
import capstone.capstone_shop.domain.UserRole;
import capstone.capstone_shop.dto.CreateUserRequest;
import capstone.capstone_shop.dto.UpdateUserRequest;
import capstone.capstone_shop.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 단위 테스트 (Mockito)
 * - 스프링 컨텍스트 미사용
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    // ======= 헬퍼 =======
    private User user(long id, String name, String phone, String idUser, String pw, Address addr, UserRole role) {
        User u = User.createUser(name, phone, idUser, pw, addr, role);
        setId(User.class, u, "id", id);
        return u;
    }

    private void setId(Class<?> type, Object target, String field, long idVal) {
        try {
            Field f = type.getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, idVal);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ======= join =======
    @Nested
    class JoinTests {

        @Test
        @DisplayName("join: 중복 ID 존재 → 실패")
        void join_duplicate_fail() {
            // given
            User dup = user(1L, "kim", "010", "kimid", "enc", null, UserRole.CLIENT);
            when(userRepository.findByIdUser("kimid")).thenReturn(Optional.of(dup));
            User newUser = User.createUser("kim", "010", "kimid", "enc", null, UserRole.CLIENT);

            // when / then
            assertThatThrownBy(() -> userService.join(newUser))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 존재하는 회원");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("join: 정상 가입 → 성공 (ID 반환)")
        void join_success() {
            // given
            when(userRepository.findByIdUser("leeid")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                setId(User.class, u, "id", 10L);
                return u;
            });
            User newUser = User.createUser("lee", "010", "leeid", "enc", null, UserRole.CLIENT);

            // when
            Long id = userService.join(newUser);

            // then
            assertThat(id).isEqualTo(10L);
            verify(userRepository).save(any(User.class));
        }
    }

    // ======= findUsers / findOne =======
    @Nested
    class FindTests {

        @Test
        @DisplayName("findUsers: 전체 조회")
        void findUsers_all() {
            // given
            var u1 = user(1L, "a", "010", "a1", "p", null, UserRole.CLIENT);
            var u2 = user(2L, "b", "010", "b1", "p", null, UserRole.CLIENT);
            when(userRepository.findAll()).thenReturn(List.of(u1, u2));

            // when
            List<User> list = userService.findUsers();

            // then
            assertThat(list).hasSize(2).extracting(User::getId).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("findOne: 존재하지 않으면 NoSuchElementException")
        void findOne_not_found() {
            // given
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> userService.findOne(99L))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("findOne: 정상 조회")
        void findOne_ok() {
            // given
            var u = user(7L, "x", "010", "x1", "p", null, UserRole.CLIENT);
            when(userRepository.findById(7L)).thenReturn(Optional.of(u));

            // when
            User found = userService.findOne(7L);

            // then
            assertThat(found.getId()).isEqualTo(7L);
        }
    }

    // ======= register (CreateUserRequest) =======
    @Nested
    class RegisterTests {

        @Test
        @DisplayName("register: 중복 ID → 실패")
        void register_duplicate_fail() {
            // given
            CreateUserRequest req = mock(CreateUserRequest.class);

            // register()가 중복 체크 전에 trim()하는 필드 4종을 반드시 스텁
            when(req.getName()).thenReturn(" Park ");
            when(req.getPhone()).thenReturn(" 010-0000-0000 ");
            when(req.getIdUser()).thenReturn(" park1 ");
            when(req.getPassword()).thenReturn(" pw ");

            // 중복 존재
            when(userRepository.findByIdUser("park1"))
                    .thenReturn(Optional.of(user(1L,"a","010","park1","p",null,UserRole.CLIENT)));

            // when / then
            assertThatThrownBy(() -> userService.register(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 존재하는 회원");
            verify(userRepository, never()).save(any());
        }


        @Test
        @DisplayName("register: 정상 등록 → 성공 (ID 반환, 비밀번호 인코딩)")
        void register_success() {
            // given
            CreateUserRequest req = mock(CreateUserRequest.class);
            when(req.getName()).thenReturn(" Park ");
            when(req.getPhone()).thenReturn(" 010-0000-0000 ");
            when(req.getIdUser()).thenReturn(" park1 ");
            when(req.getPassword()).thenReturn(" pw ");
            when(req.getState()).thenReturn(" 서울 ");
            when(req.getCity()).thenReturn(" 강남 ");
            when(req.getStreet()).thenReturn(" 테헤란로 ");

            when(userRepository.findByIdUser("park1")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("pw")).thenReturn("ENC(pw)");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                setId(User.class, u, "id", 11L);
                return u;
            });

            // when
            Long id = userService.register(req);

            // then
            assertThat(id).isEqualTo(11L);
            verify(passwordEncoder).encode("pw");
            verify(userRepository).save(any(User.class));
        }
    }

    // ======= update (name only) =======
    @Nested
    class UpdateNameTests {

        @Test
        @DisplayName("update: 이름 변경")
        void update_name() {
            // given
            var u = user(3L, "old", "010", "uid", "p", null, UserRole.CLIENT);
            when(userRepository.findById(3L)).thenReturn(Optional.of(u));

            // when
            userService.update(3L, "newName");

            // then
            assertThat(u.getName()).isEqualTo("newName");
        }
    }

    // ======= updateUserInfo (UpdateUserRequest) =======
    @Nested
    class UpdateUserInfoTests {

        @Test
        @DisplayName("updateUserInfo: 대상 유저 없음 → EntityNotFoundException")
        void updateUserInfo_target_not_found() {
            // given
            UpdateUserRequest req = mock(UpdateUserRequest.class);
            when(userRepository.findById(77L)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> userService.updateUserInfo(77L, req))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("해당 유저를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("updateUserInfo: idUser 충돌(다른 유저가 사용중) → 실패")
        void updateUserInfo_conflict_fail() {
            // given
            var current = user(5L, "me", "010", "me_id", "ENC(old)",
                    new Address("서울","강남","old"), UserRole.CLIENT);
            when(userRepository.findById(5L)).thenReturn(Optional.of(current));

            UpdateUserRequest req = mock(UpdateUserRequest.class);
            // 실제로 호출되는 Getter만 스텁
            when(req.getName()).thenReturn(" Me ");
            when(req.getPhone()).thenReturn(" 010-1234-5678 ");
            when(req.getIdUser()).thenReturn(" other_id ");

            var other = user(9L, "other", "010", "other_id", "ENC", null, UserRole.CLIENT);
            when(userRepository.findByIdUser("other_id")).thenReturn(Optional.of(other));

            // when / then
            assertThatThrownBy(() -> userService.updateUserInfo(5L, req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 존재하는 아이디");
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("updateUserInfo: 기본 정보/주소 변경, 비번 미변경 → 성공")
        void updateUserInfo_success_without_password_change() {
            // given
            var current = user(5L, "me", "010", "me_id", "ENC(old)",
                    new Address("서울","강남","old"), UserRole.CLIENT);
            when(userRepository.findById(5L)).thenReturn(Optional.of(current));

            UpdateUserRequest req = mock(UpdateUserRequest.class);
            when(req.getName()).thenReturn(" NewName ");
            when(req.getPhone()).thenReturn(" 010-2222-2222 ");
            when(req.getIdUser()).thenReturn(" me_id "); // 동일 → 변경 안 함
            when(req.getNewPassword()).thenReturn(" ");   // 공백 → 변경 안 함
            when(req.getState()).thenReturn(" 서울 ");
            when(req.getCity()).thenReturn(" 송파 ");
            when(req.getStreet()).thenReturn(" 올림픽로 ");

            // when
            User updated = userService.updateUserInfo(5L, req);

            // then
            assertThat(updated.getName()).isEqualTo("NewName");
            assertThat(updated.getPhone()).isEqualTo("010-2222-2222");
            assertThat(updated.getIdUser()).isEqualTo("me_id");
            assertThat(updated.getAddress().getCity()).isEqualTo("송파");
            assertThat(updated.getPassword()).isEqualTo("ENC(old)");
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("updateUserInfo: idUser 변경 + 비번 변경 → 성공(인코딩 호출)")
        void updateUserInfo_success_with_password_change() {
            // given
            var current = user(6L, "me", "010", "me_id", "ENC(old)",
                    new Address("서울","강남","old"), UserRole.CLIENT);
            when(userRepository.findById(6L)).thenReturn(Optional.of(current));

            UpdateUserRequest req = mock(UpdateUserRequest.class);
            when(req.getName()).thenReturn(" New ");
            when(req.getPhone()).thenReturn(" 010-9999-9999 ");
            when(req.getIdUser()).thenReturn(" new_id "); // 변경됨
            when(req.getNewPassword()).thenReturn(" newPw ");
            when(req.getState()).thenReturn(" 경기 ");
            when(req.getCity()).thenReturn(" 성남 ");
            when(req.getStreet()).thenReturn(" 판교로 ");

            when(userRepository.findByIdUser("new_id")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("newPw")).thenReturn("ENC(newPw)");

            // when
            User updated = userService.updateUserInfo(6L, req);

            // then
            assertThat(updated.getIdUser()).isEqualTo("new_id");
            assertThat(updated.getPassword()).isEqualTo("ENC(newPw)");
            assertThat(updated.getAddress().getState()).isEqualTo("경기");
            verify(passwordEncoder).encode("newPw");
        }
    }

    // ======= login =======
    @Nested
    class LoginTests {

        @Test
        @DisplayName("login: 아이디 없음 → 실패")
        void login_id_not_found_fail() {
            // given
            when(userRepository.findByIdUser("nouser")).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> userService.login("nouser", "pw"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 아이디");
        }

        @Test
        @DisplayName("login: 비밀번호 불일치 → 실패")
        void login_wrong_password_fail() {
            // given
            var u = user(1L, "kim", "010", "kimid", "ENC(pw)", null, UserRole.CLIENT);
            when(userRepository.findByIdUser("kimid")).thenReturn(Optional.of(u));
            when(passwordEncoder.matches("wrong", "ENC(pw)")).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> userService.login("kimid", "wrong"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비밀번호가 올바르지 않습니다");
        }

        @Test
        @DisplayName("login: 정상 로그인 → 성공(유저 반환)")
        void login_success() {
            // given
            var u = user(1L, "kim", "010", "kimid", "ENC(pw)", null, UserRole.CLIENT);
            when(userRepository.findByIdUser("kimid")).thenReturn(Optional.of(u));
            when(passwordEncoder.matches("pw", "ENC(pw)")).thenReturn(true);

            // when
            User loggedIn = userService.login("kimid", "pw");

            // then
            assertThat(loggedIn.getId()).isEqualTo(1L);
        }
    }
}
