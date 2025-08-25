package capstone.capstone_shop.dto;

import capstone.capstone_shop.domain.Address;
import capstone.capstone_shop.domain.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @NotBlank @Size(min = 2, max = 4)
    String name;

    @NotBlank
    @Pattern(regexp = "^01[0-9]-?\\d{3,4}-?\\d{4}$", message="휴대폰 번호 형식이 아닙니다.")
    String phone;

    @NotBlank
    String idUser;

    @Pattern(regexp = "^$|.{6,20}$", message = "비밀번호는 6 ~ 20자입니다.")
    private String newPassword;

    @NotBlank
    private String state;

    @NotBlank
    private String city;

    @NotBlank
    private String street;

}
