package capstone.capstone_shop.domain.cart;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Cart implements Serializable {

    private final Map<Long, CartItem> items = new LinkedHashMap<>();

    public Collection<CartItem> getItems() {
        return items.values();
    }

    public int getItemCount() {
        return items.size();
    }

    public int getTotalQuantity() {
        return items.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public boolean contains(Long itemId) {
        return items.containsKey(itemId);
    }

    public void addOrIncrease(CartItem newItem, int addQty) {
        CartItem cur = items.get(newItem.getItemId());

        if (cur == null) {
            items.put(newItem.getItemId(), new CartItem(newItem.getItemId(), newItem.getName(),
                    newItem.getImageUrl(), newItem.getPrice(), addQty));
        } else {
            cur.increaseQuantity(addQty);
        }
    }

    public void changeQuantity(Long itemId, int qty) {
        if (!items.containsKey(itemId)) return;
        if (qty <= 0) {
            items.remove(itemId);
            return;
        }
        items.get(itemId).changeQuantity(qty);
    }

    public void remove(Long itemId) {
        items.remove(itemId);
    }

    public void clear() {
        items.clear();
    }

    public int getQuantity(Long itemId) {
        CartItem ci = items.get(itemId);
        return (ci == null) ? 0 : ci.getQuantity();
    }

    public BigDecimal getTotal() {
        return items.values().stream()
                .map( CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
