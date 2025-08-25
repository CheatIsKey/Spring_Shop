package capstone.capstone_shop.controller;

import capstone.capstone_shop.domain.Category;
import capstone.capstone_shop.domain.Category_Item;
import capstone.capstone_shop.domain.item.Game;
import capstone.capstone_shop.domain.item.Item;
import capstone.capstone_shop.domain.item.Movie;
import capstone.capstone_shop.domain.item.Music;
import capstone.capstone_shop.dto.ItemForm;
import capstone.capstone_shop.repository.CategoryItemRepository;
import capstone.capstone_shop.repository.CategoryRepository;
import capstone.capstone_shop.service.GcsUploader;
import capstone.capstone_shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class ItemFormController {

    private final ItemService itemService;
    private final GcsUploader gcsUploader;
    private final CategoryRepository categoryRepository;
    private final CategoryItemRepository categoryItemRepository;

    @GetMapping("/items/new")
    public String createForm(Model model){
        model.addAttribute("itemForm", new ItemForm());
        model.addAttribute("categories", categoryRepository.findAll());

        return "items/newItems";
    }

    @PostMapping("/items/new")
    public String createItem(@ModelAttribute ItemForm form) throws IOException {

        String imageUrl = gcsUploader.uploadFile(form.getImage());

        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        Item item;
        switch (category.getName()) {
            case "게임":
                item = new Game(form.getName(), form.getPrice(), form.getStockQuantity(),
                        imageUrl);
                break;
            case "영화":
                item = new Movie(form.getName(), form.getPrice(), form.getStockQuantity(),
                        imageUrl);
                break;
            case "음악":
                item = new Music(form.getName(), form.getPrice(), form.getStockQuantity(),
                        imageUrl);
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 카테고리입니다.");
        }

        itemService.saveItem(item);

        Category_Item ci = new Category_Item(category, item);
        categoryItemRepository.save(ci);

        return "redirect:/";
    }
}
