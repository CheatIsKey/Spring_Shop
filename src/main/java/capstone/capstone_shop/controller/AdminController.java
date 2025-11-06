package capstone.capstone_shop.controller;

import capstone.capstone_shop.domain.UserRole;
import capstone.capstone_shop.dto.AdminUserDto;
import capstone.capstone_shop.dto.LoginUserDto;
import capstone.capstone_shop.service.AdminService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String list(@RequestParam(required = false) String word,
                       @RequestParam(required = false) UserRole role,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {

        Page<AdminUserDto> users = adminService.search(word, role, page, size);

        model.addAttribute("users", users);
        model.addAttribute("word", word);
        model.addAttribute("role", role);

        return "admin/users";
    }

    @PostMapping("/users/{userId}/delete")
    public String deleteUserByAdmin(@PathVariable Long userId,
                                    HttpSession session,
                                    @RequestParam(required = false, defaultValue = "") String redirect,
                                    RedirectAttributes attrs) {

        // 관리자 권한 확인
        LoginUserDto loginUser = (LoginUserDto) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.userRole() != UserRole.ADMIN) {
            attrs.addFlashAttribute("error", "관리자만 접근할 수 있습니다.");
            return "redirect:/login";
        }

        try {
            adminService.deleteUserByAdmin(userId);
            attrs.addFlashAttribute("flashMessage", "해당 유저를 삭제했습니다.");
        } catch (IllegalStateException ex) {
            attrs.addFlashAttribute("error", ex.getMessage());
        } catch (Exception ex) {
            attrs.addFlashAttribute("error", "삭제 처리 중 알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }

        return (redirect != null && !redirect.isBlank())
                ? "redirect:" + redirect
                : "redirect:/admin/users";
    }
}
