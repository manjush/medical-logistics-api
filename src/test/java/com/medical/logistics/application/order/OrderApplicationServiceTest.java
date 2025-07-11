package com.medical.logistics.application.order;

import com.medical.logistics.application.order.commands.*;
import com.medical.logistics.domian.order.*;
import com.medical.logistics.domian.order.exceptions.OrderNotFoundException;
import com.medical.logistics.interfaces.rest.OrderMapper;
import com.medical.logistics.interfaces.rest.dto.OrderItemDto;
import com.medical.logistics.interfaces.rest.dto.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    private OrderApplicationServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderApplicationServiceImpl(orderRepository, orderMapper);
    }

    @Test
    @DisplayName("Should place order successfully")
    void shouldPlaceOrderSuccessfully() {
        // Given
        PlaceOrderCommand command = new PlaceOrderCommand(List.of(
                new PlaceOrderCommand.OrderItemCommand("Syringe", 10),
                new PlaceOrderCommand.OrderItemCommand("Bandage", 20)
        ));

        Order savedOrder = Order.create(List.of(
                new OrderItem("Syringe", 10),
                new OrderItem("Bandage", 20)
        ));

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(savedOrder.getId().getValue())
                .status("PENDING")
                .items(List.of(
                        new OrderItemDto("Syringe", 10),
                        new OrderItemDto("Bandage", 20)
                ))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toResponse(savedOrder)).thenReturn(expectedResponse);

        // When
        OrderResponse response = orderService.placeOrder(command);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getItems()).hasSize(2);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderMapper, times(1)).toResponse(savedOrder);
    }

    @Test
    @DisplayName("Should approve order successfully")
    void shouldApproveOrderSuccessfully() {
        // Given
        OrderId orderId = OrderId.generate();
        Order order = Order.create(List.of(new OrderItem("Mask", 100)));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        orderService.approveOrder(new ApproveOrderCommand(orderId));

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.APPROVED);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Should throw exception when order not found for approval")
    void shouldThrowExceptionWhenOrderNotFoundForApproval() {
        // Given
        OrderId orderId = OrderId.generate();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> orderService.approveOrder(new ApproveOrderCommand(orderId)))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found with id");

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should cancel order successfully")
    void shouldCancelOrderSuccessfully() {
        // Given
        OrderId orderId = OrderId.generate();
        Order order = Order.create(List.of(new OrderItem("Thermometer", 5)));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        orderService.cancelOrder(new CancelOrderCommand(orderId));

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Should get all orders")
    void shouldGetAllOrders() {
        // Given
        Order order1 = Order.create(List.of(new OrderItem("Item1", 10)));
        Order order2 = Order.create(List.of(new OrderItem("Item2", 20)));

        List<Order> orders = List.of(order1, order2);

        OrderResponse response1 = OrderResponse.builder()
                .id(order1.getId().getValue())
                .status("PENDING")
                .items(List.of(new OrderItemDto("Item1", 10)))
                .build();

        OrderResponse response2 = OrderResponse.builder()
                .id(order2.getId().getValue())
                .status("PENDING")
                .items(List.of(new OrderItemDto("Item2", 20)))
                .build();

        when(orderRepository.findAll()).thenReturn(orders);
        when(orderMapper.toResponse(order1)).thenReturn(response1);
        when(orderMapper.toResponse(order2)).thenReturn(response2);

        // When
        List<OrderResponse> result = orderService.getAllOrders();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getItems()).hasSize(1);
        assertThat(result.get(1).getItems()).hasSize(1);
        verify(orderRepository, times(1)).findAll();
        verify(orderMapper, times(2)).toResponse(any(Order.class));
    }

    @Test
    @DisplayName("Should get order by id")
    void shouldGetOrderById() {
        // Given
        OrderId orderId = OrderId.generate();
        Order order = Order.create(List.of(new OrderItem("Gloves", 50)));

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId.getValue())
                .status("PENDING")
                .items(List.of(new OrderItemDto("Gloves", 50)))
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(expectedResponse);

        // When
        OrderResponse result = orderService.getOrder(orderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId.getValue());
        assertThat(result.getItems()).hasSize(1);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderMapper, times(1)).toResponse(order);
    }

    @Test
    @DisplayName("Should throw exception when order not found by id")
    void shouldThrowExceptionWhenOrderNotFoundById() {
        // Given
        OrderId orderId = OrderId.generate();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> orderService.getOrder(orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found with id");

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderMapper, never()).toResponse(any());
    }
}