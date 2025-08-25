package capstone.capstone_shop.controller;

import capstone.capstone_shop.domain.Address;
import capstone.capstone_shop.domain.Order;
import capstone.capstone_shop.domain.cart.Cart;
import capstone.capstone_shop.dto.LoginUserDto;
import capstone.capstone_shop.dto.OrderRowDto;
import capstone.capstone_shop.service.CartService;
import capstone.capstone_shop.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    @PostMapping
    public String placeOrder(@SessionAttribute("loginUser")LoginUserDto loginUserDto,
                             @RequestParam String city,
                             @RequestParam String state,
                             @RequestParam String street,
                             HttpSession session,
                             RedirectAttributes ra) {

        Cart cart = cartService.getCart(session);

        if (cart.getItems().isEmpty()) {
            ra.addFlashAttribute("error", "장바구니가 비어 있습니다.");
            return "redirect:/cart";
        }

        Address address = new Address(state, city, street);
        Long orderId = orderService.placeOrder(loginUserDto.id(), cart, address);

        cartService.clear(session);
        ra.addFlashAttribute("toast", "주문이 완료되었습니다. 주문번호: " + orderId);
        return "redirect:/orders/" + orderId;
    }

    @GetMapping("/my")
    public String myOrders(@SessionAttribute("loginUser") LoginUserDto loginUserDto, Model model) {

        List<Order> orders = orderService.myOrders(loginUserDto.id());
        List<OrderRowDto> rows = orders.stream()
                .map(o -> new OrderRowDto(
                        o.getId(),
                        o.totalPrice(),
                        o.getOrderDate(),
                        o.getStatus().name(),
                        o.getDelivery() != null ? o.getDelivery().getStatus().name() : "-"
                )).toList();

        model.addAttribute("rows", rows);
        return "orders/my";
    }

    @GetMapping("/{orderId}")
    public String detail(@PathVariable Long orderId,
                         @SessionAttribute("loginUser") LoginUserDto loginUserDto,
                         Model model, RedirectAttributes ra) {

        try {
            Order order = orderService.getOrderDetail(orderId, loginUserDto.id());
            model.addAttribute("order", order);
            return "orders/detail";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", "존재하지 않는 주문입니다.");
        } catch (AccessDeniedException e) {
            ra.addFlashAttribute("error", "본인 주문만 조회할 수 있습니다.");
        }
        return "redirect:/orders/my";
    }

    @PostMapping("/{orderId}/cancel")
    public String cancel(@PathVariable Long orderId,
                         @SessionAttribute("loginUser") LoginUserDto loginUserDto,
                         RedirectAttributes ra) throws AccessDeniedException {
        orderService.cancel(orderId, loginUserDto.id());
        ra.addFlashAttribute("toast", "주문이 취소되었습니다.");
        return "redirect:/orders/my";
    }

    @Getter
    public static class OrderRequest {
        @NotBlank private String city;
        @NotBlank private String state;
        @NotBlank private String street;
    }
}
