package capstone.capstone_shop.controller;

import capstone.capstone_shop.domain.Category;
import capstone.capstone_shop.dto.ItemDto;
import capstone.capstone_shop.service.CategoryService;
import capstone.capstone_shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class Main {

    private final CategoryService categoryService;
    private final ItemService itemService;

    @GetMapping("/")
    public String home(@RequestParam(value = "category", required = false) Long categoryId,
                       @RequestParam(value = "q", required = false) String q,
                       Model model, Principal principal) {

        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);

        List<ItemDto> itemDtos;
        if (q != null && !q.isBlank()) {
            itemDtos = itemService.searchItems(categoryId, q);
        } else if (categoryId != null) {
            itemDtos = itemService.searchItems(categoryId, q);
        } else {
            itemDtos = itemService.findRandomItems();
        }
        model.addAttribute("itemDtos", itemDtos);

        model.addAttribute("cartItemCount", 0);

        model.addAttribute("isLoggedIn", principal != null);
        return "home";
    }

}
