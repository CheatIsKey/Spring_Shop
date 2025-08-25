package capstone.capstone_shop.controller;

import capstone.capstone_shop.domain.Address;
import capstone.capstone_shop.domain.User;
import capstone.capstone_shop.domain.UserRole;
import capstone.capstone_shop.dto.CreateUserRequest;
import capstone.capstone_shop.dto.LoginUserDto;
import capstone.capstone_shop.dto.LoginUserRequest;
import capstone.capstone_shop.dto.UpdateUserRequest;
import capstone.capstone_shop.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserFormController {

    private final UserService userService;

    // 사용자 계정 생성 //
    @GetMapping("/new")
    public String createForm(Model model){
        model.addAttribute("createUserRequest", new CreateUserRequest());
        return "users/createUserForm";
    }

    @PostMapping("/new")
    public String createForm(@ModelAttribute @Valid CreateUserRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes attrs) {

        if (bindingResult.hasErrors()){
            return "users/createUserForm";
        }

        try {
            userService.register(request);
        } catch (IllegalStateException ex) {
            bindingResult.reject("duplicate", ex.getMessage());
            return "users/createUserForm";
        }

        attrs.addFlashAttribute("flashMessage", "회원가입이 완료되었습니다.");
        return "redirect:/";
    }

    // 사용자 정보 변경 폼 //
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userService.findOne(id);
        UpdateUserRequest dto = new UpdateUserRequest(
                user.getName(), user.getPhone(), user.getIdUser(),
                "",
                user.getAddress().getState(),
                user.getAddress().getCity(),
                user.getAddress().getStreet()
        );
        model.addAttribute("id", id);
        model.addAttribute("updateUserRequest", dto);
        return "users/editUserForm";
    }

//    @PutMapping("/{id}/edit")
    public String updateUser(
            @PathVariable Long id,
            @ModelAttribute("updateUserRequest") @Valid UpdateUserRequest request,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes attrs){
        if (bindingResult.hasErrors()) {
            return "users/editUserForm";
        }
        User updated = userService.updateUserInfo(id, request);
        session.setAttribute("loginUser", updated);
        attrs.addFlashAttribute("flashMessage", "변경이 완료되었습니다.");
        return "redirect:/";
    }

    @PostMapping("/{id}/edit")
    public String updateUserPost(
            @PathVariable Long id,
            @ModelAttribute @Valid UpdateUserRequest request,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes attrs
    ) {
        if (bindingResult.hasErrors()) {
            return "users/editUserForm";
        }

        User updated = userService.updateUserInfo(id, request);

        LoginUserDto loginUser = new LoginUserDto(updated.getId(), updated.getName(), updated.getIdUser(), updated.getRole());
        session.setAttribute("loginUser", loginUser);

        attrs.addFlashAttribute("flashMessage", "변경이 완료되었습니다.");
        return "redirect:/";
    }

    // 마이페이지 구현 //
    @GetMapping("/mypage")
    public String myPage(HttpSession session, Model model) {
        LoginUserDto loginUser = (LoginUserDto) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("loginUser", loginUser);
        return "users/myPage";
    }
}
