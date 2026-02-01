package src.repository;

import src.models.Order;
import src.enums.OrderStatus;

import java.util.List;
import java.util.stream.Collectors;

public class OrderRepository extends InMemoryRepository<Order> {

    @Override
    protected String getEntityId(Order entity) {
        return String.valueOf(entity.getOrderId());
    }

    public List<Order> findByCustomerId(long customerId) {
        return findAll().stream()
                .filter(order -> order.getCustomerId() == customerId)
                .collect(Collectors.toList());
    }

    public List<Order> findByStatus(OrderStatus status) {
        return findAll().stream()
                .filter(order -> order.getStatus() == status)
                .collect(Collectors.toList());
    }
}