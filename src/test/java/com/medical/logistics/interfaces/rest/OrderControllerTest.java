package com.medical.logistics.interfaces.rest;

import com.medical.logistics.application.order.OrderApplicationService;
import com.medical.logistics.application.order.commands.ApproveOrderCommand;
import com.medical.logistics.application.order.commands.CancelOrderCommand;
import com.medical.logistics.application.order.commands.PlaceOrderCommand;
import com.medical.logistics.domian.order.Order;
import com.medical.logistics.domian.order.OrderId;
import com.medical.logistics.domian.order.OrderItem;
import com.medical.logistics.domian.order.OrderStatus;
import com.medical.logistics.domian.order.exceptions.InvalidOrderStateException;
import com.medical.logistics.domian.order.exceptions.OrderNotFoundException;
import com.medical.logistics.interfaces.rest.dto.OrderItemDto;
import com.medical.logistics.interfaces.rest.dto.OrderResponse;
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
import static org.mockito.Mockito.*;
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

        UUID orderId = UUID.randomUUID();
        OrderResponse orderResponse = OrderResponse.builder()
                .id(orderId)  // or orderId.toString() if your OrderResponse uses String
                .status("PENDING")
                .items(List.of(
                        new OrderItemDto("Syringe", 10),
                        new OrderItemDto("Bandage", 20)
                ))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderService.placeOrder(any(PlaceOrderCommand.class))).thenReturn(orderResponse);

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items[0].name").value("Syringe"))
                .andExpect(jsonPath("$.items[0].quantity").value(10))
                .andExpect(jsonPath("$.items[1].name").value("Bandage"))
                .andExpect(jsonPath("$.items[1].quantity").value(20));

        verify(orderService, times(1)).placeOrder(any(PlaceOrderCommand.class));
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

        verify(orderService, never()).placeOrder(any());
    }

    @Test
    @DisplayName("Should approve order successfully")
    void shouldApproveOrderSuccessfully() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        OrderResponse approvedOrder = OrderResponse.builder()
                .id(orderId)
                .status("APPROVED")
                .items(List.of(new OrderItemDto("Mask", 100)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        doNothing().when(orderService).approveOrder(any(ApproveOrderCommand.class));
        when(orderService.getOrder(any(OrderId.class))).thenReturn(approvedOrder);

        // When & Then
        mockMvc.perform(put("/api/orders/{orderId}/approve", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.items[0].name").value("Mask"))
                .andExpect(jsonPath("$.items[0].quantity").value(100));

        verify(orderService, times(1)).approveOrder(any(ApproveOrderCommand.class));
        verify(orderService, times(1)).getOrder(any(OrderId.class));
    }


    @Test
    @DisplayName("Should return conflict when order is in invalid state")
    void shouldReturnConflictWhenOrderInInvalidState() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        doThrow(new InvalidOrderStateException("Cannot approve cancelled order"))
                .when(orderService).approveOrder(any(ApproveOrderCommand.class));

        // When & Then
        mockMvc.perform(put("/api/orders/{orderId}/approve", orderId))
                .andExpect(status().isBadRequest());

        verify(orderService, times(1)).approveOrder(any(ApproveOrderCommand.class));
    }

    @Test
    @DisplayName("Should cancel order successfully")
    void shouldCancelOrderSuccessfully() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        OrderResponse cancelledOrder = OrderResponse.builder()
                .id(orderId)
                .status("CANCELLED")
                .items(List.of(new OrderItemDto("Thermometer", 5)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        doNothing().when(orderService).cancelOrder(any(CancelOrderCommand.class));
        when(orderService.getOrder(any(OrderId.class))).thenReturn(cancelledOrder);

        // When & Then
        mockMvc.perform(put("/api/orders/{orderId}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.items[0].name").value("Thermometer"))
                .andExpect(jsonPath("$.items[0].quantity").value(5));

        verify(orderService, times(1)).cancelOrder(any(CancelOrderCommand.class));
        verify(orderService, times(1)).getOrder(any(OrderId.class));
    }

    @Test
    @DisplayName("Should get all orders")
    void shouldGetAllOrders() throws Exception {
        // Given
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        OrderResponse order1 = OrderResponse.builder()
                .id(orderId1)
                .status("PENDING")
                .items(List.of(new OrderItemDto("Item1", 10)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        OrderResponse order2 = OrderResponse.builder()
                .id(orderId2)
                .status("APPROVED")
                .items(List.of(new OrderItemDto("Item2", 20)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderService.getAllOrders()).thenReturn(List.of(order1, order2));

        // When & Then
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId1.toString()))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].items[0].name").value("Item1"))
                .andExpect(jsonPath("$[1].id").value(orderId2.toString()))
                .andExpect(jsonPath("$[1].status").value("APPROVED"))
                .andExpect(jsonPath("$[1].items[0].name").value("Item2"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    @DisplayName("Should get order by id")
    void shouldGetOrderById() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        OrderResponse order = OrderResponse.builder()
                .id(orderId)
                .status("PENDING")
                .items(List.of(new OrderItemDto("Gloves", 50)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderService.getOrder(any(OrderId.class))).thenReturn(order);

        // When & Then
        mockMvc.perform(get("/api/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items[0].name").value("Gloves"))
                .andExpect(jsonPath("$.items[0].quantity").value(50));

        verify(orderService, times(1)).getOrder(any(OrderId.class));
    }

    @Test
    @DisplayName("Should return not found when getting non-existent order")
    void shouldReturnNotFoundWhenGettingNonExistentOrder() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        when(orderService.getOrder(any(OrderId.class)))
                .thenThrow(new OrderNotFoundException("Order not found"));

        // When & Then
        mockMvc.perform(get("/api/orders/{orderId}", orderId))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).getOrder(any(OrderId.class));
    }
}