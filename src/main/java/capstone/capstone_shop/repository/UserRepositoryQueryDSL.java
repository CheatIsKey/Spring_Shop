package capstone.capstone_shop.repository;

import capstone.capstone_shop.domain.UserRole;
import capstone.capstone_shop.dto.AdminUserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryQueryDSL {
    Page<AdminUserDto> search(String keyword, UserRole role, Pageable pageable);
}
