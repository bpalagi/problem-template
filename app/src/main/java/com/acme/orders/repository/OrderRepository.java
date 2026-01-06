package com.acme.orders.repository;

import com.acme.orders.model.Order;
import com.acme.orders.model.OrderItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Order> orderRowMapper = (rs, rowNum) -> {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setOrderNumber(rs.getString("order_number"));
        order.setCustomerName(rs.getString("customer_name"));
        order.setCustomerEmail(rs.getString("customer_email"));
        order.setStatus(rs.getString("status"));
        order.setAmount(rs.getBigDecimal("amount"));
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setOrderMetadata(rs.getString("order_metadata"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            order.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            order.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return order;
    };

    private final RowMapper<OrderItem> orderItemRowMapper = (rs, rowNum) -> {
        OrderItem item = new OrderItem();
        item.setId(rs.getLong("id"));
        item.setOrderNumber(rs.getString("order_number"));
        item.setProductSku(rs.getString("product_sku"));
        item.setProductName(rs.getString("product_name"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            item.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return item;
    };

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Order> findAll() {
        return jdbcTemplate.query("SELECT * FROM orders LIMIT 100", orderRowMapper);
    }

    public Optional<Order> findById(Long id) {
        List<Order> results = jdbcTemplate.query(
            "SELECT * FROM orders WHERE id = ?",
            orderRowMapper,
            id
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<Order> findByOrderNumber(String orderNumber) {
        List<Order> results = jdbcTemplate.query(
            "SELECT * FROM orders WHERE order_number = ?",
            orderRowMapper,
            orderNumber
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<Order> findOrderWithItemsByOrderNumber(String orderNumber) {
        Optional<Order> orderOpt = findByOrderNumber(orderNumber);
        if (orderOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Order order = orderOpt.get();
        
        List<OrderItem> items = jdbcTemplate.query(
            "SELECT * FROM order_items WHERE order_number = ?",
            orderItemRowMapper,
            orderNumber
        );
        
        order.setItems(items);
        return Optional.of(order);
    }

    public Order save(Order order) {
        if (order.getId() == null) {
            return insert(order);
        } else {
            return update(order);
        }
    }

    private Order insert(Order order) {
        LocalDateTime now = LocalDateTime.now();
        
        jdbcTemplate.update(
            "INSERT INTO orders (order_number, customer_name, customer_email, status, amount, shipping_address, order_metadata, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            order.getOrderNumber(),
            order.getCustomerName(),
            order.getCustomerEmail(),
            order.getStatus(),
            order.getAmount(),
            order.getShippingAddress(),
            order.getOrderMetadata(),
            Timestamp.valueOf(now),
            Timestamp.valueOf(now)
        );

        // Get the last inserted ID for SQLite
        Long id = jdbcTemplate.queryForObject("SELECT last_insert_rowid()", Long.class);
        order.setId(id);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        return order;
    }

    private Order update(Order order) {
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
            "UPDATE orders SET customer_name = ?, status = ?, amount = ?, updated_at = ? WHERE id = ?",
            order.getCustomerName(),
            order.getStatus(),
            order.getAmount(),
            Timestamp.valueOf(now),
            order.getId()
        );
        order.setUpdatedAt(now);
        return order;
    }

    public int updateByOrderNumber(String orderNumber, Order order) {
        LocalDateTime now = LocalDateTime.now();
        return jdbcTemplate.update(
            "UPDATE orders SET customer_name = ?, status = ?, amount = ?, updated_at = ? WHERE order_number = ?",
            order.getCustomerName(),
            order.getStatus(),
            order.getAmount(),
            Timestamp.valueOf(now),
            orderNumber
        );
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM orders WHERE id = ?", id);
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Long.class);
        return count != null ? count : 0;
    }

    public List<Order> findRecentOrders(int limit) {
        return jdbcTemplate.query(
            "SELECT * FROM orders ORDER BY created_at DESC LIMIT ?",
            orderRowMapper,
            limit
        );
    }

    public List<OrderItem> findItemsByOrderNumber(String orderNumber) {
        return jdbcTemplate.query(
            "SELECT * FROM order_items WHERE order_number = ?",
            orderItemRowMapper,
            orderNumber
        );
    }
}
