package com.medical.logistics.interfaces.rest;

import com.medical.logistics.application.order.OrderApplicationService;
import com.medical.logistics.application.order.commands.ApproveOrderCommand;
import com.medical.logistics.application.order.commands.PlaceOrderCommand;
import com.medical.logistics.domian.order.Order;
import com.medical.logistics.domian.order.OrderId;
import com.medical.logistics.domian.order.OrderItem;
import com.medical.logistics.domian.order.OrderStatus;
import com.medical.logistics.domian.order.exceptions.OrderNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderApplicationService orderService;

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrderSuccessfully() throws Exception {
        // Given
        String requestBody = """
            {
                "items": [
                    {"name": "Syringe", "quantity": 10},
                    {"name": "Bandage", "quantity": 20}
                ]
            }
            """;

        OrderId orderId = OrderId.generate();
        Order order = new Order(
                orderId,
                List.of(new OrderItem("Syringe", 10), new OrderItem("Bandage", 20)),
                OrderStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(orderService.handle(any(PlaceOrderCommand.class))).thenReturn(orderId);
        when(orderService.getOrder(orderId)).thenReturn(order);

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderId.getValue().toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items[0].name").value("Syringe"))
                .andExpect(jsonPath("$.items[0].quantity").value(10));
    }

    @Test
    @DisplayName("Should return bad request for empty items")
    void shouldReturnBadRequestForEmptyItems() throws Exception {
        // Given
        String requestBody = """
            {
                "items": []
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should approve order successfully")
    void shouldApproveOrderSuccessfully() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        Order order = new Order(
                OrderId.of(orderId),
                List.of(new OrderItem("Mask", 100)),
                OrderStatus.APPROVED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(orderService.getOrder(any())).thenReturn(order);

        // When & Then
        mockMvc.perform(put("/api/orders/{orderId}/approve", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("Should return not found when order doesn't exist")
    void shouldReturnNotFoundWhenOrderDoesntExist() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        doThrow(new OrderNotFoundException("Order not found"))
                .when(orderService).handle(any(ApproveOrderCommand.class));

        // When & Then
        mockMvc.perform(put("/api/orders/{orderId}/approve", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should cancel order successfully")
    void shouldCancelOrderSuccessfully() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        Order order = new Order(
                OrderId.of(orderId),
                List.of(new OrderItem("Thermometer", 5)),
                OrderStatus.CANCELLED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(orderService.getOrder(any())).thenReturn(order);

        // When & Then
        mockMvc.perform(put("/api/orders/{orderId}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Should get all orders")
    void shouldGetAllOrders() throws Exception {
        // Given
        Order order1 = Order.create(List.of(new OrderItem("Item1", 10)));
        Order order2 = Order.create(List.of(new OrderItem("Item2", 20)));

        when(orderService.getAllOrders()).thenReturn(List.of(order1, order2));

        // When & Then
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].status").value("PENDING"))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Should get order by id")
    void shouldGetOrderById() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        Order order = new Order(
                OrderId.of(orderId),
                List.of(new OrderItem("Gloves", 50)),
                OrderStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(orderService.getOrder(any())).thenReturn(order);

        // When & Then
        mockMvc.perform(get("/api/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}