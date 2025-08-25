package capstone.capstone_shop.controller;

import capstone.capstone_shop.domain.cart.Cart;
import capstone.capstone_shop.dto.AddForm;
import capstone.capstone_shop.dto.ChangeForm;
import capstone.capstone_shop.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
@Validated
public class CartController {

    private final CartService cartService;

    @GetMapping
    public String view(HttpSession session, Model model,
                       @ModelAttribute String toast,
                       @ModelAttribute String error) {
        Cart cart = cartService.getCart(session);
        model.addAttribute("cart", cart);
        return "cart/cart";
    }

    @PostMapping("/add")
    public String add(HttpSession session, @ModelAttribute @Validated AddForm form,
                      @RequestParam(required = false, defaultValue = "/cart") String redirectURL,
                      RedirectAttributes ra) {
        try{
            cartService.add(session, form.itemId(), form.quantity());
            ra.addFlashAttribute("toast", "장바구니에 담았습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:" + redirectURL;
    }

    @PostMapping("/change")
    public String change(HttpSession session, @ModelAttribute @Validated ChangeForm form,
                         RedirectAttributes ra) {
        try {
            cartService.changeQuantity(session, form.itemId(), form.quantity());
            ra.addFlashAttribute("toast", "수량이 변경되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String remove(HttpSession session, @RequestParam Long itemId,
                         RedirectAttributes ra) {
        cartService.remove(session, itemId);
        ra.addFlashAttribute("toast", "삭제되었습니다.");
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clear(HttpSession session, RedirectAttributes ra) {
        cartService.clear(session);
        ra.addFlashAttribute("toast", "장바구니를 비웠습니다.");
        return "redirect:/cart";
    }
}
