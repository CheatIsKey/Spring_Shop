package capstone.capstone_shop.api;

import capstone.capstone_shop.domain.Address;
import capstone.capstone_shop.domain.User;
import capstone.capstone_shop.domain.UserRole;
import capstone.capstone_shop.dto.CreateUserRequest;
import capstone.capstone_shop.dto.UpdateUserRequest;
import capstone.capstone_shop.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;

    @PostMapping
    public CreateUserResponse createUser(@RequestBody @Valid CreateUserRequest request){
        Address address = new Address(
                request.getState(),
                request.getCity(),
                request.getStreet()
        );

        User user = User.createUser(
                request.getName(),
                request.getPhone(),
                request.getIdUser(),
                request.getPassword(),
                address,
                UserRole.CLIENT
        );

        Long id = userService.join(user);
        return new CreateUserResponse(id);
    }

    @Data
    @AllArgsConstructor
    private class CreateUserResponse {
        private Long id;
    }


    @PutMapping("/{id}")
    public UpdateUserResponse updateUser(@PathVariable("id") Long id,
                                         @RequestBody @Valid UpdateUserRequest request){
        userService.updateUserInfo(id, request);
        User u = userService.findOne(id);
        Address addr = u.getAddress();

        return new UpdateUserResponse(
                u.getId(),
                u.getName(),
                u.getPhone(),
                u.getIdUser(),
                new AddressDto(addr.getState(), addr.getCity(), addr.getStreet()));
    }

    @Data
    @AllArgsConstructor
    private class UpdateUserResponse {

        private Long id;
        private String name;
        private String phone;
        private String idUser;
        private AddressDto address;

    }

    @Data
    @AllArgsConstructor
    private class AddressDto {
        private String state;
        private String city;
        private String street;
    }

    @GetMapping
    public Result userList() {
        List<User> users = userService.findUsers();
        List<UserDTO> collect = users.stream()
                .map(u -> new UserDTO(u.getName()))
                .collect(Collectors.toList());

        return new Result(collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class UserDTO {
        private String name;
    }
}