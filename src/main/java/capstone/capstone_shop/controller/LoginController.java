package capstone.capstone_shop.controller;

import capstone.capstone_shop.domain.User;
import capstone.capstone_shop.dto.LoginUserDto;
import capstone.capstone_shop.dto.LoginUserRequest;
import capstone.capstone_shop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    // 로그인 폼
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginUserRequest", new LoginUserRequest());
        return "users/loginUserForm";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginUserRequest") LoginUserRequest req,
            BindingResult bindingResult,
            Model model, HttpServletRequest request) {

        // 입력 검증 오류가 있으면 폼으로
        if (bindingResult.hasErrors()) {
            return "users/loginUserForm";
        }

        try {
            User u = userService.login(req.getIdUser(), req.getPassword());

            HttpSession session = request.getSession();
            request.changeSessionId();
            session.setAttribute("loginUser", new LoginUserDto(u.getId(), u.getName(), u.getIdUser(), u.getRole()));

            String url = openRedirect(request.getParameter("redirectURL"));
            return "redirect:" + url;
        } catch (IllegalArgumentException ex) {
            model.addAttribute("loginFail", ex.getMessage());
            return "users/loginUserForm";
        }
    }

    private String openRedirect(String redirectURL) {
        if (redirectURL == null || redirectURL.isBlank())
            return "/";
        if (!redirectURL.startsWith("/"))
            return "/";
        return redirectURL;
    }

    // 로그아웃 처리
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null)
            session.invalidate();

        return "redirect:/";
    }
}
