package com.acme.orders.service;

import com.acme.orders.model.Order;
import com.acme.orders.model.OrderItem;
import com.acme.orders.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<Order> getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public Optional<Order> getOrderWithItemsByOrderNumber(String orderNumber) {
        return orderRepository.findOrderWithItemsByOrderNumber(orderNumber);
    }

    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    public Optional<Order> updateOrder(Long id, Order orderDetails) {
        return orderRepository.findById(id).map(existingOrder -> {
            existingOrder.setCustomerName(orderDetails.getCustomerName());
            existingOrder.setStatus(orderDetails.getStatus());
            existingOrder.setAmount(orderDetails.getAmount());
            return orderRepository.save(existingOrder);
        });
    }

    public boolean updateOrderByOrderNumber(String orderNumber, Order orderDetails) {
        int updated = orderRepository.updateByOrderNumber(orderNumber, orderDetails);
        return updated > 0;
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    public long getOrderCount() {
        return orderRepository.count();
    }

    public List<Order> getRecentOrdersWithItems(int limit) {
        List<Order> orders = orderRepository.findRecentOrders(limit);
        
        for (Order order : orders) {
            List<OrderItem> items = orderRepository.findItemsByOrderNumber(order.getOrderNumber());
            order.setItems(items);
        }
        
        return orders;
    }
}
