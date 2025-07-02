package com.medical.logistics.domian.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class OrderItemTest {

    @Test
    @DisplayName("Should create order item with valid data")
    void shouldCreateOrderItemWithValidData() {
        // When
        OrderItem item = new OrderItem("Syringe", 10);

        // Then
        assertThat(item.getName()).isEqualTo("Syringe");
        assertThat(item.getQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should not create order item with null name")
    void shouldNotCreateOrderItemWithNullName() {
        assertThatThrownBy(() -> new OrderItem(null, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Item name is required");
    }

    @Test
    @DisplayName("Should not create order item with empty name")
    void shouldNotCreateOrderItemWithEmptyName() {
        assertThatThrownBy(() -> new OrderItem("", 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Item name is required");
    }

    @Test
    @DisplayName("Should not create order item with blank name")
    void shouldNotCreateOrderItemWithBlankName() {
        assertThatThrownBy(() -> new OrderItem("   ", 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Item name is required");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    @DisplayName("Should not create order item with invalid quantity")
    void shouldNotCreateOrderItemWithInvalidQuantity(int quantity) {
        assertThatThrownBy(() -> new OrderItem("Mask", quantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be at least 1");
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        OrderItem item1 = new OrderItem("Bandage", 20);
        OrderItem item2 = new OrderItem("Bandage", 20);
        OrderItem item3 = new OrderItem("Bandage", 30);
        OrderItem item4 = new OrderItem("Syringe", 20);

        // Then
        assertThat(item1).isEqualTo(item2);
        assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
        assertThat(item1).isNotEqualTo(item3);
        assertThat(item1).isNotEqualTo(item4);
        assertThat(item1).isNotEqualTo(null);
        assertThat(item1).isNotEqualTo("not an OrderItem");
    }
}