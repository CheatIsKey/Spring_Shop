package capstone.capstone_shop.controller;

import capstone.capstone_shop.domain.cart.Cart;
import capstone.capstone_shop.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalCartBadgeAdvice {

    private final CartService cartService;

    @ModelAttribute("cartKindCound")
    public Integer cartKindCount(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("CART");
        return (cart == null) ? 0 : cart.getItemCount();
    }
}
