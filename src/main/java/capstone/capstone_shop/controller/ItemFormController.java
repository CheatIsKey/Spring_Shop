package capstone.capstone_shop.controller;

import capstone.capstone_shop.domain.Category;
import capstone.capstone_shop.domain.item.Game;
import capstone.capstone_shop.domain.item.Item;
import capstone.capstone_shop.domain.item.Movie;
import capstone.capstone_shop.domain.item.Music;
import capstone.capstone_shop.dto.ItemForm;
import capstone.capstone_shop.dto.LoginUserDto;
import capstone.capstone_shop.repository.CategoryRepository;
import capstone.capstone_shop.service.ItemService;
import capstone.capstone_shop.service.storage.ImageStorage;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemFormController {

    private final ItemService itemService;
    private final ImageStorage imageStorage;
    private final CategoryRepository categoryRepository;

    /** 모든 뷰에서 사용하도록 카테고리 목록 주입 */
    @ModelAttribute("categories")
    public List<Category> categories() {
        return categoryRepository.findAll();
    }

    /** 폼 바인딩 대상 보장 (Thymeleaf에서 th:object가 null이면 500) */
    @GetMapping("/new")
    public String createForm(@ModelAttribute("itemForm") ItemForm form) {
        return "items/newItems";
    }

    @PostMapping("/new")
    public String createItem(@Valid @ModelAttribute("itemForm") ItemForm form,
                             BindingResult binding,
                             Model model,
                             RedirectAttributes ra,
                             HttpSession session) {
        // 로그인 확인
        LoginUserDto login = (LoginUserDto) session.getAttribute("loginUser");
        if (login == null) {
            ra.addFlashAttribute("error", "로그인 후 이용해 주세요.");
            return "redirect:/login";
        }

        // 파일 검증
        if (form.getImage() == null || form.getImage().isEmpty()) {
            binding.rejectValue("image", "NotEmpty", "이미지를 업로드해 주세요.");
        }
        if (binding.hasErrors()) {
            return "items/newItems";
        }

        // 업로드
        final String imageUrl;
        try {
            imageUrl = imageStorage.upload(form.getImage());
        } catch (RuntimeException e) {
            binding.reject("imageUploadFail", "이미지 업로드에 실패했습니다. 잠시 후 다시 시도해 주세요.");
            return "items/newItems";
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            binding.reject("imageUploadFail", "이미지 업로드에 실패했습니다. 잠시 후 다시 시도해 주세요.");
            return "items/newItems";
        }

        // 카테고리 확인
        Category category = categoryRepository.findById(form.getCategoryId()).orElse(null);
        if (category == null) {
            binding.rejectValue("categoryId", "NotFound", "존재하지 않는 카테고리입니다.");
            return "items/newItems";
        }

        // 카테고리에 맞는 서브클래스 생성
        Item item;
        switch (category.getName()) {
            case "게임" -> item = new Game(form.getName(), form.getPrice(), form.getStockQuantity(), imageUrl);
            case "영화" -> item = new Movie(form.getName(), form.getPrice(), form.getStockQuantity(), imageUrl);
            case "음악" -> item = new Music(form.getName(), form.getPrice(), form.getStockQuantity(), imageUrl);
            default -> {
                binding.rejectValue("categoryId", "Unsupported", "지원하지 않는 카테고리입니다.");
                return "items/newItems";
            }
        }

        // 상세 내용
        item.setContent(form.getContent());

        // ✅ 작성자 정보(로그인 유저)까지 같이 저장
        Long itemId = itemService.saveItemWithCategoryAndOwner(item, form.getCategoryId(), login.id());

        ra.addFlashAttribute("toast", "상품이 등록되었습니다.");
        return "redirect:/items/" + itemId;
    }
}
