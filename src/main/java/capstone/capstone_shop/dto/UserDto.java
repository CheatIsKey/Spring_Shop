package capstone.capstone_shop.dto;

import capstone.capstone_shop.domain.Address;
import capstone.capstone_shop.domain.UserRole;
import lombok.Data;

@Data
public class UserDto {

    String name;
    String phone;
    String idUser;
    String password;
    Address address;
    UserRole role;

    public UserDto(String name, String phone, String idUser, String password, Address address) {
        this.name = name;
        this.phone = phone;
        this.idUser = idUser;
        this.password = password;
        this.address = address;
    }
}
