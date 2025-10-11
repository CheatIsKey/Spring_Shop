package capstone.capstone_shop.controller;

import capstone.capstone_shop.domain.item.Game;
import capstone.capstone_shop.domain.item.Item;
import capstone.capstone_shop.domain.item.Movie;
import capstone.capstone_shop.domain.item.Music;
import capstone.capstone_shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ItemViewController {

    private final ItemService itemService;

    @GetMapping("/items/{id}")
    public String view(@PathVariable Long id, Model model) {
        // 서비스에 맞춰서 메서드명만 조정하세요: getItem / findOne / findById 등
        Item item = itemService.findById(id); // <- 없다면 itemService.getItem(id) 등으로 변경

        String categoryName = null;
        if (item instanceof Game)  categoryName = "게임";
        if (item instanceof Movie) categoryName = "영화";
        if (item instanceof Music) categoryName = "음악";

        model.addAttribute("item", item);
        model.addAttribute("categoryName", categoryName);
        return "items/detail";
    }
}
