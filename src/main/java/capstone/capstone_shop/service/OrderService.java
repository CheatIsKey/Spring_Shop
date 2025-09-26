package capstone.capstone_shop.service;

import capstone.capstone_shop.domain.*;
import capstone.capstone_shop.domain.cart.Cart;
import capstone.capstone_shop.domain.cart.CartItem;
import capstone.capstone_shop.domain.item.Item;
import capstone.capstone_shop.repository.ItemRepository;
import capstone.capstone_shop.repository.OrderRepository;
import capstone.capstone_shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public Long placeOrder(Long userId, Cart cart, Address address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            Item item = itemRepository.findById(ci.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + ci.getItemId()));

            if (item.getStockQuantity() < ci.getQuantity()) {
                throw new IllegalStateException("재고 부족: " + item.getName());
            }

            orderItems.add(OrderItem.create(item, item.getPrice(), ci.getQuantity()));
        }

        Delivery delivery = Delivery.of(address);
        Order order = Order.create(user, delivery, orderItems);

        orderRepository.save(order);
        return order.getId();
    }

    @Transactional(readOnly = true)
    public List<Order> myOrders(Long userId) {
        return orderRepository.findSummaryByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Order getOrderDetail(Long orderId, Long userId) throws AccessDeniedException {
        Order order = orderRepository.findDetailById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (!order.getUser().getId().equals(userId))
            throw new AccessDeniedException("본인 주문만 조회할 수 있습니다.");

        return order;
    }

    public void cancel(Long orderId, Long userId) throws AccessDeniedException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (!order.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("본인 주문만 취소할 수 있습니다.");
        }
        order.cancel();
    }

}
