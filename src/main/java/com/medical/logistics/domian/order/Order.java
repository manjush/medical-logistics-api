package com.medical.logistics.domian.order;

import com.medical.logistics.domian.order.exceptions.InvalidOrderStateException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Order Aggregate Root
 */
public class Order {

    private final OrderId id;

    private final List<OrderItem> items;

    private OrderStatus status;

    private final LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    /**
     * Factory method for creating new orders
     */
    public static Order create(List<OrderItem> items) {
        validateItems(items);
        return new Order(
                OrderId.generate(),
                new ArrayList<>(items),
                OrderStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // Constructor used when loading from database
    public Order(OrderId id, List<OrderItem> items, OrderStatus status,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "Order ID cannot be null");
        this.items = new ArrayList<>(Objects.requireNonNull(items, "Items cannot be null"));
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created date cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated date cannot be null");
    }

    public void approve() {
        if (!canTransitionTo(OrderStatus.APPROVED)) {
            throw new InvalidOrderStateException(
                    String.format("Cannot approve order in %s status", status)
            );
        }
        this.status = OrderStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (!canTransitionTo(OrderStatus.CANCELLED)) {
            throw new InvalidOrderStateException(
                    String.format("Cannot cancel order in %s status", status)
            );
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
        // emit OrderCancelledEvent
    }

    private boolean canTransitionTo(OrderStatus targetStatus) {
        return status == OrderStatus.PENDING;
    }

    private static void validateItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must include at least 1 item");
        }
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderId getId() {
        return id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
