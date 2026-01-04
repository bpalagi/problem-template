package com.acme.orders;

import com.acme.orders.controller.OrderController;
import com.acme.orders.model.Order;
import com.acme.orders.service.IOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.acme.orders.model.OrderItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IOrderService orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNumber("ORD-00000001");
        testOrder.setCustomerName("Test Customer");
        testOrder.setStatus("PENDING");
        testOrder.setAmount(new BigDecimal("99.99"));
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getAllOrders_ReturnsOrderList() throws Exception {
        when(orderService.getAllOrders()).thenReturn(Arrays.asList(testOrder));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-00000001"))
                .andExpect(jsonPath("$[0].customerName").value("Test Customer"));
    }

    @Test
    void getOrderById_WhenExists_ReturnsOrder() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD-00000001"))
                .andExpect(jsonPath("$.customerName").value("Test Customer"));
    }

    @Test
    void getOrderById_WhenNotExists_Returns404() throws Exception {
        when(orderService.getOrderById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrderByNumber_WhenExists_ReturnsOrder() throws Exception {
        when(orderService.getOrderByOrderNumber("ORD-00000001")).thenReturn(Optional.of(testOrder));

        mockMvc.perform(get("/api/orders/number/ORD-00000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD-00000001"));
    }

    @Test
    void createOrder_ReturnsCreatedOrder() throws Exception {
        when(orderService.createOrder(any(Order.class))).thenReturn(testOrder);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNumber\":\"ORD-00000001\",\"customerName\":\"Test Customer\",\"status\":\"PENDING\",\"amount\":99.99}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("ORD-00000001"));
    }

    @Test
    void updateOrder_WhenExists_ReturnsUpdatedOrder() throws Exception {
        Order updatedOrder = new Order();
        updatedOrder.setId(1L);
        updatedOrder.setOrderNumber("ORD-00000001");
        updatedOrder.setCustomerName("Updated Customer");
        updatedOrder.setStatus("SHIPPED");
        updatedOrder.setAmount(new BigDecimal("149.99"));

        when(orderService.updateOrder(eq(1L), any(Order.class))).thenReturn(Optional.of(updatedOrder));

        mockMvc.perform(put("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Updated Customer\",\"status\":\"SHIPPED\",\"amount\":149.99}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Updated Customer"))
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void updateOrderByNumber_WhenExists_ReturnsOk() throws Exception {
        when(orderService.updateOrderByOrderNumber(eq("ORD-00000001"), any(Order.class))).thenReturn(true);

        mockMvc.perform(put("/api/orders/number/ORD-00000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Updated Customer\",\"status\":\"SHIPPED\",\"amount\":149.99}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateOrderByNumber_WhenNotExists_Returns404() throws Exception {
        when(orderService.updateOrderByOrderNumber(eq("ORD-99999999"), any(Order.class))).thenReturn(false);

        mockMvc.perform(put("/api/orders/number/ORD-99999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Updated Customer\",\"status\":\"SHIPPED\",\"amount\":149.99}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrder_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getOrderDetailsByNumber_WhenExists_ReturnsOrderWithItems() throws Exception {
        // Create order with items
        Order orderWithItems = new Order();
        orderWithItems.setId(1L);
        orderWithItems.setOrderNumber("ORD-00000001");
        orderWithItems.setCustomerName("Test Customer");
        orderWithItems.setStatus("PENDING");
        orderWithItems.setAmount(new BigDecimal("99.99"));
        orderWithItems.setCreatedAt(LocalDateTime.now());
        orderWithItems.setUpdatedAt(LocalDateTime.now());

        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        item1.setOrderNumber("ORD-00000001");
        item1.setProductSku("SKU-001");
        item1.setProductName("Test Product");
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("49.99"));

        orderWithItems.setItems(Arrays.asList(item1));

        when(orderService.getOrderWithItemsByOrderNumber("ORD-00000001")).thenReturn(Optional.of(orderWithItems));

        mockMvc.perform(get("/api/orders/number/ORD-00000001/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD-00000001"))
                .andExpect(jsonPath("$.customerName").value("Test Customer"))
                .andExpect(jsonPath("$.items[0].productSku").value("SKU-001"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void getOrderDetailsByNumber_WhenNotExists_Returns404() throws Exception {
        when(orderService.getOrderWithItemsByOrderNumber("ORD-99999999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/number/ORD-99999999/details"))
                .andExpect(status().isNotFound());
    }
}
