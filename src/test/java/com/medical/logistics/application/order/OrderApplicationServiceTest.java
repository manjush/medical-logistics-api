package com.medical.logistics.application.order;

import com.medical.logistics.application.order.commands.*;
import com.medical.logistics.domian.order.*;
import com.medical.logistics.domian.order.exceptions.OrderNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderApplicationService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderApplicationService(orderRepository);
    }

    @Test
    @DisplayName("Should place order successfully")
    void shouldPlaceOrderSuccessfully() {
        // Given
        PlaceOrderCommand command = new PlaceOrderCommand(List.of(
                new PlaceOrderCommand.OrderItemCommand("Syringe", 10),
                new PlaceOrderCommand.OrderItemCommand("Bandage", 20)
        ));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderId orderId = orderService.handle(command);

        // Then
        assertThat(orderId).isNotNull();
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should approve order successfully")
    void shouldApproveOrderSuccessfully() {
        // Given
        OrderId orderId = OrderId.generate();
        Order order = Order.create(List.of(new OrderItem("Mask", 100)));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        orderService.handle(new ApproveOrderCommand(orderId));

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
        assertThatThrownBy(() -> orderService.handle(new ApproveOrderCommand(orderId)))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found with id");
    }

    @Test
    @DisplayName("Should cancel order successfully")
    void shouldCancelOrderSuccessfully() {
        // Given
        OrderId orderId = OrderId.generate();
        Order order = Order.create(List.of(new OrderItem("Thermometer", 5)));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        orderService.handle(new CancelOrderCommand(orderId));

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Should get all orders")
    void shouldGetAllOrders() {
        // Given
        List<Order> orders = List.of(
                Order.create(List.of(new OrderItem("Item1", 10))),
                Order.create(List.of(new OrderItem("Item2", 20)))
        );
        when(orderRepository.findAll()).thenReturn(orders);

        // When
        List<Order> result = orderService.getAllOrders();

        // Then
        assertThat(result).hasSize(2);
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get order by id")
    void shouldGetOrderById() {
        // Given
        OrderId orderId = OrderId.generate();
        Order order = Order.create(List.of(new OrderItem("Gloves", 50)));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When
        Order result = orderService.getOrder(orderId);

        // Then
        assertThat(result).isEqualTo(order);
        verify(orderRepository, times(1)).findById(orderId);
    }
}