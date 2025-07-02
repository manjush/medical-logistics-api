package com.medical.logistics.domian.order;

import com.medical.logistics.domian.order.exceptions.InvalidOrderStateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    @Nested
    @DisplayName("Order Creation")
    class OrderCreation {

        @Test
        @DisplayName("Should create order with valid items")
        void shouldCreateOrderWithValidItems() {
            // Given
            List<OrderItem> items = List.of(
                    new OrderItem("Syringe", 10),
                    new OrderItem("Bandage", 20)
            );

            // When
            Order order = Order.create(items);

            // Then
            assertThat(order).isNotNull();
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getItems()).hasSize(2);
            assertThat(order.getId()).isNotNull();
            assertThat(order.getCreatedAt()).isNotNull();
            assertThat(order.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should not create order without items")
        void shouldNotCreateOrderWithoutItems() {
            assertThatThrownBy(() -> Order.create(Collections.emptyList()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Order must include at least 1 item");
        }

        @Test
        @DisplayName("Should not create order with null items")
        void shouldNotCreateOrderWithNullItems() {
            assertThatThrownBy(() -> Order.create(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Order must include at least 1 item");
        }
    }

    @Nested
    @DisplayName("Order Approval")
    class OrderApproval {

        @Test
        @DisplayName("Should approve pending order")
        void shouldApprovePendingOrder() {
            // Given
            Order order = Order.create(List.of(new OrderItem("Mask", 100)));
            LocalDateTime originalUpdatedAt = order.getUpdatedAt();

            // When
            order.approve();

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.APPROVED);
            assertThat(order.getUpdatedAt()).isAfter(originalUpdatedAt);
        }

        @Test
        @DisplayName("Should not approve already approved order")
        void shouldNotApproveAlreadyApprovedOrder() {
            // Given
            Order order = Order.create(List.of(new OrderItem("Gloves", 50)));
            order.approve();

            // When/Then
            assertThatThrownBy(() -> order.approve())
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot approve order in APPROVED status");
        }

        @Test
        @DisplayName("Should not approve cancelled order")
        void shouldNotApproveCancelledOrder() {
            // Given
            Order order = Order.create(List.of(new OrderItem("Thermometer", 5)));
            order.cancel();

            // When/Then
            assertThatThrownBy(() -> order.approve())
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot approve order in CANCELLED status");
        }
    }

    @Nested
    @DisplayName("Order Cancellation")
    class OrderCancellation {

        @Test
        @DisplayName("Should cancel pending order")
        void shouldCancelPendingOrder() {
            // Given
            Order order = Order.create(List.of(new OrderItem("Thermometer", 5)));
            LocalDateTime originalUpdatedAt = order.getUpdatedAt();

            // When
            order.cancel();

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(order.getUpdatedAt()).isAfter(originalUpdatedAt);
        }

        @Test
        @DisplayName("Should not cancel approved order")
        void shouldNotCancelApprovedOrder() {
            // Given
            Order order = Order.create(List.of(new OrderItem("Vaccine", 200)));
            order.approve();

            // When/Then
            assertThatThrownBy(order::cancel)
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot cancel order in APPROVED status");
        }

        @Test
        @DisplayName("Should not cancel already cancelled order")
        void shouldNotCancelAlreadyCancelledOrder() {
            // Given
            Order order = Order.create(List.of(new OrderItem("Stethoscope", 3)));
            order.cancel();

            // When/Then
            assertThatThrownBy(order::cancel)
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot cancel order in CANCELLED status");
        }
    }
}