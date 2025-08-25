package capstone.capstone_shop.controller;

import capstone.capstone_shop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAllDto());
        return "admin/categories";
    }

    @PostMapping
    public String create(@RequestParam String name,
                         @RequestParam(required = false) Long parentId,
                         RedirectAttributes attributes) {

        try {
            categoryService.create(name, parentId);
            attributes.addFlashAttribute("flashMessage", "카테고리가 추가되었습니다.");
        } catch (IllegalArgumentException e) {
            attributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes attributes) {

        try {
            categoryService.delete(id);
            attributes.addFlashAttribute("flashMessage", "카테고리가 삭제되었습니다.");
        } catch (Exception e) {
            attributes.addFlashAttribute("errorMessage", "삭제할 수 없습니다. (하위 항목 존재)");
        }

        return "redirect:/admin/categories";
    }
}
