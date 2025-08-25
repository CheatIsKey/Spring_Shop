package capstone.capstone_shop.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginUserRequest {

    @NotEmpty
    private String idUser;
    @NotEmpty
    private String password;
}
