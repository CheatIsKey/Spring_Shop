package capstone.capstone_shop.service;

import capstone.capstone_shop.domain.cart.Cart;
import capstone.capstone_shop.domain.cart.CartItem;
import capstone.capstone_shop.domain.item.Item;
import capstone.capstone_shop.repository.ItemRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final String CART_KEY = "CART";
    private final ItemRepository itemRepository;

    public Cart getCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute(CART_KEY);

        if (cart == null) {
            cart = new Cart();
            session.setAttribute(CART_KEY, cart);
        }
        return cart;
    }

    public void add(HttpSession session, Long itemId, int qty) {
        if (qty <= 0)
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (item.getStockQuantity() <= 0) {
            throw new IllegalStateException("품절된 상품입니다.");
        }

        Cart cart = getCart(session);

        int stock = item.getStockQuantity();
        int currentInCart = cart.getQuantity(itemId);
        int allowed = stock - currentInCart;

        if (allowed <= 0) {
            throw new IllegalStateException("이미 최대 " + stock + "개까지 담겨 있습니다.");
        }

        int addQty = Math.min(qty, allowed);

        CartItem ci = new CartItem(
                item.getId(),
                item.getName(),
                item.getImageUrl(),
                BigDecimal.valueOf(item.getPrice()),
                0
        );
        cart.addOrIncrease(ci, addQty);
    }

    public void changeQuantity(HttpSession session, Long itemId, int qty) {
        Cart cart = getCart(session);

        if (qty <= 0) {
            cart.remove(itemId);
            return;
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (item.getStockQuantity() <= 0 ) {
            cart.remove(itemId);
            throw new IllegalStateException("해당 상품이 품절되어 장바구니에서 제거되었습니다.");
        }

        if (item.getStockQuantity() < qty) {
            cart.changeQuantity(itemId, item.getStockQuantity());
            throw new IllegalStateException("최대 " + item.getStockQuantity() + "개로 조정되었습니다.");
        }

        cart.changeQuantity(itemId, qty);
    }

    public void remove(HttpSession session, Long itemId) {
        getCart(session).remove(itemId);
    }

    public void clear(HttpSession session) {
        getCart(session).clear();
    }
}
