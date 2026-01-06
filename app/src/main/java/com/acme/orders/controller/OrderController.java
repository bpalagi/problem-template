package com.acme.orders.controller;

import com.acme.orders.model.Order;
import com.acme.orders.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    private final IOrderService orderService;

    public OrderController(IOrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        long startTime = System.currentTimeMillis();
        List<Order> orders = orderService.getAllOrders();
        long duration = System.currentTimeMillis() - startTime;
        logger.info("GET /api/orders completed in {}ms", duration);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        long startTime = System.currentTimeMillis();
        ResponseEntity<Order> response = orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        long duration = System.currentTimeMillis() - startTime;
        logger.info("GET /api/orders/{} completed in {}ms", id, duration);
        return response;
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Order> getOrderByNumber(@PathVariable String orderNumber) {
        long startTime = System.currentTimeMillis();
        ResponseEntity<Order> response = orderService.getOrderByOrderNumber(orderNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        long duration = System.currentTimeMillis() - startTime;
        logger.info("GET /api/orders/number/{} completed in {}ms", orderNumber, duration);
        return response;
    }

    @GetMapping("/number/{orderNumber}/details")
    public ResponseEntity<Order> getOrderDetailsByNumber(@PathVariable String orderNumber) {
        long startTime = System.currentTimeMillis();
        ResponseEntity<Order> response = orderService.getOrderWithItemsByOrderNumber(orderNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        long duration = System.currentTimeMillis() - startTime;
        
        if (duration > 100) {
            logger.warn("GET /api/orders/number/{}/details completed in {}ms", orderNumber, duration);
        } else {
            logger.info("GET /api/orders/number/{}/details completed in {}ms", orderNumber, duration);
        }
        return response;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        long startTime = System.currentTimeMillis();
        Order created = orderService.createOrder(order);
        long duration = System.currentTimeMillis() - startTime;
        logger.info("POST /api/orders completed in {}ms", duration);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order order) {
        long startTime = System.currentTimeMillis();
        ResponseEntity<Order> response = orderService.updateOrder(id, order)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        long duration = System.currentTimeMillis() - startTime;
        logger.info("PUT /api/orders/{} completed in {}ms", id, duration);
        return response;
    }

    @PutMapping("/number/{orderNumber}")
    public ResponseEntity<Void> updateOrderByNumber(@PathVariable String orderNumber, @RequestBody Order order) {
        long startTime = System.currentTimeMillis();
        boolean updated = orderService.updateOrderByOrderNumber(orderNumber, order);
        long duration = System.currentTimeMillis() - startTime;
        
        if (duration > 500) {
            logger.warn("PUT /api/orders/number/{} completed in {}ms", orderNumber, duration);
        } else {
            logger.info("PUT /api/orders/number/{} completed in {}ms", orderNumber, duration);
        }
        
        if (updated) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        long startTime = System.currentTimeMillis();
        orderService.deleteOrder(id);
        long duration = System.currentTimeMillis() - startTime;
        logger.info("DELETE /api/orders/{} completed in {}ms", id, duration);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getOrderCount() {
        return ResponseEntity.ok(orderService.getOrderCount());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Order>> getRecentOrders(
            @RequestParam(defaultValue = "50") int limit) {
        long startTime = System.currentTimeMillis();
        List<Order> orders = orderService.getRecentOrdersWithItems(Math.min(limit, 200));
        long duration = System.currentTimeMillis() - startTime;
        logger.info("GET /api/orders/recent?limit={} completed in {}ms, returned {} orders", 
                    limit, duration, orders.size());
        return ResponseEntity.ok(orders);
    }
}
