package com.acme.orders.repository;

import com.acme.orders.model.Order;
import com.acme.orders.model.OrderItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
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

    /**
     * Finds an order with all its items by order number.
     * NOTE: This performs a lookup on order_number which has no index - will be slow!
     */
    public Optional<Order> findOrderWithItemsByOrderNumber(String orderNumber) {
        // First get the order
        Optional<Order> orderOpt = findByOrderNumber(orderNumber);
        if (orderOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Order order = orderOpt.get();
        
        // Then get all items for this order (full table scan on order_items!)
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
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO orders (order_number, customer_name, customer_email, status, amount, shipping_address, order_metadata, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, order.getOrderNumber());
            ps.setString(2, order.getCustomerName());
            ps.setString(3, order.getCustomerEmail());
            ps.setString(4, order.getStatus());
            ps.setBigDecimal(5, order.getAmount());
            ps.setString(6, order.getShippingAddress());
            ps.setString(7, order.getOrderMetadata());
            ps.setTimestamp(8, Timestamp.valueOf(now));
            ps.setTimestamp(9, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        // Handle both SQLite (returns Number directly) and PostgreSQL (returns map with 'id' key)
        Number key = keyHolder.getKey();
        if (key != null) {
            order.setId(key.longValue());
        } else if (keyHolder.getKeys() != null && keyHolder.getKeys().containsKey("id")) {
            order.setId(((Number) keyHolder.getKeys().get("id")).longValue());
        }
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

    /**
     * Updates an order by order_number.
     * NOTE: This query performs a full table scan because there is no index on order_number.
     */
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
}
