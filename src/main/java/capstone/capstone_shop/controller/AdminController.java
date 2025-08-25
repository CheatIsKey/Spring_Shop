package capstone.capstone_shop.controller;

import capstone.capstone_shop.domain.UserRole;
import capstone.capstone_shop.dto.AdminUserDto;
import capstone.capstone_shop.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
}
