package ua.dp.maxym.demo6.order.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends MongoRepository<Order, UUID> {

    default void updateOrderStatus(UUID orderId, OrderStatus orderStatus) {
        Order order = getOrder(orderId);
        Order updatedOrder = new Order(order.id(), orderStatus, order.statusReason(), order.user(), order.goods(),
                                       order.quantity(), order.price());
        save(updatedOrder);
    }

    default void updatePrice(UUID orderId, double price) {
        Order order = getOrder(orderId);
        Order updatedOrder = new Order(order.id(), order.status(), order.statusReason(), order.user(), order.goods(),
                                       order.quantity(), price);
        save(updatedOrder);
    }

    private Order getOrder(UUID orderId) {
        Optional<Order> orderOptional = findById(orderId);
        if (orderOptional.isEmpty()) throw new NoSuchElementException(String.format("Order %s not found", orderId));
        Order order = orderOptional.get();
        return order;
    }
}
