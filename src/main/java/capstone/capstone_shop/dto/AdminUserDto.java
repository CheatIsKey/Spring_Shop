package capstone.capstone_shop.dto;

import capstone.capstone_shop.domain.Address;
import capstone.capstone_shop.domain.User;
import capstone.capstone_shop.domain.UserRole;

public record AdminUserDto (
        Long id, String idUser, String name, String phone, UserRole role,
        String state, String city, String street) {

    public static AdminUserDto from(User u) {
        Address address = u.getAddress();

        return new AdminUserDto(
                u.getId(), u.getIdUser(), u.getName(), u.getPhone(), u.getRole(),
                address != null ? address.getState() : null,
                address != null ? address.getCity() : null,
                address != null ? address.getStreet() : null
        );
    }
}
