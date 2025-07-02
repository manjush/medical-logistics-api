package com.medical.logistics.domian.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class OrderIdTest {

    @Test
    @DisplayName("Should generate unique OrderId")
    void shouldGenerateUniqueOrderId() {
        // When
        OrderId id1 = OrderId.generate();
        OrderId id2 = OrderId.generate();

        // Then
        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1.getValue()).isNotEqualTo(id2.getValue());
    }

    @Test
    @DisplayName("Should create OrderId from UUID")
    void shouldCreateOrderIdFromUUID() {
        // Given
        UUID uuid = UUID.randomUUID();

        // When
        OrderId orderId = OrderId.of(uuid);

        // Then
        assertThat(orderId.getValue()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Should create OrderId from String")
    void shouldCreateOrderIdFromString() {
        // Given
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();

        // When
        OrderId orderId = OrderId.of(uuidString);

        // Then
        assertThat(orderId.getValue()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Should throw exception for invalid UUID string")
    void shouldThrowExceptionForInvalidUUIDString() {
        // Given
        String invalidUuid = "invalid-uuid";

        // When/Then
        assertThatThrownBy(() -> OrderId.of(invalidUuid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid OrderId format");
    }

    @Test
    @DisplayName("Should throw exception for null UUID")
    void shouldThrowExceptionForNullUUID() {
        assertThatThrownBy(() -> OrderId.of((UUID) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("OrderId value cannot be null");
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        UUID uuid = UUID.randomUUID();
        OrderId id1 = OrderId.of(uuid);
        OrderId id2 = OrderId.of(uuid);
        OrderId id3 = OrderId.generate();

        // Then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        assertThat(id1).isNotEqualTo(id3);
        assertThat(id1).isNotEqualTo(null);
        assertThat(id1).isNotEqualTo("not an OrderId");
    }
}