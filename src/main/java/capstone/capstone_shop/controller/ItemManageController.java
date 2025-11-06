package capstone.capstone_shop.controller;

import capstone.capstone_shop.domain.Category;
import capstone.capstone_shop.domain.Category_Item;
import capstone.capstone_shop.domain.item.Item;
import capstone.capstone_shop.dto.ItemEditForm;
import capstone.capstone_shop.dto.LoginUserDto;
import capstone.capstone_shop.repository.CategoryRepository;
import capstone.capstone_shop.service.ItemService;
import capstone.capstone_shop.service.storage.ImageStorage;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
@Validated
public class ItemManageController {

    private final ItemService itemService;
    private final CategoryRepository categoryRepository;
    private final ImageStorage imageStorage;

    @ModelAttribute("categories")
    public List<Category> categories() { return categoryRepository.findAll(); }

    // 내가 등록한 상품 목록
    @GetMapping("/mine")
    public String myItems(HttpSession session, Model model) {
        LoginUserDto login = (LoginUserDto) session.getAttribute("loginUser");
        if (login == null) return "redirect:/login";
        List<Item> items = itemService.findMyItems(login.id());
        model.addAttribute("items", items);
        return "items/myItems";
    }

    // 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes ra) {
        LoginUserDto login = (LoginUserDto) session.getAttribute("loginUser");
        if (login == null) return "redirect:/login";

        Item item = itemService.findByIdWithCategories(id);

        if (item.getCreatedBy() == null || !item.getCreatedBy().getId().equals(login.id())) {
            ra.addFlashAttribute("error", "본인이 등록한 상품만 수정할 수 있습니다.");
            return "redirect:/items/mine";
        }

        Long categoryId = item.getCategoryItems().isEmpty()
                ? null
                : item.getCategoryItems().get(0).getCategory().getId();

        ItemEditForm form = new ItemEditForm();
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setContent(item.getContent());
        form.setCategoryId(categoryId);

        model.addAttribute("form", form);
        model.addAttribute("itemId", id);
        model.addAttribute("currentImageUrl", item.getImageUrl());

        return "items/editItem";
    }

    // 수정 저장
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       HttpSession session,
                       @Valid @ModelAttribute("form") ItemEditForm form,
                       BindingResult binding,
                       RedirectAttributes ra) {
        LoginUserDto login = (LoginUserDto) session.getAttribute("loginUser");
        if (login == null) return "redirect:/login";

        // 유효성 에러 시 폼 다시
        if (binding.hasErrors()) {
            ra.addFlashAttribute("error", "입력값을 확인해 주세요.");
            return "redirect:/items/" + id + "/edit";
        }

        // 선택 이미지 업로드
        String newImageUrl = null;
        if (form.getImage() != null && !form.getImage().isEmpty()) {
            try {
                newImageUrl = imageStorage.upload(form.getImage());
            } catch (RuntimeException e) {
                ra.addFlashAttribute("error", "이미지 업로드 실패. 잠시 후 다시 시도해 주세요.");
                return "redirect:/items/" + id + "/edit";
            }
        }

        try {
            itemService.updateItemByOwner(
                    id,
                    login.id(),
                    form.getName(),
                    form.getPrice(),
                    form.getStockQuantity(),
                    form.getContent(),
                    form.getCategoryId(),
                    newImageUrl // null 이면 기존 이미지 유지
            );
            ra.addFlashAttribute("flashMessage", "상품이 수정되었습니다.");
            return "redirect:/items/mine";
        } catch (IllegalStateException | IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/items/" + id + "/edit";
        }
    }

    // 삭제
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        LoginUserDto login = (LoginUserDto) session.getAttribute("loginUser");
        if (login == null) return "redirect:/login";
        try {
            itemService.deleteItemByOwner(id, login.id());
            ra.addFlashAttribute("flashMessage", "상품이 삭제되었습니다.");
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/items/mine";
    }
}
