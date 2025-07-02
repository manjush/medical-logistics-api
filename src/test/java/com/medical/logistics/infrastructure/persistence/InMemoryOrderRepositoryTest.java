package com.medical.logistics.infrastructure.persistence;

import com.medical.logistics.domian.order.Order;
import com.medical.logistics.domian.order.OrderId;
import com.medical.logistics.domian.order.OrderItem;
import com.medical.logistics.domian.order.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class InMemoryOrderRepositoryTest {

    private InMemoryOrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryOrderRepository();
    }

    @Test
    @DisplayName("Should save and retrieve order")
    void shouldSaveAndRetrieveOrder() {
        // Given
        Order order = Order.create(List.of(new OrderItem("Syringe", 10)));

        // When
        Order savedOrder = repository.save(order);
        Optional<Order> retrievedOrder = repository.findById(order.getId());

        // Then
        assertThat(savedOrder).isEqualTo(order);
        assertThat(retrievedOrder).isPresent();
        assertThat(retrievedOrder.get()).isEqualTo(order);
    }

    @Test
    @DisplayName("Should return empty when order not found")
    void shouldReturnEmptyWhenOrderNotFound() {
        // Given
        OrderId nonExistentId = OrderId.generate();

        // When
        Optional<Order> result = repository.findById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find all orders")
    void shouldFindAllOrders() {
        // Given
        Order order1 = Order.create(List.of(new OrderItem("Item1", 10)));
        Order order2 = Order.create(List.of(new OrderItem("Item2", 20)));
        Order order3 = Order.create(List.of(new OrderItem("Item3", 30)));

        repository.save(order1);
        repository.save(order2);
        repository.save(order3);

        // When
        List<Order> orders = repository.findAll();

        // Then
        assertThat(orders).hasSize(3);
        assertThat(orders).containsExactlyInAnyOrder(order1, order2, order3);
    }

    @Test
    @DisplayName("Should update existing order")
    void shouldUpdateExistingOrder() {
        // Given
        Order order = Order.create(List.of(new OrderItem("Mask", 100)));
        repository.save(order);

        // When
        order.approve();
        repository.save(order);

        Optional<Order> retrievedOrder = repository.findById(order.getId());

        // Then
        assertThat(retrievedOrder).isPresent();
        assertThat(retrievedOrder.get().getStatus()).isEqualTo(OrderStatus.APPROVED);
    }

    @Test
    @DisplayName("Should return empty list when no orders")
    void shouldReturnEmptyListWhenNoOrders() {
        // When
        List<Order> orders = repository.findAll();

        // Then
        assertThat(orders).isEmpty();
    }
}