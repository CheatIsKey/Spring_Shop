package capstone.capstone_shop.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotEmpty
    @Size(max = 4, message = "이름은 최대 4자까지 입력 가능합니다.")
    private String name;

    @NotEmpty
    @Pattern(regexp = "^01[0-9]-?\\d{3,4}-?\\d{4}$", message="휴대폰 번호 형식이 아닙니다.")
    private String phone;

    @NotEmpty
    private String idUser;

    @NotEmpty
    @Size(min = 6, max = 20, message = "비밀번호는 6 ~ 20자입니다.")
    private String password;

    @NotEmpty
    private String state;
    @NotEmpty
    private String city;
    @NotEmpty
    private String street;
}
