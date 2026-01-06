package com.acme.orders.service;

import com.acme.orders.model.Order;

import java.util.List;
import java.util.Optional;

public interface IOrderService {
    List<Order> getAllOrders();
    Optional<Order> getOrderById(Long id);
    Optional<Order> getOrderByOrderNumber(String orderNumber);
    Optional<Order> getOrderWithItemsByOrderNumber(String orderNumber);
    Order createOrder(Order order);
    Optional<Order> updateOrder(Long id, Order orderDetails);
    boolean updateOrderByOrderNumber(String orderNumber, Order orderDetails);
    void deleteOrder(Long id);
    long getOrderCount();
    List<Order> getRecentOrdersWithItems(int limit);
}
